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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Processes all fields and methods on a specified class.
 */
// https://stackoverflow.com/questions/28400408/what-is-the-new-way-of-getting-all-methods-of-a-class-including-inherited-defau
public abstract class MemberProcessor {

    /** We never process any classes that are located in java.base. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** */
    private final HashSet<Package> PKG = new HashSet<>();

    private final Class<?> baseType;

    public final Class<?> actualType;

    public MemberProcessor(Class<?> baseType, Class<?> actualType) {
        this.baseType = requireNonNull(baseType);
        this.actualType = requireNonNull(actualType);
        // TODO we should probably check that actual type is a super type
    }

    public MethodHandle findConstructor(Class<?>... parameterTypes) {
        return ConstructorFinder.extract(actualType, parameterTypes);
    }

    /** Finds all relevant methods and invokes {@link #processMethod(Method)}. */
    public final void findMethods() {
        find(false);
    }

    public final void findMethodsAndFields() {
        find(true);
    }

    private void find(boolean processFields) {
        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        HashMap<MethodEntry, HashSet<Package>> types = new HashMap<>();
        for (Method m : actualType.getMethods()) {
            // Filter methods whose declaring class is in java.base and bridge methods
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                // Should we also ignore methods on base class????
                processMethod(m); // move this to step 2???
                types.put(new MethodEntry(m), PKG);
            }
        }

        // Step 2 process all declared methods
        for (Class<?> c = actualType; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            if (processFields) {
                for (Field field : c.getDeclaredFields()) {
                    processField(field);
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
                        // if we do not include static methods on interfaces.
                        processMethod(m);
                    }
                } else if (!m.isBridge()) {
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new MethodEntry(m), key -> new HashSet<>());
                        if (pkg != PKG && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new MethodEntry(m), PKG) != null) {
                            continue;
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    processMethod(m);
                }
            }
        }
    }

    /**
     * Processes a field.
     * 
     * @param field
     *            the field to process
     */
    protected void processField(Field field) {
        throw new IllegalStateException("This method should be overridden, if field processing is enabled");
    }

    protected void processMethod(Method method) {
        throw new IllegalStateException("This method should be overridden, if method processing is enabled");
    }

    private static final class MethodEntry {

        /** A pre calculated hash. */
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
