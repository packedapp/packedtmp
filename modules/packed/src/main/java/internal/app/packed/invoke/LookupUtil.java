/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.lang.invoke.VarHandle;

/** Various {@link Lookup} utility methods. */
final class LookupUtil {

    private static final int DEFAULT_LOOKUP_MODES = MethodHandles.lookup().lookupModes();

    /** Never instantiate. */
    private LookupUtil() {}

    public static MethodHandle findConstructor(MethodHandles.Lookup caller, Class<?> inClass, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(void.class, parameterTypes);
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(inClass, caller);
            return l.findConstructor(inClass, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle findStaticSelf(MethodHandles.Lookup caller, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return caller.findStatic(caller.lookupClass(), name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle findStaticPublic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return MethodHandles.publicLookup().findStatic(refc, name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

//    public static VarHandle findVarHandle(MethodHandles.Lookup lookup, Class<?> recv, String name, Class<?> type) {
//        try {
//            MethodHandles.Lookup l = MethodHandles.privateLookupIn(recv, lookup);
//            return l.findVarHandle(recv, name, type);
//        } catch (ReflectiveOperationException e) {
//            throw new ExceptionInInitializerError(e);
//        }
//    }

    public static VarHandle findVarHandle(MethodHandles.Lookup lookup, String name, Class<?> type) {
        try {
            return lookup.findVarHandle(lookup.lookupClass(), name, type);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // finds a method in a class that is different form lookup.lookupClass
    public static MethodHandle findVirtual(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(refc, lookup);
            return l.findVirtual(refc, name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle findVirtual(MethodHandles.Lookup caller, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return caller.findVirtual(caller.lookupClass(), name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * A utility method that wraps any {@link ReflectiveOperationException} thrown by
     * {@link Lookup#findVirtual(Class, String, MethodType)} in an {@link ExceptionInInitializerError}.
     *
     * @param refc
     *            the ref
     * @param name
     *            the method name
     * @return a method handle
     * @see Lookup#findVirtual(Class, String, MethodType)
     * @throws ExceptionInInitializerError
     *             if the {@link Lookup#findVirtual(Class, String, MethodType)} fails with an reflection operation exception
     */
    // Finds a method that is public (typically any method in java.base)
    public static MethodHandle findVirtualPublic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return MethodHandles.publicLookup().findVirtual(refc, name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Tests whether or not the specified lookup is
     *
     * @param lookup
     *            the lookup to the
     * @return whether it is default
     */
    public static boolean isLookupDefault(Lookup lookup) {
        return lookup.lookupModes() == DEFAULT_LOOKUP_MODES && lookup.previousLookupClass() == null;
    }
}
