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

import static packed.util.Formatter.format;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

import app.packed.inject.TypeLiteral;
import packed.inject.InjectAPI;

/**
 *
 */
// Based on this post http://www.artima.com/weblogs/viewpost.jsp?thread=208860
public final class GenericsUtil {

    /** Cannot instantiate. */
    private GenericsUtil() {}
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Type getTypeOfArgumentX(Class<?> superClass, int parameterIndex, Class<? > subClass) {
        Type t = GenericsUtil.getTypeOfArgument((Class) superClass, subClass, parameterIndex);
        if (t instanceof TypeVariable) {
            Class<?> component = subClass.getSuperclass();
            StringBuilder sb = new StringBuilder();
            TypeVariable<?>[] typeparms = component.getTypeParameters();
            if (typeparms.length > 0) {
                StringJoiner sj = new StringJoiner(",", "<", ">");
                for (TypeVariable<?> typeparm : typeparms) {
                    sj.add(typeparm.getTypeName());
                }
                sb.append(sj.toString());
            }
            throw new IllegalArgumentException(
                    "Could not determine the type variable <" + t + "> of " + component.getSimpleName() + sb + " for " + format(subClass));
        }
        return t;
    }
    

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> getTypeOfArgument(Class<T> superClass, int parameterIndex, Class<? extends T> subClass) {
        Type t = GenericsUtil.getTypeOfArgument(superClass, subClass, parameterIndex);
        if (t instanceof TypeVariable) {
            Class<?> component = subClass.getSuperclass();
            StringBuilder sb = new StringBuilder();
            TypeVariable<?>[] typeparms = component.getTypeParameters();
            if (typeparms.length > 0) {
                StringJoiner sj = new StringJoiner(",", "<", ">");
                for (TypeVariable<?> typeparm : typeparms) {
                    sj.add(typeparm.getTypeName());
                }
                sb.append(sj.toString());
            }
            throw new IllegalArgumentException(
                    "Could not determine the type variable <" + t + "> of " + component.getSimpleName() + sb + " for " + format(subClass));
        }
        return (TypeLiteral<T>) InjectAPI.toTypeLiteral(t);
    }
    


    static <T> List<Type> getTypeOfArguments(Class<T> baseClass, Class<? extends T> child) {
        if (child.isInterface()) {
            throw new IllegalArgumentException();
        }
        HashMap<Type, Type> resolvedTypes = new HashMap<>();
        Type type = child;
        Class<?> class1 = TypeUtil.findRawType(type);
        if (class1 == null) {
            return null;
        }
        while (class1 != null && !class1.equals(baseClass)) {
            if (type instanceof Class) {
                // Cannot use a raw type for anything, just move on to the super class
                type = ((Class<?>) type).getGenericSuperclass();
            } else {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }
                if (!rawType.equals(baseClass)) {
                    type = rawType.getGenericSuperclass();
                }
            }
            class1 = TypeUtil.findRawType(type);
        }

        // finally, for each actual type argument provided to baseClass, determine (if possible)
        // the raw class for that type argument.
        Type[] actualTypeArguments = type instanceof Class ? ((Class<?>) type).getTypeParameters() : ((ParameterizedType) type).getActualTypeArguments();

        List<Type> typeArgumentsAsClasses = new ArrayList<>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(baseType /*canonicalize(baseType)*/);
        }
        return typeArgumentsAsClasses;
    }

    // A hack to find the interface a serviceProvider provides, above code throws NPE
    public static Class<?> getServiceProviderInterfaceHack(Class<?> interfaceToFind, Class<?> from, int parameterIndex) {
        Class<?> cl = from;
        while (interfaceToFind.isAssignableFrom(cl.getSuperclass())) {
            cl = cl.getSuperclass();
        }
        for (Type genericInterface : cl.getGenericInterfaces()) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                if (TypeUtil.findRawType(pt.getRawType()) == interfaceToFind) {
                    Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                    return TypeUtil.findRawType(genericTypes[parameterIndex]);
                }
            }
        }

        return cl;
    }

    public static <T> Type getTypeOfArgument(Class<T> baseClass, Class<? extends T> childClass, int parameterIndex) {
        if (baseClass.isInterface()) {
            throw new UnsupportedOperationException("Base classes that are interfaces are currently not supported, class = " + format(baseClass));
        }
        List<Type> l = getTypeOfArguments(baseClass, childClass);
        if (l == null || l.size() < parameterIndex) {
            return null;
        }
        return l.get(parameterIndex);
    }
}
