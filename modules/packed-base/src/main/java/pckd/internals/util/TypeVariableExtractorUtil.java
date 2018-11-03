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
import static pckd.internals.util.Formatter.format;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.StringJoiner;

/**
 *
 */
public class TypeVariableExtractorUtil {

    public static <T> Type findTypeParameterUnsafe(Class<? extends T> childClass, Class<T> baseClass, int typeVariableIndexOnBaseClass) {
        requireNonNull(baseClass, "baseClass is null");
        requireNonNull(childClass, "childClass is null");
        if (baseClass == childClass) {
            throw new IllegalArgumentException("baseClass and chilClass cannot be the same class, ");
        }

        TypeVariable<Class<T>>[] typeParameters = baseClass.getTypeParameters();
        if (typeParameters.length == 0) {
            throw new IllegalArgumentException("baseClass '" + format(baseClass) + "' does not define any type parameters");
        } else if (typeVariableIndexOnBaseClass < 0) {
            throw new IllegalArgumentException("typeVariableIndexOnBaseClass cannot be negative, was " + typeVariableIndexOnBaseClass);
        } else if (typeVariableIndexOnBaseClass >= typeParameters.length) {
            throw new IllegalArgumentException(
                    "baseClass '" + format(baseClass) + "' only defines " + typeParameters.length + " type parameters, but typeVariableIndexOnBaseClass was "
                            + typeVariableIndexOnBaseClass + ". Must be 0 <= typeVariableIndexOnBaseClass" + " <= " + (typeParameters.length - 1));
        }
        if (!baseClass.isAssignableFrom(childClass)) {
            throw new IllegalArgumentException("childClass " + childClass + " is not assignable to baseClass " + baseClass);
        }

        if (childClass.isInterface()) {
            throw new UnsupportedOperationException("Child class cannot be an interface, class = " + format(baseClass));
        }
        if (baseClass.isInterface()) {
            throw new UnsupportedOperationException("Base classes that are interfaces are currently not supported, class = " + format(baseClass));
        }

        return findTypeParameterFromSuperClass(childClass, baseClass, typeVariableIndexOnBaseClass);
    }

    // This class should be called from places where baseClass is a guaranteed super class of child class, and type variable
    // index is guaranteed valid.
    public static <T> Type findTypeParameterFromSuperClass(Class<? extends T> childClass, Class<T> baseClass, int typeVariableIndexOnBaseClass) {
        // This method works by first recursively calling all the way down to the first class that extends baseClass.
        // And then we keep going finding out which of the actual type parameters matches the super classes type parameters
        if (baseClass == childClass.getSuperclass()) {
            return findTypeParameterFromSuperClass0(baseClass, childClass, typeVariableIndexOnBaseClass);
        }
        @SuppressWarnings("unchecked")
        Type pp = findTypeParameterFromSuperClass((Class<? extends T>) childClass.getSuperclass(), baseClass, typeVariableIndexOnBaseClass);
        if (pp instanceof TypeVariable) {
            TypeVariable<?>[] tvs = childClass.getSuperclass().getTypeParameters();
            for (int i = 0; i < tvs.length; i++) {
                if (tvs[i].equals(pp)) {
                    return findTypeParameterFromSuperClass0(baseClass, childClass, i);
                }
            }
        }
        return pp;
    }

    /**
     * A helper method for {@link #findTypeParameterFromSuperClass(Class, Class, int)}.
     * 
     * @param baseClass
     *            the child class
     * @param superClass
     *            the super class
     * @param index
     *            the index of type parameter in superClass
     * @return the resolved type parameter
     */
    private static Type findTypeParameterFromSuperClass0(Class<?> baseClass, Class<?> superClass, int index) {
        Type t = superClass.getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            String name = superClass.getSuperclass().getTypeParameters()[index].getName();
            StringJoiner sj = new StringJoiner(", ", baseClass.getSimpleName() + "<", ">");
            for (Type ty : baseClass.getTypeParameters()) {
                sj.add(TypeUtil.toShortString(ty));
            }
            throw new IllegalArgumentException("Cannot determine type variable <" + name + "> for " + sj.toString() + " on class " + format(superClass));
        }
        ParameterizedType pt = (ParameterizedType) t;
        return pt.getActualTypeArguments()[index];
    }
}
