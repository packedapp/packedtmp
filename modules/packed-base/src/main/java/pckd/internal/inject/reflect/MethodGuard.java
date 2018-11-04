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
package pckd.internal.inject.reflect;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class MethodGuard {

    private final long guard;

    private final Class<?> guardedClass;

    /** A cache of class mirrors. */
    private static final ClassValue<ClassInfo> CACHE = new ClassValue<ClassInfo>() {

        /** {@inheritDoc} */
        @Override
        protected ClassInfo computeValue(Class<?> type) {
            return new ClassInfo(type);
        }
    };
    /** A cache of class mirrors. */
    private static final ClassValue<Integer> COLLISIONS = new ClassValue<Integer>() {

        private final AtomicInteger COLLISION_COUNTER = new AtomicInteger();

        /** {@inheritDoc} */
        @Override
        protected Integer computeValue(Class<?> type) {
            return COLLISION_COUNTER.getAndIncrement();
        }
    };

    private MethodGuard(Class<?> guardedClass, long guard) {
        this.guardedClass = requireNonNull(guardedClass);
        this.guard = guard;
    }

    public long guard() {
        return guard;
    }

    public static MethodGuard of(Class<?> guardedClass, String methodName) {
        throw new UnsupportedOperationException();
    }

    public static MethodGuard of(Class<?> guardedClass, String methodName, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    static class ClassInfo {
        final Method[] methods;

        ClassInfo(Class<?> clazz) {
            methods = clazz.getDeclaredMethods();
            Arrays.sort(methods, MethodSorter.INSTANCE);
            // check size 64
        }

        long get(String methodName) {
            return -1;
        }
    }

    /** Actually we do not need this..... */
    static class MethodSorter implements Comparator<Method> {
        static MethodSorter INSTANCE = new MethodSorter();

        /** {@inheritDoc} */
        @Override
        public int compare(Method m1, Method m2) {
            int val = m1.getName().compareTo(m2.getName());
            if (val == 0) {
                val = m1.getParameterCount() - m2.getParameterCount();
                if (val == 0) {
                    Parameter[] p1 = m1.getParameters();
                    Parameter[] p2 = m2.getParameters();
                    for (int i = 0; i < p2.length; i++) {
                        Class<?> c1 = p1[i].getType();
                        Class<?> c2 = p2[i].getType();
                        if (c1 != c2) {
                            String n1 = c1.getCanonicalName();
                            String n2 = c1.getCanonicalName();
                            if (n1 != null) {
                                if (n2 == null) {
                                    return 1;
                                } else {
                                    return n1.compareTo(n2);
                                }
                            } else if (n2 != null) {
                                return -1;
                            } else {
                                return COLLISIONS.get(c1) - COLLISIONS.get(c2);
                            }
                        }
                    }
                    throw new Error("Should never get here: " + m1 + ", " + m2);
                }
            }
            return val;
        }
    }
    // of(ComponentListener, "onsd","asdsda","asdasd","asdasd);
}
