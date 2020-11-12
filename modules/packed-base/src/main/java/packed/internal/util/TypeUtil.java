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

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

/** Various utility methods for working {@link Type types}. */
public final class TypeUtil {

    /** Cannot instantiate. */
    private TypeUtil() {}

    /**
     * Checks that the specified class can be instantiated. That is, a public non-abstract class with at least one public
     * constructor.
     *
     * @param clazz
     *            the class to check
     */
    public static <T> Class<T> checkClassIsInstantiable(Class<T> clazz) {
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
     * Returns a set of all type variable names that occurs in the specified type
     * 
     * @param type
     *            the type to check
     * @return a set of all type variable names that occurs in the specified type
     * @see TypeVariable#getName()
     */
    public static Set<String> findTypeVariableNames(Type type) {
        requireNonNull(type, "type is null");
        LinkedHashSet<String> addTo = new LinkedHashSet<>();
        findTypeVariableNames0(addTo, type);
        return addTo;
    }

    /**
     * Helper method for {@link #findTypeVariableNames(Type)}.
     * 
     * @param addTo
     *            the set to add each variable to
     * @param type
     *            the type to analyse
     */
    private static void findTypeVariableNames0(LinkedHashSet<String> addTo, Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            findTypeVariableNames0(addTo, pt.getOwnerType());
            for (Type t : pt.getActualTypeArguments()) {
                findTypeVariableNames0(addTo, t);
            }
            findTypeVariableNames0(addTo, pt.getRawType());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            findTypeVariableNames0(addTo, gat.getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            addTo.add(((TypeVariable<?>) type).getName());
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            if (wt.getLowerBounds().length > 0) {
                findTypeVariableNames0(addTo, wt.getLowerBounds()[0]);
            }
            findTypeVariableNames0(addTo, wt.getUpperBounds()[0]);
        }
    }

    /**
     * Returns true if the specified {@code type} is free from type variables, otherwise false. For example,
     * {@code List<Map<String, ? extends Integer>} is free from type variables. {@code List<Map<String, ? extends T>} is
     * not.
     * 
     * @param type
     *            the type to check
     * @return true is the specified type is free from type variable, otherwise false
     */
    public static boolean isFreeFromTypeVariables(Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return true;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            if (pt.getOwnerType() != null && !isFreeFromTypeVariables(pt.getOwnerType())) {
                return false;
            }
            for (Type t : pt.getActualTypeArguments()) {
                if (!isFreeFromTypeVariables(t)) {
                    return false;
                }
            }
            // To be safe we check the raw type as well, I expect it should always be a class, but the method signature says
            // something else
            return isFreeFromTypeVariables(pt.getRawType());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            return isFreeFromTypeVariables(gat.getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            return false;
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            for (Type t : wt.getLowerBounds()) {
                if (!isFreeFromTypeVariables(t)) {
                    return false;
                }
            }
            for (Type t : wt.getUpperBounds()) {
                if (!isFreeFromTypeVariables(t)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * Tests if the specified class is an inner class.
     * 
     * @param clazz
     *            the class to test
     * @return whether or not the specified class is an inner class
     */
    public static boolean isInnerOrLocalClass(Class<?> clazz) {
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
     * Finds the raw class type for the specified type
     *
     * @param type
     *            the type to find the raw class from
     * @return the raw type
     * @throws IllegalArgumentException
     *             if the raw type could not be found
     */
    public static Class<?> rawTypeOf(Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(rawTypeOf(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of type: " + type.getClass().getName());
        }
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
