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
package pckd.internals.util;

import static java.util.Objects.requireNonNull;

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
    public static <T> Class<T> boxClass(Class<T> type) {
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

    /**
     * Finds the raw class type for the specified type
     *
     * @param type
     *            the type to find the raw class from
     * @return the raw type
     * @throws IllegalArgumentException
     *             if the raw type could not be found
     */
    public static Class<?> findRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(findRawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of type: " + type.getClass().getName());
        }
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
     *            the type to analyze
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
            if (wt.getLowerBounds().length > 0 && !isFreeFromTypeVariables(wt.getLowerBounds()[0])) {
                return false;
            }
            return isFreeFromTypeVariables(wt.getUpperBounds()[0]);// upperBound always defines, as a minimum Object
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
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
        return (type == Optional.class || type == OptionalLong.class || type == OptionalDouble.class || type == OptionalInt.class);
    }

    /**
     * Creates a short string representation of the specified type. Basically this method uses {@link Class#getSimpleName()}
     * instead of {@link Class#getCanonicalName()}. Which results in short string such as {@code List<String>} instead of
     * {@code java.util.List<java.lang.String>}.
     * 
     * @param type
     *            the type to create a short representation of
     * @return the representation
     */
    public static String toShortString(Type type) {
        StringBuilder sb = new StringBuilder();
        toShortString0(type, sb);
        return sb.toString();
    }

    /**
     * Helper method for {@link #toShortString(Type)}.
     * 
     * @param type
     *            the type to process
     * @param sb
     *            the string builder
     */
    private static void toShortString0(Type type, StringBuilder sb) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            sb.append(((Class<?>) type).getSimpleName());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            toShortString0(pt.getRawType(), sb);
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            // The array can be empty according to #ParameterizedType.getActualTypeArguments()
            if (actualTypeArguments.length > 0) {
                sb.append("<");
                toShortString0(actualTypeArguments[0], sb);
                for (int i = 1; i < actualTypeArguments.length; i++) {
                    sb.append(", ");
                    toShortString0(actualTypeArguments[i], sb);
                }
                sb.append(">");
            }
        } else if (type instanceof GenericArrayType) {
            toShortString0(((GenericArrayType) type).getGenericComponentType(), sb);
            sb.append("[]");
        } else if (type instanceof TypeVariable) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            sb.append(tv.getName());
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length == 1) {
                sb.append("? super ");
                toShortString0(lowerBounds[0], sb);
            } else {
                Type[] upperBounds = wt.getUpperBounds();
                if (upperBounds[0] == Object.class) {
                    sb.append("?");
                } else {
                    sb.append("? extends ");
                    toShortString0(upperBounds[0], sb);
                }
            }
        } else {
            throw new IllegalArgumentException("Don't know how to process type '" + type + "' of type: " + type.getClass().getName());
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
    public static <T> Class<T> unboxClass(Class<T> type) {
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
}
