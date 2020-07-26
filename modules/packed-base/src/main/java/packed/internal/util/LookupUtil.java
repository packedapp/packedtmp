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
package packed.internal.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

/** Various {@link Lookup} utility methods. */
public final class LookupUtil {

    private static final int DEFAULT_LOOKUP_MODES = MethodHandles.lookup().lookupModes();

    private static final MethodHandle PREVIOUS_LOOKUP_CLASS;

    static {
        if (Runtime.version().feature() >= 14) {
            PREVIOUS_LOOKUP_CLASS = mhVirtualPublic(Lookup.class, "previousLookupClass", Class.class);
        } else {
            PREVIOUS_LOOKUP_CLASS = null;
        }
    }

    /** Never instantiate. */
    private LookupUtil() {}

    /**
     * Tests whether or not the specified lookup is
     * 
     * @param lookup
     *            the lookup to the
     * @return whether it is default
     */
    public static boolean isLookupDefault(Lookup lookup) {
        return lookup.lookupModes() == DEFAULT_LOOKUP_MODES && (PREVIOUS_LOOKUP_CLASS == null || previousLookupClass(lookup) == null);
    }

    // finds a method in a class that is different form lookup.lookupClass
    public static MethodHandle mhVirtualPrivate(MethodHandles.Lookup lookup, Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(refc, lookup);
            return l.findVirtual(refc, name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle mhStaticPublic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return MethodHandles.publicLookup().findStatic(refc, name, mt);
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
    public static MethodHandle mhVirtualPublic(Class<?> refc, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return MethodHandles.publicLookup().findVirtual(refc, name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Finds a method in caller.lookupClass
    public static MethodHandle mhVirtualSelf(MethodHandles.Lookup caller, String name, Class<?> returnType, Class<?>... parameterTypes) {
        MethodType mt = MethodType.methodType(returnType, parameterTypes);
        try {
            return caller.findVirtual(caller.lookupClass(), name, mt);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Class<?> previousLookupClass(Lookup lookup) {
//        if (PREVIOUS_LOOKUP_CLASS == null) {
//            return null;
//        }
        try {
            return (Class<?>) PREVIOUS_LOOKUP_CLASS.invokeExact(lookup);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    public static VarHandle vhPrivateOther(MethodHandles.Lookup lookup, Class<?> recv, String name, Class<?> type) {
        try {
            MethodHandles.Lookup l = MethodHandles.privateLookupIn(recv, lookup);
            return l.findVarHandle(recv, name, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static VarHandle vhSelf(MethodHandles.Lookup lookup, String name, Class<?> type) {
        try {
            return lookup.findVarHandle(lookup.lookupClass(), name, type);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
