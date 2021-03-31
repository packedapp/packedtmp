package packed.internal.invoke;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public abstract class ClassScanner {

    /** We never process classes that are located in the java.base module. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    protected abstract void onMethod(Method method);

    protected void onField(Field field) {}

    public final void scan(Class<?> actualType, boolean reflectOnFields, Class<?> baseType) {
        HashSet<Package> packages = new HashSet<>();
        HashMap<Helper, HashSet<Package>> types = new HashMap<>();

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : actualType.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(new Helper(m), packages);
                // Should we also ignore methods on base assembly class????
                onMethod(m);// move this to step 2???
            }
        }
        
        // Step 2 process all declared methods
        
        // Maybe some kind of detection if current type (c) switches modules. 
        for (Class<?> c = actualType; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            if (reflectOnFields) {
                for (Field field : c.getDeclaredFields()) {
                    onField(field);
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
                        onMethod(m);
                    }
                } else if (!m.isBridge()) {
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(new Helper(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(new Helper(m), packages) != null) {
                            continue; // method has been overridden by a super type
                        }
                        // otherwise fall-through
                    case Modifier.PRIVATE:
                        // Private methods are never overridden
                    }
                    onMethod(m);
                }
            }
        }
    }

    /** Processes all fields and methods on a class. */
    record Helper(int hash, String name, Class<?>[] parameterTypes) {

        /**
         * Creates a new entry for the specified method.
         * 
         * @param method
         *            the method
         */
        private Helper(Method method) {
            this(method.getName(), method.getParameterTypes());
        }

        private Helper(String name, Class<?>[] parameterTypes) {
            this(name.hashCode() ^ Arrays.hashCode(parameterTypes), name, parameterTypes);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object obj) {
            return obj instanceof Helper h && name == h.name() && Arrays.equals(parameterTypes, h.parameterTypes);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return hash;
        }
    }
}
