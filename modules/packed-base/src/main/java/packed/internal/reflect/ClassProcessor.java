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
package packed.internal.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import app.packed.lang.NativeImage;
import app.packed.lang.reflect.UncheckedIllegalAccessException;
import packed.internal.util.StringFormatter;
import packed.internal.util.UncheckedThrowableFactory;

/**
 *
 */
// Make sure the constructor is registered if we are generating a native image
// NativeImage.registerConstructor(constructor);
public class ClassProcessor {

    /** The app.packed.base module. */
    private static final Module THIS_MODULE = ClassProcessor.class.getModule();

    /** The class that can be processed. */
    private final Class<?> clazz;

    /** A lookup object that can be used to access {@link #clazz}. */
    private final MethodHandles.Lookup lookup;

    private boolean lookupInitialized;

    private MethodHandles.Lookup privateLookup;

    /** Whether or not every unreflected action results in the member being registered for native image generation. */
    private final boolean registerForNative;

    public ClassProcessor(MethodHandles.Lookup lookup, Class<?> clazz, boolean registerForNative) {
        this.lookup = requireNonNull(lookup);
        this.clazz = requireNonNull(clazz);
        this.registerForNative = registerForNative;
    }

    private <T extends Throwable> void checkPackageOpen(UncheckedThrowableFactory<T> tf) throws T {
        String pckName = clazz.getPackageName();
        if (!clazz.getModule().isOpen(pckName, THIS_MODULE)) {
            String otherModule = clazz.getModule().getName();
            String m = THIS_MODULE.getName();
            throw tf.newThrowable("In order to access '" + StringFormatter.format(clazz) + "', the module '" + otherModule + "' must be open to '" + m
                    + "'. This can be done, for example, by adding 'opens " + pckName + " to " + m + ";' to the module-info.java file of " + otherModule);
        }
    }

    /**
     * Returns the class that is processed.
     * 
     * @return the class that is processed
     */
    public Class<?> clazz() {
        return clazz;
    }

    public void findMethods(Consumer<? super Method> methodConsumer) {
        MemberFinder.findMethods(Object.class, clazz, methodConsumer);
    }

    public void findMethodsAndFields(Class<?> baseType, Consumer<? super Method> methodConsumer, Consumer<? super Field> fieldConsumer) {
        MemberFinder.findMethodsAndFields(baseType, clazz, methodConsumer, fieldConsumer);
    }

    public void findMethodsAndFields(Consumer<? super Method> methodConsumer, Consumer<? super Field> fieldConsumer) {
        MemberFinder.findMethodsAndFields(Object.class, clazz, methodConsumer, fieldConsumer);
    }

    private <T extends Throwable> Lookup lookup(Member member, UncheckedThrowableFactory<T> tf) throws T {
        if (!member.getDeclaringClass().isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Was " + member.getDeclaringClass() + " expecting " + clazz);
        }

        if (!lookupInitialized) {
            checkPackageOpen(tf);
            // Should we use lookup.getdeclaringClass???
            if (!THIS_MODULE.canRead(clazz.getModule())) {
                THIS_MODULE.addReads(clazz.getModule());
            }
            lookupInitialized = true;
        }

        // If we already have made a private lookup object, lets just use it. Even if we might need less access
        MethodHandles.Lookup p = privateLookup;
        if (p != null) {
            return p;
        }

        // See if we need private access, otherwise just return ordinary lookup.
        if (!needsPrivateLookup(member)) {
            return lookup;
        }

        // Create and cache a private lookup.
        try {
            return privateLookup = MethodHandles.privateLookupIn(clazz, lookup);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create private lookup", e);
        }
    }

    public ClassProcessor spawn(Class<?> clazz) {
        //
        if (clazz.getModule() != this.clazz.getModule()) {
            throw new IllegalArgumentException();
        }
        return new ClassProcessor(lookup, clazz, registerForNative);
    }

    /**
     * @param method
     *            the method to unreflect
     * @return a method handle for the unreflected method
     */
    public <T extends Throwable> MethodHandle unreflect(Method method, UncheckedThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(method, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("stuff", e);
        }

        if (registerForNative) {
            NativeImage.registerMethod(method);
        }
        return mh;
    }

    public <T extends Throwable> MethodHandle unreflectConstructor(Constructor<?> constructor, UncheckedThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(constructor, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerConstructor(constructor);
        }
        return mh;
    }

    public <T extends Throwable> MethodHandle unreflectGetter(Field field, UncheckedThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(field, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return mh;
    }

    public <T extends Throwable> MethodHandle unreflectSetter(Field field, UncheckedThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(field, tf);

        MethodHandle mh;
        try {
            mh = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a MethodHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return mh;
    }

    public <T extends Throwable> VarHandle unreflectVarhandle(Field field, UncheckedThrowableFactory<T> tf) throws T {
        Lookup lookup = lookup(field, tf);

        VarHandle vh;
        try {
            vh = lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException("Could not create a VarHandle", e);
        }

        if (registerForNative) {
            NativeImage.registerField(field);
        }
        return vh;
    }

    private static boolean needsPrivateLookup(Member m) {
        // Needs private lookup, unless class is public or protected and member is public
        int decMod = m.getDeclaringClass().getModifiers();
        return !((Modifier.isPublic(decMod) || Modifier.isProtected(decMod)) && Modifier.isPublic(m.getModifiers()));
    }
}
// Check to see, if we need to use a private lookup

// if (needsPrivateLookup(constructor)) {
// // TODO check module access on lookup object, lol see next comment. No need to check
//
// try {
// lookup = MethodHandles.privateLookupIn(onType, lookup);
// } catch (IllegalAccessException e) {
// // This should never happen, because we have checked all preconditions
// // And we use our own lookup object which have Module access mode enabled.
// // Maybe something with unnamed modules...
// throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details",
// e);
// }
// }
//
// // Finally, lets unreflect the constructor
// MethodHandle methodHandle;
// try {
// methodHandle = lookup.unreflectConstructor(constructor);
// } catch (IllegalAccessException e) {
// throw new UncheckedIllegalAccessException("This exception was not expected, please file a bug report with details",
// e);
// }