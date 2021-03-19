package packed.internal.util;

import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/** Various utility methods for working {@link Class classes}. */
public class ClassUtil {

    /**
     * Checks that the specified class can be instantiated. That is, a public non-abstract class with at least one public
     * constructor.
     *
     * @param clazz
     *            the class to check
     */
    // TODO tror godt vi kan fjerne denne, eftersom den er flyttet til FindInjectableConstructod...
    // Tror ikke vi finder constructere som vi ikke bruger
    public static <T> Class<T> checkIsInstantiable(Class<T> clazz) {
        if (clazz.isAnnotation()) {
            throw new IllegalArgumentException("The specified class (" + format(clazz) + ") is an annotation and cannot be instantiated");
        } else if (clazz.isInterface()) {
            throw new IllegalArgumentException("The specified class (" + format(clazz) + ") is an interface and cannot be instantiated");
        } else if (clazz.isArray()) {
            throw new IllegalArgumentException("The specified class (" + format(clazz) + ") is an array and cannot be instantiated");
        } else if (clazz.isPrimitive()) {
            throw new IllegalArgumentException("The specified class (" + format(clazz) + ") is a primitive class and cannot be instantiated");
        }
        int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers)) {
            // Yes a primitive class is abstract
            throw new IllegalArgumentException("The specified class (" + format(clazz) + ") is an abstract class and cannot be instantiated");
        }
        /*
         * else if (!Modifier.isPublic(modifiers)) { throw new IllegalArgumentException("The specified class (" + format(clazz)
         * + ") is not a public class and cannot be instantiated"); } if (clazz.getConstructors().length == 0) { throw new
         * IllegalArgumentException("The specified class (" + format(clazz) +
         * ") does not have any public constructors and cannot be instantiated"); }
         */
        return clazz;
    }

    /**
     * Converts the specified primitive wrapper class to the corresponding primitive class. Or returns the specified class
     * if it is not a primitive wrapper class.
     * 
     * @param <T>
     *            the type to unbox
     * @param type
     *            the class to convert
     * @return the converted class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> unwrap(Class<T> type) {
        if (type == Boolean.class) {
            return (Class<T>) boolean.class;
        } else if (type == Byte.class) {
            return (Class<T>) byte.class;
        } else if (type == Character.class) {
            return (Class<T>) char.class;
        } else if (type == Double.class) {
            return (Class<T>) double.class;
        } else if (type == Float.class) {
            return (Class<T>) float.class;
        } else if (type == Integer.class) {
            return (Class<T>) int.class;
        } else if (type == Long.class) {
            return (Class<T>) long.class;
        } else if (type == Short.class) {
            return (Class<T>) short.class;
        } else if (type == Void.class) {
            return (Class<T>) void.class;
        }
        return type;
    }

    /**
     * Tests if the specified class is an inner class.
     * 
     * @param clazz
     *            the class to test
     * @return whether or not the specified class is an inner class
     */
    public static boolean isInnerOrLocal(Class<?> clazz) {
        return clazz.isLocalClass() || (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()));
    }

    /**
     * Tests if the class is an optional type.
     * 
     * @param type
     *            the type to test
     * @return whether or not the specified is an optional type
     * @see Optional
     * @see OptionalLong
     * @see OptionalDouble
     * @see OptionalInt
     */
    public static boolean isOptional(Class<?> type) {
        return (type == Optional.class || type == OptionalLong.class || type == OptionalInt.class || type == OptionalDouble.class);
    }

    /**
     * Converts the specified primitive class to the corresponding Object based class. Or returns the specified class if it
     * is not a primitive class.
     *
     * @param <T>
     *            the type to box
     * @param type
     *            the class to convert
     * @return the converted class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> wrap(Class<T> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) {
                return (Class<T>) Boolean.class;
            } else if (type == byte.class) {
                return (Class<T>) Byte.class;
            } else if (type == char.class) {
                return (Class<T>) Character.class;
            } else if (type == double.class) {
                return (Class<T>) Double.class;
            } else if (type == float.class) {
                return (Class<T>) Float.class;
            } else if (type == int.class) {
                return (Class<T>) Integer.class;
            } else if (type == long.class) {
                return (Class<T>) Long.class;
            } else if (type == short.class) {
                return (Class<T>) Short.class;
            } else { /* if (type == void.class) */
                return (Class<T>) Void.class;
            }
        }
        return type;
    }
}
