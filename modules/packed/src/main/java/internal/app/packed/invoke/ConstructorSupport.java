/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package internal.app.packed.invoke;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import app.packed.Framework;
import app.packed.bean.BeanIntrospector;
import app.packed.build.BuildException;
import app.packed.build.hook.ApplyBuildHook;
import app.packed.build.hook.BuildHook;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.assembly.AssemblyClassModel;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.extension.PackedExtensionHandle;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
public class ConstructorSupport {

    public static BuildHookFactory newBuildHookFactory(Class<?> assemblyClass, ApplyBuildHook applyHook, Class<? extends BuildHook> type) {
        MethodHandle constructor;

        if (!AssemblyClassModel.class.getModule().canRead(type.getModule())) {
            AssemblyClassModel.class.getModule().addReads(type.getModule());
        }

        Lookup privateLookup;
        try {
            privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException(e1);
        }
        // TODO fix visibility
        // Maybe common findConstructorMethod
        try {
            constructor = privateLookup.findConstructor(type, MethodType.methodType(void.class));
        } catch (NoSuchMethodException e) {
            throw new BuildException("A container hook must provide an empty constructor, hook = " + applyHook, e);
        } catch (IllegalAccessException e) {
            throw new BuildException("Can't see it sorry, hook = " + applyHook, e);
        }

        // For consistency reasons we always tries to use invokeExact() even if not strictly needed
        constructor = constructor.asType(MethodType.methodType(BuildHook.class));

        return new BuildHookFactory(constructor);
    }

    public static BeanIntrospectorFactory findBeanIntrospector(Class<? extends BeanIntrospector<?>> extensionClass) {
        if (Modifier.isAbstract(extensionClass.getModifiers())) {
            throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an abstract class");
        } else if (ClassUtil.isInnerOrLocal(extensionClass)) {
            throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an an inner or local class");
        }

        // An extension must provide an empty constructor
        Constructor<?>[] constructors = extensionClass.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new InternalExtensionException(StringFormatter.format(extensionClass) + " must declare a single constructor taking no arguments");
        }

        Constructor<?> constructor = constructors[0];
        if (constructor.getParameterCount() != 0) {
            throw new InternalExtensionException(extensionClass + " must declare a single a constructor taking ExtensionHandle, but constructor required "
                    + Arrays.toString(constructor.getParameters()));
        }

        // Create a MethodHandle for the constructor
        MethodHandle mh;
        try {
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            mh = l.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException(illegalAccessExtensionMsg(extensionClass), e);
        }
        // cast from (Concrete BeanIntrospector class) -> (BeanIntrospector)
        mh = MethodHandles.explicitCastArguments(mh, MethodType.methodType(BeanIntrospector.class));
        return new BeanIntrospectorFactory(mh);
    }

    /**
     * Finds a constructor on an extension.
     *
     * @param extensionClass
     *            the extension class to find the constructor on.
     * @return an extension factory for creating extension instances of the specified extension class type
     *
     * @throws InternalExtensionException
     *             if a valid constructor could not be found
     */
    public static ExtensionFactory findExtensionConstructor(Class<? extends Extension<?>> extensionClass) {
        if (Modifier.isAbstract(extensionClass.getModifiers())) {
            throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an abstract class");
        } else if (ClassUtil.isInnerOrLocal(extensionClass)) {
            throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot be an an inner or local class");
        }

        // An extension must provide an empty constructor
        Constructor<?>[] constructors = extensionClass.getDeclaredConstructors();
        if (constructors.length != 1) {
            throw new InternalExtensionException(StringFormatter.format(extensionClass) + " must declare exactly 1 constructor");
        }

        Constructor<?> constructor = constructors[0];
        if (constructor.getParameterCount() != 1 || constructor.getParameterTypes()[0] != ExtensionHandle.class) {
            throw new InternalExtensionException(extensionClass + " must provide a constructor taking ExtensionHandle, but constructor required "
                    + Arrays.toString(constructor.getParameters()));
        }

        // The constructor must be non-public
        if (Modifier.isPublic(constructor.getModifiers())) {
            throw new InternalExtensionException(extensionClass + " cannot declare a public constructor");
        }

        // Create a MethodHandle for the constructor
        MethodHandle mh;
        try {
            // Highly unlikely to fails since we have already loaded it
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            mh = l.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException(illegalAccessExtensionMsg(extensionClass), e);
        }
        // cast from (ExtensionClass) -> (Extension)
        mh = MethodHandles.explicitCastArguments(mh, MethodType.methodType(Extension.class, ExtensionHandle.class));
        return new ExtensionFactory(mh);
    }

    public static void forceLoad(Class<? extends Extension<?>> extensionClass) {
        // Ensure that the class initializer of the extension has been run before we progress
        try {
            // We need to read the module the extension is in
            if (ExtensionClassModel.class.getModule() != extensionClass.getModule()) {
                ExtensionClassModel.class.getModule().addReads(extensionClass.getModule());
            }
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            l.ensureInitialized(extensionClass);
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException(illegalAccessExtensionMsg(extensionClass), e);
        }
    }

    public static String illegalAccessExtensionMsg(Class<?> clazz) {
        return clazz + " must be opened to " + Framework.name() + " by adding this line 'opens " + clazz.getPackageName() + " to "
                + ConstructorSupport.class.getModule() + "' to module-info.java";
    }

    /** A factory class for creating instances of {@link Extension} */
    public static final class ExtensionFactory {
        private final MethodHandle mh; // (ExtensionHandle)Extension

        public ExtensionFactory(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public Extension<?> create(ExtensionSetup extension) {
            ExtensionHandle<?> handle = new PackedExtensionHandle<>(extension);
            try {
                return (Extension<?>) mh.invokeExact(handle);
            } catch (Throwable e) {
                throw new InternalExtensionException("An instance of the extension " + extension.model.fullName() + " could not be created.", e);
            }
        }
    }

    public static final class BuildHookFactory {
        private final MethodHandle mh;

        public BuildHookFactory(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public BuildHook create() {
            try {
                return (BuildHook) mh.invokeExact();
            } catch (Throwable t) {
                throw ThrowableUtil.orUndeclared(t);
            }
        }
    }

    /** A factory class for creating instances of {@link BeanIntrospector} */
    public static final class BeanIntrospectorFactory {
        private final MethodHandle mh; // ()BeanIntrospector

        public BeanIntrospectorFactory(MethodHandle mh) {
            this.mh = requireNonNull(mh);
        }

        public BeanIntrospector<?> create() {
            try {
                return (BeanIntrospector<?>) mh.invokeExact();
            } catch (Throwable e) {
                throw new InternalExtensionException("An instance of " + mh.type().returnType() + " could not be created.", e);
            }
        }
    }

}
