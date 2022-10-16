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
package internal.deprecated.invoke;

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

import app.packed.bean.InaccessibleBeanMemberException;
import internal.app.packed.util.StringFormatter;

/**
 * An open class is a thin wrapper for a single class and a {@link Lookup} object.
 * <p>
 * This class is not safe for use with multiple threads.
 */
//TODO should we know whether or the lookup is Packed one or a user supplied??
// lookup.getClass().getModule==OpenClass.getModule...? nah virker ikke paa classpath
final class OpenClass {

    /** The app.packed.base module. */
    private static final Module APP_PACKED_BASE_MODULE = OpenClass.class.getModule();

    /** A lookup object that can be used to access {@link #type}. */
    private final MethodHandles.Lookup lookup;

    /** A lookup that can be used on non-public members. */
    private MethodHandles.Lookup privateLookup;

    /** Whether or not the private lookup has been initialized. */
    private boolean privateLookupInitialized;

    /** The class that is wrapped. */
    private final Class<?> type;

    private OpenClass(MethodHandles.Lookup lookup, Class<?> clazz) {
        this.lookup = requireNonNull(lookup);
        this.type = requireNonNull(clazz);
    }

    Lookup lookup(Member member) {
        if (!member.getDeclaringClass().isAssignableFrom(type)) {
            throw new IllegalArgumentException("Was " + member.getDeclaringClass() + " expecting " + type);
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

        if (!privateLookupInitialized) {
            String pckName = type.getPackageName();
            if (!type.getModule().isOpen(pckName, APP_PACKED_BASE_MODULE)) {
                String otherModule = type.getModule().getName();
                String m = APP_PACKED_BASE_MODULE.getName();
                throw new InaccessibleBeanMemberException("In order to access '" + StringFormatter.format(type) + "', the module '" + otherModule
                        + "' must be open to '" + m + "'. This can be done, for example, by adding 'opens " + pckName + " to " + m
                        + ";' to the module-info.java file of " + otherModule);
            }
            // Should we use lookup.getdeclaringClass???
            if (!APP_PACKED_BASE_MODULE.canRead(type.getModule())) {
                APP_PACKED_BASE_MODULE.addReads(type.getModule());
            }
            privateLookupInitialized = true;
        }

        // Create and cache a private lookup.
        try {
            // Fjernede lookup... Skal vitterligt have samlet det i en klasse
            return privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create private lookup [type=" + type + ", Member = " + member + "]", e);
        }
    }

    /**
     * Unreflects the specified method.
     * 
     * @param method
     *            the method to unreflect
     * @return a method handle for the unreflected method
     * @throws InaccessibleBeanMemberException
     *             if the method could not be unreflected
     * @see Lookup#unreflect(Method)
     */
    public MethodHandle unreflect(Method method) {
        Lookup lookup = lookup(method);
        try {
            return lookup.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("stuff", e);
        }
    }

    public MethodHandle unreflectConstructor(Constructor<?> constructor) {
        Lookup lookup = lookup(constructor);

        try {
            return lookup.unreflectConstructor(constructor);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectGetter(Field field) {
        Lookup lookup = lookup(field);

        try {
            return lookup.unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    MethodHandle unreflectSetter(Field field) {
        Lookup lookup = lookup(field);

        try {
            return lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a MethodHandle", e);
        }
    }

    VarHandle unreflectVarHandle(Field field) {
        Lookup lookup = lookup(field);

        try {
            return lookup.unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new InaccessibleBeanMemberException("Could not create a VarHandle", e);
        }
    }

    private static boolean needsPrivateLookup(Member m) {
        // Needs private lookup, unless class is public or protected and member is public
        // We are comparing against the members declaring class..
        // We could store boolean isPublicOrProcected in a field.
        // But do not know how it would work with abstract super classes in other modules...
        int classModifiers = m.getDeclaringClass().getModifiers();
        return !((Modifier.isPublic(classModifiers) || Modifier.isProtected(classModifiers)) && Modifier.isPublic(m.getModifiers()));
    }

    public static OpenClass of(MethodHandles.Lookup lookup, Class<?> clazz) {
        return new OpenClass(lookup, clazz);
    }
}

//IDeen er at man kan specificere den til OpenClass...
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