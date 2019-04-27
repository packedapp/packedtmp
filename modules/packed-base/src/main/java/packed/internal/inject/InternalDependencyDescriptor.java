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

import app.packed.inject.DependencyDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import app.packed.util.TypeLiteral;
import app.packed.util.VariableDescriptor;
import packed.internal.util.AppPackedUtilSupport;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.InternalErrorException;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalFieldDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;
import packed.internal.util.descriptor.InternalVariableDescriptor;

/**
 * The default implementation of {@link DependencyDescriptor}.
 */
public final class InternalDependencyDescriptor implements DependencyDescriptor {

    /** The key of this dependency. */
    private final Key<?> key;

    /**
     * Null if a non-optional dependency, otherwise one of {@link Optional}, {@link OptionalInt}, {@link OptionalLong},
     * {@link OptionalDouble} or {@link Nullable} annotation.
     */
    @Nullable
    private final Class<?> optionalType;

    /** The variable of this dependency. */
    @Nullable
    private final InternalVariableDescriptor variable;

    private InternalDependencyDescriptor(Key<?> key, Class<?> optionalType, InternalVariableDescriptor variable) {
        this.key = requireNonNull(key, "key is null");
        this.optionalType = optionalType;
        this.variable = variable;
    }

    /**
     * Returns an object indicating that an optional dependency could not be fulfilled. For example, this method will return
     * {@link OptionalInt#empty()} if a dependency was created from field with a {@link OptionalInt} type. And {@code null}
     * if a parameter is annotated with {@link Nullable}.
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
    public OptionalInt parameterIndex() {
        // TODO cache
        return variable == null ? OptionalInt.empty() : OptionalInt.of(variable.index());
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Member> member() {
        if (variable instanceof FieldDescriptor) {
            return Optional.of(((FieldDescriptor) variable));
        } else if (variable instanceof ParameterDescriptor) {
            return Optional.of(((ParameterDescriptor) variable).declaringExecutable());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the optional container type ({@link Optional}, {@link OptionalInt}, {@link OptionalDouble},
     * {@link OptionalLong} or {@link Nullable}) that was used to create this dependency or {@code null} if this dependency
     * is not optional.
     *
     * @return the optional container type
     * @see #isOptional()
     */
    @Nullable
    public Class<?> getOptionalContainerType() {
        return optionalType;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<VariableDescriptor> variable() {
        return Optional.ofNullable(variable);
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
    public static List<InternalDependencyDescriptor> fromExecutable(Executable executable) {
        requireNonNull(executable, "executable is null");
        throw new UnsupportedOperationException();
    }

    public static List<InternalDependencyDescriptor> fromExecutable(InternalExecutableDescriptor executable) {
        InternalParameterDescriptor[] parameters = executable.getParametersUnsafe();
        switch (parameters.length) {
        case 0:
            return List.of();
        case 1:
            return List.of(of(parameters[0]));
        case 2:
            return List.of(of(parameters[0]), of(parameters[1]));
        default:
            ArrayList<InternalDependencyDescriptor> list = new ArrayList<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                list.add(of(parameters[i]));
            }
            return List.copyOf(list);
        }
    }

    /**
     * Returns the type of the specified field as a key.
     * 
     * @param field
     *            the field to return a type literal for
     * @return the type literal for the field
     * @see Field#getGenericType()
     */
    public static InternalDependencyDescriptor fromField(Field field) {
        requireNonNull(field, "field is null");
        return ofVariable(InternalFieldDescriptor.of(field));
    }

    public static <T> InternalDependencyDescriptor fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
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
        return new InternalDependencyDescriptor(AppPackedUtilSupport.invoke().toKeyNullableQualifier(type, qa), optionalType, null);
    }

    public static <T> List<InternalDependencyDescriptor> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass,
            int... baseClassTypeVariableIndexes) {
        ArrayList<InternalDependencyDescriptor> result = new ArrayList<>();
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
    public static InternalDependencyDescriptor of(Class<?> type) {
        requireNonNull(type, "type is null");
        if (type == Optional.class) {
            throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
        } else if (type == OptionalInt.class) {
            return new InternalDependencyDescriptor(Key.of(Integer.class), OptionalInt.class, null);
        } else if (type == OptionalLong.class) {
            return new InternalDependencyDescriptor(Key.of(Long.class), OptionalLong.class, null);
        } else if (type == OptionalDouble.class) {
            return new InternalDependencyDescriptor(Key.of(Double.class), OptionalDouble.class, null);
        }
        return of(Key.of(type));
    }

    public static <T> InternalDependencyDescriptor of(Key<?> key) {
        return new InternalDependencyDescriptor(key, null, null);
    }

    public static <T> InternalDependencyDescriptor of(VariableDescriptor variable) {
        requireNonNull(variable, "variable is null");
        return ofVariable(InternalVariableDescriptor.unwrap(variable));
    }

    private static InternalDependencyDescriptor ofVariable(InternalVariableDescriptor variable) {
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
            tl = AppPackedUtilSupport.invoke().toTypeLiteral(cl);
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
        Key<?> key = Key.fromTypeLiteralNullableAnnotation(variable, tl, a);
        System.out.println(key);

        return new InternalDependencyDescriptor(key, optionalType, variable);
    }
}
