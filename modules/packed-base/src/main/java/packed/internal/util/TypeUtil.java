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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.LinkedHashSet;
import java.util.Set;

/** Various utility methods for working {@link Type types}. */
public final class TypeUtil {

    /** Cannot instantiate. */
    private TypeUtil() {}

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
        } else if (type instanceof ParameterizedType pt) {
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
        } else if (type instanceof GenericArrayType gat) {
            return isFreeFromTypeVariables(gat.getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            return false;
        } else if (type instanceof WildcardType wt) {
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
        if (type instanceof Class<?> cl) {
            return cl;
        } else if (type instanceof ParameterizedType pt) {
            return (Class<?>) pt.getRawType();
        } else if (type instanceof GenericArrayType gat) {
            return Array.newInstance(rawTypeOf(gat.getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            return Object.class;
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of unknown type: " + type.getClass().getName());
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
    public static Set<String> typeVariableNamesOf(Type type) {
        requireNonNull(type, "type is null");
        LinkedHashSet<String> addTo = new LinkedHashSet<>();
        typeVariableNamesOf0(addTo, type);
        return addTo;
    }


    /**
     * Helper method for {@link #typeVariableNamesOf(Type)}.
     * 
     * @param addTo
     *            the set to add each variable to
     * @param type
     *            the type to analyse
     */
    private static void typeVariableNamesOf0(LinkedHashSet<String> addTo, Type type) {
        if (type instanceof ParameterizedType pt) {
            typeVariableNamesOf0(addTo, pt.getOwnerType());
            for (Type t : pt.getActualTypeArguments()) {
                typeVariableNamesOf0(addTo, t);
            }
            typeVariableNamesOf0(addTo, pt.getRawType());
        } else if (type instanceof GenericArrayType gat) {
            typeVariableNamesOf0(addTo, gat.getGenericComponentType());
        } else if (type instanceof TypeVariable<?> tv) {
            addTo.add(tv.getName());
        } else if (type instanceof WildcardType wt) {
            if (wt.getLowerBounds().length > 0) {
                typeVariableNamesOf0(addTo, wt.getLowerBounds()[0]);
            }
            typeVariableNamesOf0(addTo, wt.getUpperBounds()[0]);
        }
    }
}
