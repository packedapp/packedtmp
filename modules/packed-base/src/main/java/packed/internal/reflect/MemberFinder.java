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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import packed.internal.util.function.ThrowableConsumer;

/**
 * Processes all fields and methods on a specified class.
 */
// https://stackoverflow.com/questions/28400408/what-is-the-new-way-of-getting-all-methods-of-a-class-including-inherited-defau
public final class MemberFinder {

    /** We never process any classes that are located in java.base. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** Never instantiate. */
    private MemberFinder() {}

    private static <T extends Throwable> void find(Class<?> baseType, Class<?> actualType, ThrowableConsumer<? super Method, T> methodConsumer,
            ThrowableConsumer<? super Field, T> fieldConsumer) throws T {
        HashSet<Package> packages = new HashSet<>();
        HashMap<MethodEntry, HashSet<Package>> types = new HashMap<>();

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : actualType.getMethods()) {
            // Filter methods whose declaring class is in java.base and bridge methods
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                // Should we also ignore methods on base bundle class????
                methodConsumer.accept(m);// move this to step 2???
                types.put(new MethodEntry(m), packages);
            }
        }

        // Step 2 process all declared methods
        for (Class<?> c = actualType; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            if (fieldConsumer != null) {
                for (Field field : c.getDeclaredFields()) {
                    fieldConsumer.accept(field);
                }
            }

            for (Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == actualType && !Modifier.isPublic(mod)) { // we have already processed public static methods
                        // only include static methods in the top level class
                        // We do this, because it would be strange to include
                        // static methods on any interfaces this class implements.
                        // But it would also be strange to include static methods on sub classes
                        // but not include static methods on interfaces.
                        methodConsumer.accept(m);
                    }
                } else if (!m.isBridge()) {
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new MethodEntry(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new MethodEntry(m), packages) != null) {
                            continue;
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    methodConsumer.accept(m);
                }
            }
        }
    }

    public static <T extends Throwable> void findMethods(Class<?> baseType, Class<?> actualType, ThrowableConsumer<? super Method, T> methodConsumer) throws T {
        find(baseType, actualType, methodConsumer, null);
    }

    public static <T extends Throwable> void findMethodsAndFields(Class<?> baseType, Class<?> actualType, ThrowableConsumer<? super Method, T> methodConsumer,
            ThrowableConsumer<? super Field, T> fieldConsumer) throws T {
        find(baseType, actualType, methodConsumer, fieldConsumer);
    }

    private static final class MethodEntry {

        /** A pre-calculated hash. */
        private final int hash;

        /** The name of the method */
        private final String name;

        /** The parameters of the method. */
        private final Class<?>[] parameterTypes;

        /**
         * Creates a new entry for the specified method.
         * 
         * @param method
         *            the method
         */
        private MethodEntry(Method method) {
            this.name = method.getName();
            this.parameterTypes = method.getParameterTypes();
            this.hash = name.hashCode() ^ Arrays.hashCode(parameterTypes);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodEntry) {
                MethodEntry e = (MethodEntry) obj;
                // name is always interned so just use ==
                return name == e.name && Arrays.equals(parameterTypes, e.parameterTypes);
            }
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
