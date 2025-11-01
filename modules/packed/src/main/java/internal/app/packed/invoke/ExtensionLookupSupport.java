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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import app.packed.Framework;
import app.packed.bean.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.InternalExtensionException;
import internal.app.packed.extension.ExtensionClassModel;
import internal.app.packed.invoke.MethodHandleInvoker.ExtensionFactory;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.ClassUtil;

/**
 *
 */
public class ExtensionLookupSupport {

    public static MethodHandle findBeanIntrospector(Class<? extends BeanIntrospector<?>> extensionClass) {
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
        try {
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            MethodHandle mh = l.unreflectConstructor(constructor);
            // cast from (Concrete BeanIntrospector class) -> (BeanIntrospector)
            return MethodHandles.explicitCastArguments(mh, MethodType.methodType(BeanIntrospector.class));
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException(extensionClass + " must be open to '" + BeanIntrospector.class.getModule().getName() + "'", e);
        }
    }

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
        try {
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            MethodHandle mh = l.unreflectConstructor(constructor);
            // cast from (ExtensionClass) -> (Extension)
            mh = MethodHandles.explicitCastArguments(mh, MethodType.methodType(Extension.class, ExtensionHandle.class));
            return new ExtensionFactory(mh);
        } catch (IllegalAccessException e) {
            throw new InternalExtensionException(extensionClass + " must be open to '" + Framework.class.getModule().getName() + "'", e);
        }
    }

    public static void forceLoad(Class<? extends Extension<?>> extensionClass) {
        // Ensure that the class initializer of the extension has been run before we progress
        try {
            ExtensionClassModel.class.getModule().addReads(extensionClass.getModule());
            Lookup l = MethodHandles.privateLookupIn(extensionClass, MethodHandles.lookup());
            l.ensureInitialized(extensionClass);
        } catch (IllegalAccessException e) {
            // TODO this is likely the first place we check that an extension is readable by Packed
            // Better error message..
            // Maybe we have other stuff that we need to check here...
            // We need to be open.. In order to create the extension...
            // So probably no point in just checking for Readable...
            throw new InternalExtensionException("Extension is not readable for " + Framework.name(), e);
        }
    }
}
