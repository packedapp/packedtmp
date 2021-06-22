package packed.internal.invoke;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import app.packed.inject.Inject;
import packed.internal.util.StringFormatter;

/**
 * An class that helps with finding members (fields, constructors and methods) on a class.
 */
// Maybe have MethodScanner and MethodAndFieldScanner (extends MethodScanner) + ConstructorScanner
public abstract class MemberScanner {

    /** We never process classes that are located in the java.base module. */
    private static final Module JAVA_BASE_MODULE = Class.class.getModule();

    protected final Class<?> classToScan;

    protected MemberScanner(Class<?> classToScan) {
        this.classToScan = requireNonNull(classToScan);
    }

    /**
     * Invoked for every field from scan if reflectOnFields is true.
     * 
     * @param field
     *            the field that was found
     */
    protected void onField(Field field) {}

    protected abstract void onMethod(Method method);

    /**
     * @param reflectOnFields
     *            whether or not to iterate over fields
     * @param baseType
     *            the base type
     */
    public final void scan(boolean reflectOnFields, Class<?> baseType) {
        HashSet<Package> packages = new HashSet<>();
        HashMap<Helper, HashSet<Package>> types = new HashMap<>();

        // Step 1, .getMethods() is the easiest way to find all default methods. Even if we also have to call
        // getDeclaredMethods() later.
        for (Method m : classToScan.getMethods()) {
            // Filter methods whose from java.base module and bridge methods
            // TODO add check for
            if (m.getDeclaringClass().getModule() != JAVA_BASE_MODULE && !m.isBridge()) {
                types.put(helperOf(m), packages);
                // Should we also ignore methods on base assembly class????
                onMethod(m);// move this to step 2???
            }
        }

        // Step 2 process all declared methods

        // Maybe some kind of detection if current type (c) switches modules.
        for (Class<?> c = classToScan; c != baseType && c.getModule() != JAVA_BASE_MODULE; c = c.getSuperclass()) {
            // First process every field
            if (reflectOnFields) {
                for (Field field : c.getDeclaredFields()) {
                    onField(field);
                }
            }
            for (Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (Modifier.isStatic(mod)) {
                    if (c == classToScan && !Modifier.isPublic(mod)) { // we have already processed public static methods
                        // only include static methods in the top level class
                        // We do this, because it would be strange to include
                        // static methods on any interfaces this class implements.
                        // But it would also be strange to include static methods on sub classes
                        // but not include static methods on interfaces.
                        onMethod(m);
                    }
                } else if (!m.isBridge()) { // TODO should we include synthetic methods??
                    switch (mod & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) {
                    case Modifier.PUBLIC:
                        continue; // we have already added the method in the first step
                    default: // default access
                        HashSet<Package> pkg = types.computeIfAbsent(helperOf(m), key -> new HashSet<>());
                        if (pkg != packages && pkg.add(c.getPackage())) {
                            break;
                        } else {
                            continue;
                        }
                    case Modifier.PROTECTED:
                        if (types.putIfAbsent(helperOf(m), packages) != null) {
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

    /**
     * @param clazz
     *            the class to scan
     * @param allowInjectAnnotation
     *            whether or not we allow usage of {@link Inject}. If not, the specified class must have a single
     *            constructor
     * @param errorMaker
     *            invoked with an error message if something goes wrong
     * @return the constructor
     */
    // Taenker vi skal have en exception der specifikt naevner noget med constructor
    // NoConstructorExtension
    // InjectableConstructorMissingException
    // MissingInjectableConstructorException
    // ConstructorInjectionException (lyder mere som noget vi ville smide naar vi instantiere det
    public static Constructor<?> getConstructor(Class<?> clazz, boolean allowInjectAnnotation, Function<String, RuntimeException> errorMaker) {
        if (clazz.isAnnotation()) { // must be checked before isInterface
            String errorMsg = format(clazz) + " is an annotation and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isInterface()) {
            String errorMsg = format(clazz) + " is an interface and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isArray()) {
            String errorMsg = format(clazz) + " is an array and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (clazz.isPrimitive()) {
            String errorMsg = format(clazz) + " is a primitive class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        } else if (Modifier.isAbstract(clazz.getModifiers())) {
            String errorMsg = format(clazz) + " is an abstract class and cannot be instantiated";
            throw errorMaker.apply(errorMsg);
        }

        // Get all declared constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return constructors[0];
        } else if (!allowInjectAnnotation) {
            StringBuilder sb = new StringBuilder();
            sb.append(clazz).append(" must declare exactly 1 constructor, [constructors = ");
            StringJoiner sj = new StringJoiner(", ");
            List.of(constructors).forEach(c -> sj.add(StringFormatter.formatShortParameters(c)));
            sb.append(sj).append("]");
            throw errorMaker.apply(sb.toString());
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        for (Constructor<?> c : constructors) {
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    String errorMsg = "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(clazz);
                    throw errorMaker.apply(errorMsg);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single public constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isPublic(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "public", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // See if we have a single protected constructor
        for (Constructor<?> c : constructors) {
            if (Modifier.isProtected(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "protected", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Remaining constructors are either private or package private constructors
        for (Constructor<?> c : constructors) {
            if (!Modifier.isPrivate(c.getModifiers())) {
                if (constructor != null) {
                    throw getErrMsg(clazz, "package-private", errorMaker);
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return constructor;
        }

        // Only private constructors left, and we have already checked whether or not we only have a single method
        // So we must have more than 1 private methods
        throw getErrMsg(clazz, "private", errorMaker);
    }

    private static RuntimeException getErrMsg(Class<?> type, String visibility, Function<String, RuntimeException> errorMaker) {
        String errorMsg = "No constructor annotated with @" + Inject.class.getSimpleName() + ". And multiple " + visibility + " constructors on class "
                + format(type);
        return errorMaker.apply(errorMsg);
    }

    // Eclipse fails with these as addition constructors
    static Helper helperOf(Method method) {
        return helperOf(method.getName(), method.getParameterTypes());
    }

    // Eclipse fails with these as addition constructors
    static Helper helperOf(String name, Class<?>[] parameterTypes) {
        return new Helper(name.hashCode() ^ Arrays.hashCode(parameterTypes), name, parameterTypes);
    }

    /** Processes all fields and methods on a class. */
    private record Helper(int hash, String name, Class<?>[] parameterTypes) {

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
