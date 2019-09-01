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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Processes all fields and methods on selected classes.
 */
// https://stackoverflow.com/questions/28400408/what-is-the-new-way-of-getting-all-methods-of-a-class-including-inherited-defau
public abstract class FieldAndMethodProcessor {

    /** We never process any classes that are located in java.base. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    /** */
    private static final HashSet<Package> PKG = new HashSet<>();

    public final void process(Class<?> cl) {
        // Step 1, find all public methods (including default methods)
        HashMap<Key, HashSet<Package>> types = new HashMap<>();

        for (Method m : cl.getMethods()) {
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                processMethod(m);
                types.put(new Key(m), PKG);
            }
        }

        // Step 2 process all declared methods
        for (Class<?> c = cl; c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            for (Field field : c.getDeclaredFields()) {
                processField(field);
            }

            for (Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == cl && !Modifier.isPublic(mod)) { // we have already processed public static methods
                        // only include static methods in the top level class
                        // We do this, because I'm it would be strange to include
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
                        HashSet<Package> pkg = types.computeIfAbsent(new Key(m), key -> new HashSet<>());
                        if (pkg != PKG && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new Key(m), PKG) != null) {
                            continue;
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Never overridden
                    }
                    processMethod(m);
                }
            }
        }
    }

    protected abstract void processField(Field f);

    protected abstract void processMethod(Method method);

    private static final class Key {
        private final Class<?>[] args;
        private final int hash;
        private final String name;

        Key(Method m) {
            this.name = m.getName();
            this.args = m.getParameterTypes();
            this.hash = name.hashCode() ^ Arrays.hashCode(args);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            Key k = (Key) obj;
            // name is always interned so just use ==
            return name == k.name && Arrays.equals(args, k.args);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
