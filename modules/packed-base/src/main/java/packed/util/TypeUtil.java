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
package packed.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 *
 */
public class TypeUtil {

    /** Cannot instantiate. */
    private TypeUtil() {}

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
            return Object.class;// Probably the best we can do, maybe just fail??
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of type: " + type.getClass().getName());
        }
    }

    public static String toShortString(Type type) {
        StringBuilder sb = new StringBuilder();
        toShortString(type, sb);
        return sb.toString();
    }

    private static void toShortString(Type type, StringBuilder sb) {
        if (type instanceof Class<?>) {
            sb.append(((Class<?>) type).getSimpleName());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            sb.append(toShortString(pt.getRawType()));
            Type[] actualTypeArguments = pt.getActualTypeArguments();
            if (actualTypeArguments.length > 0) {
                sb.append("<");
                toShortString(actualTypeArguments[0], sb);
                for (int i = 1; i < actualTypeArguments.length; i++) {
                    sb.append(", ");
                    toShortString(actualTypeArguments[i], sb);
                }
                sb.append(">");
            }
        } else if (type instanceof GenericArrayType) {
            toShortString(((GenericArrayType) type).getGenericComponentType(), sb);
            sb.append("[]");
        } else if (type instanceof TypeVariable) {
            // Hmm
            throw new UnsupportedOperationException();
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] lowerBounds = wt.getLowerBounds();
            if (lowerBounds.length == 1) {
                sb.append("? super ");
                toShortString(lowerBounds[0], sb);
            } else {
                Type[] upperBounds = wt.getUpperBounds();
                if (upperBounds[0] == Object.class) {
                    sb.append("?");
                } else {
                    sb.append("? extends ");
                    toShortString(upperBounds[0], sb);
                }
            }
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of type: " + type.getClass().getName());
        }
    }
}
