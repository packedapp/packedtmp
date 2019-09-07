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
package app.packed.inject;

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
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.reflect.ConstructorDescriptor;
import app.packed.reflect.ExecutableDescriptor;
import app.packed.reflect.FieldDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.reflect.ParameterDescriptor;
import app.packed.reflect.VariableDescriptor;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.Nullable;
import app.packed.util.TypeLiteral;
import packed.internal.access.SharedSecrets;
import packed.internal.inject.util.QualifierHelper;
import packed.internal.reflect.typevariable.TypeVariableExtractor;
import packed.internal.util.ErrorMessageBuilder;
import packed.internal.util.InternalErrorException;
import packed.internal.util.TypeUtil;

/**
 * A descriptor of a dependency. An instance of this class is typically created from a parameter on a constructor or
 * method. In which case the parameter (represented by a {@link ParameterDescriptor}) can be obtained by calling
 * {@link #variable()}. A descriptor can also be created from a field, in which case {@link #variable()} returns an
 * instance of {@link FieldDescriptor}. Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
// Declaring class for use with Type Variables???
public final class ServiceDependency {

    /** A cache of service dependencies. */
    private static final ClassValue<ServiceDependency> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ServiceDependency computeValue(Class<?> type) {
            if (type == Optional.class) {
                throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
            } else if (type == OptionalInt.class) {
                return new ServiceDependency(Key.of(Integer.class), OptionalInt.class, null);
            } else if (type == OptionalLong.class) {
                return new ServiceDependency(Key.of(Long.class), OptionalLong.class, null);
            } else if (type == OptionalDouble.class) {
                return new ServiceDependency(Key.of(Double.class), OptionalDouble.class, null);
            }
            return new ServiceDependency(Key.of(type), null, null);
        }
    };

    /** The key of this dependency. */
    private final Key<?> key;

    /**
     * Null if it is a required dependency, otherwise one of {@link Optional}, {@link OptionalInt}, {@link OptionalLong},
     * {@link OptionalDouble} or {@link Nullable} annotation.
     */
    @Nullable
    private final Class<?> optionalType;

    /** The variable of this dependency. */
    @Nullable
    private final VariableDescriptor variable;

    /**
     * Creates a new dependency.
     * 
     * @param key
     *            the key
     * @param optionalType
     *            the optional type
     * @param variable
     *            an optional field or parameter
     */
    private ServiceDependency(Key<?> key, @Nullable Class<?> optionalType, @Nullable VariableDescriptor variable) {
        this.key = requireNonNull(key, "key is null");
        this.optionalType = optionalType;
        this.variable = variable;
    }

    /**
     * Returns an object indicating that an optional dependency could not be fulfilled. For example, this method will return
     * {@link OptionalInt#empty()} if a dependency was created from a field with a {@link OptionalInt} type. And
     * {@code null} if a parameter is annotated with {@link Nullable}.
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() != ServiceDependency.class) {
            return false;
        }
        ServiceDependency other = (ServiceDependency) obj;
        // Hmm hashcode and equals for optional????
        return Objects.equals(key, other.key) && optionalType == other.optionalType && Objects.equals(variable, other.variable);
    }

    @Override
    public int hashCode() {
        int result = 31 + key.hashCode();
        result = 31 * result + Objects.hashCode(optionalType);
        return 31 * result + Objects.hashCode(variable);
    }

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    public boolean isOptional() {
        return optionalType != null;
    }

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    public Key<?> key() {
        return key;
    }

    /**
     * The member (field, method or constructor) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a member.
     * <p>
     * If this dependency was created from a member this method will an optional containing either a {@link FieldDescriptor}
     * in case of field injection, A {@link MethodDescriptor} in case of method injection or a {@link ConstructorDescriptor}
     * in case of constructor injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     * @see #variable()
     */
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
    public Class<?> optionalContainerType() {
        return optionalType;
    }

    /**
     * If this dependency represents a parameter to a constructor or method. This method will return the index of the
     * parameter, otherwise {@code -1}.
     * 
     * @apiNote While it would be natural for this method to return OptionalInt. We have found that in most use cases it has
     *          already been established whether a parameter is present via the optional return by {@link #variable()}.
     * 
     * @return the optional parameter index of the dependency
     */
    public int parameterIndex() {
        return variable instanceof ParameterDescriptor ? variable.index() : -1;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceDependency[");
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
     * The variable (field or parameter) for which this dependency was created. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * <p>
     * If this dependency was created from a field this method will return a {@link FieldDescriptor}. If this dependency was
     * created from a parameter this method will return a {@link ParameterDescriptor}.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     * @see #member()
     */
    public Optional<VariableDescriptor> variable() {
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
    public static List<ServiceDependency> fromExecutable(Executable executable) {
        return fromExecutable(ExecutableDescriptor.of(executable));
    }

    public static List<ServiceDependency> fromExecutable(ExecutableDescriptor desc) {
        ParameterDescriptor[] parameters = desc.getParametersUnsafe();
        switch (parameters.length) {
        case 0:
            return List.of();
        case 1:
            return List.of(fromVariable(parameters[0]));
        case 2:
            return List.of(fromVariable(parameters[0]), fromVariable(parameters[1]));
        default:
            ArrayList<ServiceDependency> list = new ArrayList<>(parameters.length);
            for (int i = 0; i < parameters.length; i++) {
                list.add(fromVariable(parameters[i]));
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
    public static ServiceDependency fromField(Field field) {
        return fromVariable(FieldDescriptor.of(field));
    }

    public static ServiceDependency fromField(FieldDescriptor field) {
        return fromVariable(field);
    }

    public static <T> ServiceDependency fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        Type type = TypeVariableExtractor.of(baseClass, baseClassTypeVariableIndex).extract(actualClass);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) actualClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation qa = QualifierHelper.findQualifier(pta, annotations);

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
        return new ServiceDependency(SharedSecrets.util().toKeyNullableQualifier(type, qa), optionalType, null);
    }

    public static <T> List<ServiceDependency> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        ArrayList<ServiceDependency> result = new ArrayList<>();
        for (int i = 0; i < baseClassTypeVariableIndexes.length; i++) {
            result.add(fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndexes[i]));
        }
        return List.copyOf(result);
    }

    public static <T> ServiceDependency fromVariable(VariableDescriptor desc) {
        requireNonNull(desc, "variable is null");
        TypeLiteral<?> tl = desc.getTypeLiteral();

        Annotation qualifier = desc.findQualifiedAnnotation();

        // Illegal
        // Optional<Optional*>
        Class<?> optionalType = null;
        Class<?> rawType = tl.rawType();

        if (rawType.isPrimitive()) {
            tl = tl.box();
        } else if (rawType == Optional.class) {
            optionalType = Optional.class;
            Type cl = ((ParameterizedType) desc.getParameterizedType()).getActualTypeArguments()[0];
            tl = SharedSecrets.util().toTypeLiteral(cl);
            if (TypeUtil.isOptionalType(tl.rawType())) {
                throw new InvalidDeclarationException(ErrorMessageBuilder.of(desc).cannot("have multiple layers of optionals such as " + cl));
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

        if (desc.isAnnotationPresent(Nullable.class)) {
            if (optionalType != null) {
                throw new InvalidDeclarationException(
                        ErrorMessageBuilder.of(desc).cannot("both be of type " + optionalType.getSimpleName() + " and annotated with @Nullable")
                                .toResolve("remove the @Nullable annotation, or make it a non-optional type"));
            }
            optionalType = Nullable.class;
        }

        // TL is free from Optional
        Key<?> key = Key.fromTypeLiteralNullableAnnotation(desc, tl, qualifier);

        return new ServiceDependency(key, optionalType, desc);
    }

    /**
     * Returns a dependency on the specified class
     *
     * @param type
     *            the class to return a dependency for
     * @return a dependency for the specified class
     */
    public static ServiceDependency of(Class<?> type) {
        requireNonNull(type, "type is null");
        return CLASS_CACHE.get(type);
    }

    public static <T> ServiceDependency of(Key<?> key) {
        requireNonNull(key, "key is null");
        if (!key.hasQualifier()) {
            TypeLiteral<?> tl = key.typeLiteral();
            if (tl.type() == tl.rawType()) {
                return CLASS_CACHE.get(tl.rawType());
            }
        }
        return new ServiceDependency(key, null, null);
    }

    public static <T> ServiceDependency ofOptional(Key<?> key) {
        requireNonNull(key, "key is null");
        if (!key.hasQualifier()) {
            TypeLiteral<?> tl = key.typeLiteral();
            if (tl.type() == tl.rawType()) {
                return CLASS_CACHE.get(tl.rawType());
            }
        }
        return new ServiceDependency(key, Nullable.class, null);
    }
}
// Flyt member, parameterIndex og Variable???? til ServiceRequest..
// Vi goer det kun for at faa en paenere arkitk
// Bliver brugt med factory, for at kunne se dens dependencies.....
// Bliver brugt med Factory + BindableFactory

// From Field
// From Parameter
// From Type variable
// From InjectorExtension.require
// From InjectorExtension.optional
// Via Wildcard Qualifier methods (static? Dependency Chain). Jeg har en @Foo int fff
// -- Som goer at jeg dependenr paa Configuration + XConverter.. Heh saa giver parameter index.. vel ikke mening
// -- Kunne lave en limitation der siger at man kun maa transformere med 1 parameter...

// Vi tager alle annotations med...@SystemProperty(fff) @Foo String xxx
// Includes any qualifier...

/// **
// * Returns . Returns
// *
// * @return stuff
// */
// default AnnotatedElement annotations() {

// }
