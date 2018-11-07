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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.Dependency;
import app.packed.inject.Key;
import app.packed.inject.TypeLiteral;
import app.packed.util.ExecutableDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.InternalErrorException;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;
import packed.internal.util.descriptor.AbstractVariableDescriptor;
import packed.internal.util.descriptor.InternalFieldDescriptor;

/**
 * A dependency Annotations are take from the field or parameter
 */

// Key <- extract information with Optional, Provider, etc
// Index (Er jo saadan set ogsaa i Parameter)
// Optional <-annotated with, or returning Optional, xyz
// Provider??? isProvider, isOptional

// OptionalInt != Optional<Integer> <- key is identical, but dependencies are not
// Also the way we need to things are different

/**
 * A dependency object. This is typically created from a parameter on a constructor or method. In which case the
 * parameter (represented by a {@link ParameterDescriptor}) can be obtained by calling {@link #variable}. It can also be
 * a field, in which case {@link #variable} returns an instance of {@link ParameterDescriptor}. Dependencies can be
 * optional in which case {@link #isOptional()} returns true.
 */
public final class InternalDependency implements Dependency {

    /** The index of this dependency. */
    private final int index;

    /** The key of this dependency. */
    private final Key<?> key;

    /**
     * Null if a non-optional dependency, otherwise one of {@link Optional}, {@link OptionalInt}, {@link OptionalLong},
     * {@link OptionalDouble} or {@link Nullable} annotation.
     */
    private final Class<?> optionalType;

    /** The variable of this dependency. */
    private final VariableDescriptor variable;

    InternalDependency(Key<?> key, AbstractVariableDescriptor variable, Class<?> optionalType) {
        this.key = requireNonNull(key);
        this.index = variable.getIndex();
        this.optionalType = optionalType;
        this.variable = variable;
    }

    InternalDependency(Key<?> key, Class<?> optionalType) {
        this.key = requireNonNull(key, "key is null");
        this.optionalType = optionalType;
        this.index = 0;
        this.variable = null;
    }

    /**
     * Returns an object indicating that an optional dependency could not be fulfilled. For example, this method will return
     * {@link Optional#empty()} if the dependency was created from an {@link Optional} object. And {@code null} if a
     * parameter is annotated with {@link Nullable}.
     * <p>
     * If this dependency is not optional this method throws an {@link UnsupportedOperationException}.
     * 
     * @return a matching optional empty type. Or null if the variable is annotated with {@link Nullable}.
     * @throws UnsupportedOperationException
     *             if this dependency is not optional
     * @see Nullable
     * @see Optional#empty()
     * @see OptionalLong#empty()
     * @see OptionalInt#empty()
     * @see OptionalDouble#empty()
     */
    @Nullable
    public Object emptyValue() {
        if (optionalType == Optional.class) {
            return Optional.empty();
        } else if (optionalType == OptionalLong.class) {
            return OptionalLong.empty();
        } else if (optionalType == OptionalInt.class) {
            return OptionalInt.empty();
        } else if (optionalType == OptionalDouble.class) {
            return OptionalDouble.empty();
        } else if (optionalType == Nullable.class) {
            return null;
        }
        throw new UnsupportedOperationException("This dependency is not optional, dependency = " + this);
    }

    /** {@inheritDoc} */
    @Override
    public int getIndex() {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> getKey() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> getMember() {
        if (variable instanceof FieldDescriptor) {
            return Optional.of(((FieldDescriptor) variable));
        } else {
            return Optional.of(((ParameterDescriptor) variable).getDeclaringExecutable());
        }
    }

    /**
     * Returns the optional container type ({@link Optional}, {@link OptionalInt}, {@link OptionalDouble} or
     * {@link OptionalLong}) that was used to create this dependency or {@code null} if this dependency is not optional.
     *
     * @return the optional container type
     * @see #isOptional()
     */
    public Class<?> getOptionalContainerType() {
        return optionalType;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOptional() {
        return optionalType != null;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dependency[");
        if (optionalType == OptionalInt.class || optionalType == OptionalLong.class || optionalType == OptionalDouble.class) {
            sb.append(optionalType.getSimpleName());
        } else {
            sb.append(key);
        }
        if (variable != null) {
            sb.append(", ");
            sb.append(variable);
        }
        sb.append("]");
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<VariableDescriptor> getVariable() {
        return Optional.ofNullable(variable);
    }

    /**
     * TODO add nullable... Returns the specified object if not optional, or a the specified object in an optional type
     * (either {@link Optional}, {@link OptionalDouble}, {@link OptionalInt} or {@link OptionalLong}) if optional.
     *
     * @param object
     *            the object to potentially wrap in an optional type @return the specified object if not optional, or a the
     *            specified object in an optional type if optional.
     * 
     * @throws ClassCastException
     *             if this dependency is an optional type and type of this dependency does not match the specified object.
     */
    public Object wrapIfOptional(Object object) {
        requireNonNull(object, "object is null");
        if (optionalType == null || optionalType == Nullable.class) {
            return object;
        } else if (optionalType == Optional.class) {
            return Optional.of(object);
        } else if (optionalType == OptionalLong.class) {
            return OptionalLong.of((Long) object);
        } else if (optionalType == OptionalInt.class) {
            return OptionalInt.of((Integer) object);
        } else if (optionalType == OptionalDouble.class) {
            return OptionalDouble.of((Double) object);
        }
        throw new InternalErrorException("object", object);
    }

    /**
     * Returns a list of dependencies from the specified executable
     * 
     * @param executable
     *            the executable to return a list of dependencies for
     * @return a list of dependencies from the specified executable
     */
    public static List<InternalDependency> fromExecutable(Executable executable) {
        requireNonNull(executable, "executable is null");
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<InternalDependency> fromExecutable(ExecutableDescriptor executable) {
        return (List) executable.toDependencyList();
    }

    /**
     * Returns the type of the specified field as a key.
     * 
     * @param field
     *            the field to return a type literal for
     * @return the type literal for the field
     * @see Field#getGenericType()
     */
    public static InternalDependency fromField(Field field) {
        requireNonNull(field, "field is null");
        return ofVariable(InternalFieldDescriptor.of(field));
    }

    public static <T> InternalDependency fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        Type type = TypeVariableExtractorUtil.findTypeParameterUnsafe(actualClass, baseClass, baseClassTypeVariableIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) actualClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation qa = JavaXInjectSupport.findQualifier(pta, annotations);

        Class<?> optionalType = null;
        if (type instanceof ParameterizedType && ((ParameterizedType) type).getRawType() == Optional.class) {
            type = ((ParameterizedType) type).getActualTypeArguments()[0];
            // TODO check that we do not have optional of OptionalX, also ServiceRequest can never be optionally
            // Also Provider cannot be optionally...
            // TODO include annotation
            optionalType = Optional.class;
        } else if (type == OptionalInt.class) {
            optionalType = OptionalInt.class;
            type = Integer.class;
        } else if (type == OptionalLong.class) {
            optionalType = OptionalLong.class;
            type = Long.class;
        } else if (type == OptionalDouble.class) {
            optionalType = OptionalDouble.class;
            type = Double.class;
        }
        // TODO check that there are no qualifier annotations on the type.
        return new InternalDependency(Key.internalOf(type, qa), optionalType);
    }

    public static <T> List<InternalDependency> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        ArrayList<InternalDependency> result = new ArrayList<>();
        for (int i = 0; i < baseClassTypeVariableIndexes.length; i++) {
            result.add(fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndexes[i]));
        }
        return List.copyOf(result);
    }

    /**
     * Returns a dependency on the specified class
     *
     * @param type
     *            the class to return a dependency for
     * @return a dependency for the specified class
     */
    public static InternalDependency of(Class<?> type) {
        requireNonNull(type, "type is null");
        if (type == Optional.class) {
            throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
        } else if (type == OptionalInt.class) {
            return new InternalDependency(Key.of(Integer.class), OptionalInt.class);
        } else if (type == OptionalLong.class) {
            return new InternalDependency(Key.of(Long.class), OptionalLong.class);
        } else if (type == OptionalDouble.class) {
            return new InternalDependency(Key.of(Double.class), OptionalDouble.class);
        }
        return of(Key.of(type));
    }

    public static <T> InternalDependency of(Key<?> key) {
        return new InternalDependency(key, null);
    }

    public static <T> InternalDependency of(VariableDescriptor variable) {
        requireNonNull(variable, "variable is null");
        return ofVariable(AbstractVariableDescriptor.unwrap(variable));
    }

    private static InternalDependency ofVariable(AbstractVariableDescriptor variable) {
        TypeLiteral<?> tl = variable.getTypeLiteral();
        Annotation a = variable.findQualifiedAnnotation();

        // Illegal
        // Optional<Optional*>
        Class<?> optionalType = null;
        Class<?> rawType = tl.getRawType();

        if (rawType.isPrimitive()) {
            tl = tl.box();
        } else if (rawType == Optional.class) {
            optionalType = Optional.class;
            Type cl = ((ParameterizedType) variable.getParameterizedType()).getActualTypeArguments()[0];
            tl = TypeLiteral.fromJavaImplementationType(cl);
            if (TypeUtil.isOptionalType(tl.getRawType())) {
                throw new InvalidDeclarationException(ErrorMessageBuilder.of(variable).cannot("have multiple layers of optionals such as " + cl));
            }
        } else if (rawType == OptionalLong.class) {
            optionalType = OptionalLong.class;
            tl = TypeLiteral.of(Long.class);
        } else if (rawType == OptionalInt.class) {
            optionalType = OptionalInt.class;
            tl = TypeLiteral.of(Integer.class);
        } else if (rawType == OptionalDouble.class) {
            optionalType = OptionalDouble.class;
            tl = TypeLiteral.of(Double.class);
        }

        if (variable.isAnnotationPresent(Nullable.class)) {
            if (optionalType != null) {
                throw new InvalidDeclarationException(
                        ErrorMessageBuilder.of(variable).cannot("both be of type " + optionalType.getSimpleName() + " and annotated with @Nullable")
                                .toResolve("remove the @Nullable annotation, or make it a non-optional type"));
            }
            optionalType = Nullable.class;
        }

        // TL is free from Optional
        Key<?> key = tl.toKeyNullableAnnotation(a);
        return new InternalDependency(key, variable, optionalType);
    }
}

//// ofOptional istedet for tror jeg
// public static <T> Dependency optionalOf(Key<T> key, T defaultValue) {
// throw new UnsupportedOperationException();
// }
//
// public static Dependency optionalOfInt(int defaultValue) {
// throw new UnsupportedOperationException();
// }
//
// /**
// * Creates a new dependency keeping the same properties as this dependency but replacing the existing index with the
// * specified index.
// *
// * @param index
// * the index of the returned variable
// * @return a new dependency with the specified index
// * @throws IllegalArgumentException
// * if the index is negative ({@literal <}0
// */
// public Dependency withIndex(int index) {
// throw new UnsupportedOperationException();
// }
