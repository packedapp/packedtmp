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
package internal.app.packed.util.types;

import static internal.app.packed.util.StringFormatter.format;
import static internal.app.packed.util.StringFormatter.formatSimple;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import app.packed.binding.Variable;
import app.packed.lifecycle.runtime.errorhandling.ErrorProcessor;
import app.packed.operation.Op0;
import app.packed.operation.Op1;
import app.packed.util.Nullable;
import internal.app.packed.binding.PackedVariable;
import internal.app.packed.util.StringFormatter;

/**
 * A small utility class that allows easy extraction of type variables.
 *
 * @see ExtensionMirror
 * @see ExtensionPoint
 * @see Key
 * @see GenericType
 * @see Op
 */

// Tror vi skal smide en exception

// Tror ikke vi skal tillade at returne TypeVariable
// Kan ikke se det er brugbart

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

    public <T extends Throwable> Variable[] extractAllVariables(Class<?> from, ErrorProcessor<T> ep) throws T {
        Type[] types = extractAllTypes(from, ep);
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) from.getAnnotatedSuperclass();

        Variable[] variables = new Variable[types.length];
        for (int i = 0; i < variables.length; i++) {
            AnnotatedType at = pta.getAnnotatedActualTypeArguments()[i];
            variables[i] = PackedVariable.of(at.getType(), at.getAnnotations());
        }
        return variables;
    }

    public static void main(String[] args) {
        Op1<@Nullable String, Long> o = new Op1<@Nullable String, Long>(_ -> 12L) {};

        TypeVariableExtractor tve = TypeVariableExtractor.of(Op1.class);

    //    Op0<Integer> ox = new Intermediate<String, Integer, Long>(() -> 1) {};

//        TypeVariableExtractor tvex = TypeVariableExtractor.of(CapturingOp);
//        tvex.extractAllTypes(, null)

        IO.println(tve.extractAllVariables(o.getClass(), Error::new)[0]);
        IO.println(tve.extractAllVariables(o.getClass(), Error::new)[1]);
    }

    /** Check that we can have an intermediate abstract class. */
    static abstract class Intermediate<S, T, R> extends Op0<T> {
        protected Intermediate(Supplier<T> supplier) {
            super(supplier);
        }
    }

    private <T extends Throwable> Type[] extractAllTypes(Class<?> from, ErrorProcessor<T> ep) throws T {
        if (!baseType.isAssignableFrom(from)) {
            String op = Modifier.isInterface(from.getModifiers()) == isInterface ? "extend" : "implement";
            throw new IllegalArgumentException(StringFormatter.format(from) + " does not " + op + " " + StringFormatter.format(baseType));
        }
        Type[] result = new Type[indexes.length];
        if (isInterface) {
            fromInterface0(from, result, ep);
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = findTypeParameterFromSuperClass(from, indexes[i], ep);
            }
        }
        return result;
    }

    public <T extends Throwable> Variable extractVariable(Class<?> from, ErrorProcessor<T> ep) throws T {
        if (indexes.length != 1) {
            throw new UnsupportedOperationException("This method can only be used when the extractor was extracts a single index, baseType = "
                    + StringFormatter.format(baseType) + ", indexes = " + Arrays.toString(indexes));
        }
        return extractAllVariables(from, ep)[0];
    }

    public <T extends Throwable> Type extractType(Class<?> from, ErrorProcessor<T> ep) throws T {
        if (indexes.length != 1) {
            throw new UnsupportedOperationException("This method can only be used when the extractor was extracts a single index, baseType = "
                    + StringFormatter.format(baseType) + ", indexes = " + Arrays.toString(indexes));
        }
        return extractAllTypes(from, ep)[0];
    }

    private <T extends Throwable> Type findTypeParameterFromSuperClass(Class<?> childClass, int typeVariableIndexOnBaseClass, ErrorProcessor<T> ep) throws T {
        // This method works by first recursively calling all the way down to the first class that extends baseClass.
        // And then we keep going finding out which of the actual type parameters matches the super classes type parameters

        if (baseType == childClass.getSuperclass()) {
            return findTypeParameterFromSuperClass0(childClass, typeVariableIndexOnBaseClass, ep);
        }
        Type pp = findTypeParameterFromSuperClass(childClass.getSuperclass(), typeVariableIndexOnBaseClass, ep);
        if (pp instanceof TypeVariable) {
            TypeVariable<?>[] tvs = childClass.getSuperclass().getTypeParameters();
            for (int i = 0; i < tvs.length; i++) {
                if (tvs[i].equals(pp)) {
                    return findTypeParameterFromSuperClass0(childClass, i, ep);
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
     * @throws T
     */
    private <T extends Throwable> Type findTypeParameterFromSuperClass0(Class<?> superClass, int index, ErrorProcessor<T> ep) throws T {
        Type t = superClass.getGenericSuperclass();
        if (!(t instanceof ParameterizedType pt)) {
            String name = superClass.getSuperclass().getTypeParameters()[index].getName();
            StringJoiner sj = new StringJoiner(", ", baseType.getSimpleName() + "<", ">");
            for (Type ty : baseType.getTypeParameters()) {
                sj.add(formatSimple(ty));
            }
            // TODO I think we have a special extension for this
            throw ep.onError("Cannot determine type variable <" + name + "> for " + sj.toString() + " on class " + format(superClass));
        }
        return pt.getActualTypeArguments()[index];
    }

    <T extends Throwable> boolean fromInterface0(Class<?> cc, Type[] result, ErrorProcessor<T> ep) {
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
                    IO.println(tt);
                    // Ahh fuck skal lave noget ledt her ogsaa....
                }
            } else if (t instanceof Class<?> cl) {
                if (fromInterface0(cl, result, ep)) {
                    return true;
                }
            }
        }
        return false;
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
            for (int element : indexes) {
                if (element < 0 || element >= typeVariables.length) {
                    throw new IllegalArgumentException("Cannot specify non existing index, index = " + element);
                }
            }
            return new TypeVariableExtractor(baseType, typeVariables, indexes);
        }
    }
}
