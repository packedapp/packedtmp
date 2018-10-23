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
}
