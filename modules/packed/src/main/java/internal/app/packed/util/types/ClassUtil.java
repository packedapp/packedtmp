package internal.app.packed.util.types;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.lifecycle.sandbox.errorhandling.ErrorProcessor;
import internal.app.packed.util.StringFormatter;

/** Various utility methods for working {@link Class classes}. */
public class ClassUtil {

    @SuppressWarnings("unchecked")
    public static <T> Class<T> checkProperSubclass(Class<T> expectedSuperClass, Class<?> clazzToCheck, String paramName) {
        requireNonNull(clazzToCheck, paramName + " is null");
        if (clazzToCheck == expectedSuperClass) {
            throw new IllegalArgumentException(expectedSuperClass.getSimpleName() + ".class is not a valid argument to this method.");
        } else if (!expectedSuperClass.isAssignableFrom(clazzToCheck)) {
            throw new IllegalArgumentException(
                    "The specified type '" + StringFormatter.format(clazzToCheck) + "' did not extend '" + StringFormatter.format(expectedSuperClass) + "'");
        }
        return (Class<T>) clazzToCheck;
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable, T> Class<T> checkProperSubclass(Class<T> clazz, Class<?> clazzToCheck, ErrorProcessor<E> f) throws E {
        requireNonNull(clazzToCheck, "class is null");
        if (clazzToCheck == clazz) {
            throw f.onError(clazz.getSimpleName() + ".class is not a valid argument to this method.");
        } else if (!clazz.isAssignableFrom(clazzToCheck)) {
            throw f.onError("The specified type '" + StringFormatter.format(clazz) + "' must extend '" + StringFormatter.format(clazzToCheck) + "'");
        }
        return (Class<T>) clazzToCheck;
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
    public static <T> Class<T> unbox(Class<T> type) {
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
    public static boolean isOptionalType(Class<?> type) {
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
    public static <T> Class<T> box(Class<T> type) {
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
