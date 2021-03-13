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
package packed.internal.invoke.typevariable;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.IntStream;

import packed.internal.util.StringFormatter;

/**
 * A small utility class that allows easy extraction of type variables.
 */
//// Naeh hov.. det er bare ting der ogsaa skal bruge type converters...
// Field
// Method Return type + Method Annotations
// Parameter
// Constructor

// Vi har kun 2 metoder...
// Fordi vi ogsaa gerne vil have dem paa FieldDescriptor og friends..
// Proever vi at begraense os

// extractAndConvert
public final class TypeVariableExtractor {

    /** The base type where the type variables are located. */
    private final Class<?> baseType;

    /** The index that should be resolved. */
    private final int[] indexes;

    /** Whether or not the base type is an interface. */
    final boolean isInterface;

    /** The type variables, matchings the indexes. */
    final TypeVariable<?>[] typeVariables;

    TypeVariableExtractor(Class<?> baseType, TypeVariable<?>[] typeVariables, int[] indexes) {
        this.baseType = requireNonNull(baseType);
        this.indexes = requireNonNull(indexes);
        this.typeVariables = requireNonNull(typeVariables);
        this.isInterface = Modifier.isInterface(baseType.getModifiers());
    }

    public Type extract(Class<?> from) {
        if (indexes.length != 1) {
            throw new UnsupportedOperationException("This method can only be used when the extractor was created with a single index, baseType = "
                    + StringFormatter.format(baseType) + ", indexes = " + Arrays.toString(indexes));
        }
        return extractAll(from)[0];
    }

    public <T> T extract(Class<?> from, TypeConverter<T> converter) {
        requireNonNull(from, "from is null");
        if (indexes.length != 1) {
            throw new UnsupportedOperationException(
                    "This method can only be used when the extractor was created with a single index, indexes = " + Arrays.toString(indexes));
        }
        throw new UnsupportedOperationException();
    }

    public Type[] extractAll(Class<?> from) {
        if (!baseType.isAssignableFrom(from)) {
            String op = Modifier.isInterface(from.getModifiers()) == isInterface ? "extend" : "implement";
            throw new IllegalArgumentException(StringFormatter.format(from) + " does not " + op + " " + StringFormatter.format(baseType));
        }
        Type[] result = new Type[indexes.length];
        if (isInterface) {
            fromInterface0(from, result);
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = findTypeParameterFromSuperClass(from, indexes[i]);
            }
        }
        return result;
    }

    // number of converters must match number of indexes...
    public Object[] extractAll(Class<?> from, TypeConverter<?>... converters) {
        requireNonNull(from, "from is null");
        // Type[] result = new Type[indexes.length];
        throw new UnsupportedOperationException();
    }

    public <T> Type findTypeParameterFromSuperClass(Class<? extends T> childClass, int typeVariableIndexOnBaseClass) {
        // This method works by first recursively calling all the way down to the first class that extends baseClass.
        // And then we keep going finding out which of the actual type parameters matches the super classes type parameters

        if (baseType == childClass.getSuperclass()) {
            return findTypeParameterFromSuperClass0(childClass, typeVariableIndexOnBaseClass);
        }
        @SuppressWarnings("unchecked")
        Type pp = findTypeParameterFromSuperClass((Class<? extends T>) childClass.getSuperclass(), typeVariableIndexOnBaseClass);
        if (pp instanceof TypeVariable) {
            TypeVariable<?>[] tvs = childClass.getSuperclass().getTypeParameters();
            for (int i = 0; i < tvs.length; i++) {
                if (tvs[i].equals(pp)) {
                    return findTypeParameterFromSuperClass0(childClass, i);
                }
            }
        }
        return pp;
    }

    /**
     * 
     * @param superClass
     *            the super class
     * @param index
     *            the index of type parameter in superClass
     * @return the resolved type parameter
     */
    private Type findTypeParameterFromSuperClass0(Class<?> superClass, int index) {
        Type t = superClass.getGenericSuperclass();
        if (!(t instanceof ParameterizedType)) {
            String name = superClass.getSuperclass().getTypeParameters()[index].getName();
            StringJoiner sj = new StringJoiner(", ", baseType.getSimpleName() + "<", ">");
            for (Type ty : baseType.getTypeParameters()) {
                sj.add(formatSimple(ty));
            }
            // TODO this is not for Factory0
            throw new IllegalArgumentException("Cannot determine type variable <" + name + "> for " + sj.toString() + " on class " + format(superClass));
        }
        ParameterizedType pt = (ParameterizedType) t;
        return pt.getActualTypeArguments()[index];
    }

    boolean fromInterface0(Class<?> cc, Type[] result) {
        for (Type t : cc.getGenericInterfaces()) {
            if (t instanceof ParameterizedType pt) {
                if (pt.getRawType() == baseType) {
                    Type[] typeArguments = pt.getActualTypeArguments();
                    for (int i = 0; i < result.length; i++) {
                        result[i] = typeArguments[i];
                    }
                    return true;
                }

                for (Type tt : pt.getActualTypeArguments()) {
                    System.out.println(tt);
                    // Ahh fuck skal lave noget ledt her ogsaa....
                }
            } else if (t instanceof Class<?> cl) {
                if (fromInterface0(cl, result)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the number of type variables this extractor extracts.
     * 
     * @return the number of type variables this extractor extracts
     */
    public int size() {
        return indexes.length;
    }

    // If no indexes specified, choose all..
    public static <T> TypeVariableExtractor of(Class<?> baseType, int... indexes) {
        requireNonNull(baseType, "baseType is null");
        requireNonNull(indexes, "indexes is null");
        TypeVariable<?>[] typeVariables = baseType.getTypeParameters();
        if (typeVariables.length == 0) {
            throw new IllegalArgumentException("baseClass '" + StringFormatter.format(baseType) + "' does not define any type parameters");
        }
        if (indexes.length == 0) {
            return new TypeVariableExtractor(baseType, typeVariables, IntStream.range(0, typeVariables.length).toArray());
        } else {
            for (int i = 0; i < indexes.length; i++) {
                if (indexes[i] < 0 || indexes[i] >= typeVariables.length) {
                    throw new IllegalArgumentException("Cannot specify non existing index, index = " + indexes[i]);
                }
            }
            return new TypeVariableExtractor(baseType, typeVariables, indexes);
        }
    }
}

// public static <T> TypeVariableExtractor<T> of(Class<?> baseClass, TypeConverter<T> converter) {
// return of(baseClass, converter, 0);
// }
//
// public static <T> TypeVariableExtractor<T> of(Class<?> baseClass, TypeConverter<T> converter, int index) {
// throw new UnsupportedOperationException();
// }
//
// public static <T> TypeVariableExtractor<List<Object>> ofMany(Class<?> baseClass, TypeConverter<?>... converters) {
// requireNonNull(converters, "converters is null");
// return ofMany(baseClass, converters, IntStream.range(0, converters.length).toArray());
// }
//
// public static <T> TypeVariableExtractor<List<Object>> ofMany(Class<?> baseClass, TypeConverter<?>[] converters,
// int... indexes) {
// throw new UnsupportedOperationException();
// }