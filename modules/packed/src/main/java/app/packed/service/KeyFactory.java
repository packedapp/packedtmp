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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

import internal.app.packed.util.ClassUtil;

/**
 *
 */
// 3 maader at lave keys paa

// Extend Key
// of();

// Checks
//// No TypeVariable
//// Reduce wildcards
//// no Void key, Optional, ..., keys (Can be used in combined though)

class KeyFactory {

    static Type convert(Type type) {
        if (type instanceof Class<?> c) {
            if (c.isPrimitive()) {
                return ClassUtil.wrap(c);
            } else {
                return c;
            }
        }
        return null;
    }

    // Problemet er saa TypeVariableExtractor som ikke smider InvalidKeyException
    // Saa aaah, det er i sidste ende flere dags arbejde, der maaske skal refactoreres igen.
    
    
    public static Type convertx(Object source, Type originalType, Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return type;
        } else if (type instanceof ParameterizedType pt) {
            throw new UnsupportedOperationException();
//            pt.get
//            if (pt.getOwnerType() != null && !isFreeFromTypeVariables(pt.getOwnerType())) {
//                return false;
//            }
//            for (Type t : pt.getActualTypeArguments()) {
//                if (!isFreeFromTypeVariables(t)) {
//                    return false;
//                }
//            }
//            // To be safe we check the raw type as well, I expect it should always be a class, but the method signature says
//            // something else
//            return isFreeFromTypeVariables(pt.getRawType());
        } else if (type instanceof GenericArrayType gat) {
            return gat;
        } else if (type instanceof TypeVariable) {
            throw new InvalidKeyException("opps");
        } else if (type instanceof WildcardType wt) {
            Type t = wt.getLowerBounds()[0];
            if (t == null) {
                t = wt.getUpperBounds()[0];
                if (t == null) {
                    throw new InvalidKeyException("opps");
                }
            }
            return convert(t);
        } else {
            throw new InvalidKeyException("Unknown type: " + type);
        }
    }
}
