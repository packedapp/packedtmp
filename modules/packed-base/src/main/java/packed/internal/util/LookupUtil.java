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

/** A utility class dealing with {@link Lookup} and {@link MethodHandle}. */
public final class LookupUtil {

    private static final int DEFAULT_LOOKUP_MODES = MethodHandles.lookup().lookupModes();

    private static final MethodHandle PREVIOUS_LOOKUP_CLASS;

    /** Never instantiate. */
    private LookupUtil() {}

    static {
        if (Runtime.version().feature() >= 14) {
            PREVIOUS_LOOKUP_CLASS = findVirtualEIIE(MethodHandles.publicLookup(), Lookup.class, "previousLookupClass", MethodType.methodType(Class.class));
        } else {
            PREVIOUS_LOOKUP_CLASS = null;
        }
    }

    private static Class<?> previousLookupClass(Lookup lookup) {
//        if (PREVIOUS_LOOKUP_CLASS == null) {
//            return null;
//        }
        try {
            return (Class<?>) PREVIOUS_LOOKUP_CLASS.invokeExact(lookup);
        } catch (Throwable e) {
            throw ThrowableUtil.rethrowAsUndeclared(e);
        }
    }

    public static void main(String[] args) {
        // Maaske behoever vi slet ikke tests previous... fordi lookup mode aendrer sig..
        System.out.println(isLookupDefault(MethodHandles.lookup().in(Global.class)));
    }

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

    public static MethodHandle findVirtualEIIE(MethodHandles.Lookup caller, String name, MethodType type) {
        return findVirtualEIIE(caller, caller.lookupClass(), name, type);
    }

    /**
     * A utility method that wraps any {@link ReflectiveOperationException} thrown by
     * {@link Lookup#findVirtual(Class, String, MethodType)} in an {@link ExceptionInInitializerError}.
     * 
     * @param caller
     *            the caller
     * @param refc
     *            the ref
     * @param name
     *            the method name
     * @param type
     *            the method type
     * @return a method handle
     * @see Lookup#findVirtual(Class, String, MethodType)
     * @throws ExceptionInInitializerError
     *             if the {@link Lookup#findVirtual(Class, String, MethodType)} fails with an reflection operation exception
     */
    public static MethodHandle findVirtualEIIE(MethodHandles.Lookup caller, Class<?> refc, String name, MethodType type) {
        try {
            return caller.findVirtual(refc, name, type);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static MethodHandle findStaticEIIE(MethodHandles.Lookup caller, Class<?> refc, String name, MethodType type) {
        try {
            return caller.findStatic(refc, name, type);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}
