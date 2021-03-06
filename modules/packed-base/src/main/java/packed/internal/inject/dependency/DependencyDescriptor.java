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
package packed.internal.inject.dependency;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.base.OldVariable;
import app.packed.base.TypeToken;
import app.packed.component.BuildException;
import packed.internal.errorhandling.ErrorMessageBuilder;
import packed.internal.invoke.typevariable.TypeVariableExtractor;
import packed.internal.util.BasePackageAccess;
import packed.internal.util.ClassUtil;
import packed.internal.util.QualifierUtil;
import packed.internal.util.ReflectionUtil;

/**
 * A descriptor of a dependency. An instance of this class is typically created from a parameter on a constructor or
 * method. In which case the parameter (represented by a {@link Parameter}) can be obtained by calling
 * {@link #variable()}. A descriptor can also be created from a field, in which case {@link #variable()} returns an
 * instance of. Dependencies can be optional in which case {@link #isOptional()} returns true.
 */
// Declaring class for use with Type Variables???
// Det her er ogsaa en Const..

// Supporterer vi noget mht til AnnotatedProvider?????
// Er det et nyt lag....
// Det der er, er at vi jo faktisk kan overskriver provideren...
// Dvs den er ikke statisk....

// int = Integer
// OptionalInt = Optional<Integer> = @Nullable Integer
// @Nullable int (Forbidden)

// Saa faar ogsaa lige pludselig.... Vi behoever vist en ny historie her......

// DefaultValue... (We need some default values converters...... Unless we have default converters...)
// Prime annotation (Den ødelægger jo lidt Key/Lazy/OSV, hvad hvis den nu vil noget andet...)
// Provider
// Lazy (Hvordan supportere vi denne???)

// Composite..... wtf... Det kan jo være 4 dependencies (Hirakiske dependencies)....
// Bliver helt klart noedt til at rethinke it.
// 1 parameter != 1 dependency...
// Requesting entity != key we ask a provided service for..
//// For example, we ask for a composite -> 5 calls (maybe to 3 different service providers)

//// Maaske skal den slet ikke exposes til brugere....
//// Men kun vaere en vi har internt.....

//// Den er vel meget god til Factory???
//// Men hvis vi nu bruger en composite... Giver den jo ingen mening...
//// Hvad hvis vi har den samme dependency 2 gange...
//// Hvad hvis vi bruger @Prime annotation.. Saa faar vi jo nogle andre dependencies.
// Som ikke er synlige...

//Dependency Chain.... 

//Dependency er flyttet til en intern klasse. Fordi den er begyndt at blive lidt for kompleks.
// Naar vi tilfoere composites. Hvor der ikke rigtig laengere er en parameter til en service mapning.

public final class DependencyDescriptor implements OldVariable {

    /** A cache of service dependencies. */
    private static final ClassValue<DependencyDescriptor> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected DependencyDescriptor computeValue(Class<?> type) {
            if (type == Optional.class) {
                throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
            } else if (type == OptionalInt.class) {
                return new DependencyDescriptor(int.class, Key.of(Integer.class), Optionality.OPTIONAL_INT, null);
            } else if (type == OptionalLong.class) {
                return new DependencyDescriptor(long.class, Key.of(Long.class), Optionality.OPTIONAL_LONG, null);
            } else if (type == OptionalDouble.class) {
                return new DependencyDescriptor(double.class, Key.of(Double.class), Optionality.OPTIONAL_DOUBLE, null);
            }
            return new DependencyDescriptor(type, Key.of(type), Optionality.REQUIRED, null);
        }
    };

    /** The key of this dependency. */
    private final Key<?> key;

    /** The optionality of this dependency. */
    private final Optionality optionality;

    /** The variable of this dependency. */
    @Nullable
    private final Parameter variable;

    final Type type;

    /**
     * Creates a new service dependency.
     * 
     * @param key
     *            the key
     * @param optionality
     *            the optional type
     * @param variable
     *            an optional field or parameter
     */
    private DependencyDescriptor(Type type, Key<?> key, Optionality optionality, @Nullable Parameter variable) {
        this.type = requireNonNull(type);
        this.key = requireNonNull(key, "key is null");
        this.optionality = requireNonNull(optionality);
        this.variable = variable;
    }

    /**
     * Returns an object representing an optional dependency could not be fulfilled. For example, this method will return
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
        return optionality.empty(this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj.getClass() != DependencyDescriptor.class) {
            return false;
        }
        DependencyDescriptor other = (DependencyDescriptor) obj;
        // Hmm hashcode and equals for optional????
        return Objects.equals(key, other.key) && optionality == other.optionality && Objects.equals(variable, other.variable);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int result = 31 + key.hashCode();
        result = 31 * result + optionality.ordinal();
        return 31 * result + Objects.hashCode(variable);
    }

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     */
    public boolean isOptional() {
        return optionality != Optionality.REQUIRED;
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
     * If this dependency was created from a member this method will an optional containing either a {@link Field} in case
     * of field injection, A {@link Method} in case of method injection or a {@link Constructor} in case of constructor
     * injection.
     * 
     * @return the member that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         member.
     */
    public Optional<Member> member() {
        // MemberDescriptor???
        return Optional.empty();
//        if (variable instanceof PackedFieldDescriptor) {
//            return Optional.of(((PackedFieldDescriptor) variable).copyField());
//        } else if (variable instanceof PackedParameterDescriptor) {
//            return Optional.of(((PackedExecutableDescriptor) ((PackedParameterDescriptor) variable).getDeclaringExecutable()).copyExecutable());
//        } else {
//            return Optional.empty();
//        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ServiceDependency[");
        if (optionality == Optionality.OPTIONAL_INT || optionality == Optionality.OPTIONAL_LONG || optionality == Optionality.OPTIONAL_DOUBLE) {
            sb.append(optionality.name());
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
     * The variable (field or parameter) from which this dependency originates. Or an empty {@link Optional} if this
     * dependency was not created from a variable.
     * 
     * @return the variable that is being injected, or an empty {@link Optional} if this dependency was not created from a
     *         variable.
     */
//    public Optional<Variable> variable() {
//        return Optional.empty();
//        // return Optional.ofNullable(variable);
//    }

    /**
     * TODO add nullable... Returns the specified object if not optional, or a the specified object in an optional type
     * (either {@link Optional}, {@link OptionalDouble}, {@link OptionalInt} or {@link OptionalLong}) if optional.
     *
     * @param object
     *            the object to potentially wrap in an optional type @return the specified object if not optional, or a the
     *            specified object in an optional type if optional.
     * 
     * @return the wrapped object if needed
     * @throws ClassCastException
     *             if this dependency is an optional type and type of this dependency does not match the specified object.
     */
    @Nullable
    public Object wrapIfOptional(Object object) {
        return optionality.wrapIfOptional(requireNonNull(object, "object is null"));
    }

    /**
     * Returns a list of dependencies from the specified executable.
     * 
     * @param executable
     *            the executable to return a list of dependencies for
     * @return a list of dependencies from the specified executable
     */
    public static List<DependencyDescriptor> fromExecutable(Executable executable) {
        Parameter[] parameters = executable.getParameters();
        return switch (parameters.length) {
        case 0 -> List.of();
        case 1 -> List.of(fromVariable(parameters[0], 0));
        case 2 -> List.of(fromVariable(parameters[0], 0), fromVariable(parameters[1], 1));
        default -> {
            DependencyDescriptor[] sd = new DependencyDescriptor[parameters.length];
            for (int i = 0; i < sd.length; i++) {
                sd[i] = fromVariable(parameters[i], i);
            }
            yield List.of(sd);
        }
        };
    }

    public static <T> DependencyDescriptor fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        Type type = TypeVariableExtractor.of(baseClass, baseClassTypeVariableIndex).extract(actualClass);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) actualClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation[] qa = QualifierUtil.findQualifier(annotations);

        Optionality optionalType = null;
        if (type instanceof ParameterizedType pt && pt.getRawType() == Optional.class) {
            type = pt.getActualTypeArguments()[0];
            // TODO check that we do not have optional of OptionalX, also ServiceRequest can never be optionally
            // Also Provider cannot be optionally...
            // TODO include annotation
            // Cannot have Nullable + Optional....
            optionalType = Optionality.OPTIONAL;
        } else if (type == OptionalInt.class) {
            optionalType = Optionality.OPTIONAL_INT;
            type = Integer.class;
        } else if (type == OptionalLong.class) {
            optionalType = Optionality.OPTIONAL_LONG;
            type = Long.class;
        } else if (type == OptionalDouble.class) {
            optionalType = Optionality.OPTIONAL_DOUBLE;
            type = Double.class;
        }
        if (optionalType == null) {
            optionalType = Optionality.REQUIRED;
        }
        // TODO check that there are no qualifier annotations on the type.
        return new DependencyDescriptor(type, BasePackageAccess.base().toKeyNullableQualifier(type, qa), optionalType, null);
    }

    public static <T> List<DependencyDescriptor> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        ArrayList<DependencyDescriptor> result = new ArrayList<>();
        for (int i = 0; i < baseClassTypeVariableIndexes.length; i++) {
            result.add(fromTypeVariable(actualClass, baseClass, baseClassTypeVariableIndexes[i]));
        }
        return List.copyOf(result);
    }

    // Taenker den her skal laves fra en Infuser...
    // Som bestemmer om vi f.eks. forstaar Provider, Optional osv.
    public static <T> DependencyDescriptor fromVariable(Parameter parameter, int index) {
        requireNonNull(parameter, "variable is null");

        Type getParameterizedType = ReflectionUtil.getParameterizedType(parameter, index);

        TypeToken<?> tl = TypeToken.fromType(getParameterizedType);

        Annotation[] qualifiers = QualifierUtil.findQualifier(parameter.getAnnotations());

        // Illegal
        // Optional<Optional*>
        Optionality optionallaity = null;
        Class<?> rawType = tl.rawType();

        // if (desc instanceof ParameterDescriptor) {
        // ParameterDescriptor pd = (ParameterDescriptor) desc;
        // if (pd.isVarArgs()) {
        // throw new InvalidDeclarationException(ErrorMessageBuilder.of(desc).cannot("use varargs for injection for " +
        // pd.getDeclaringExecutable()));
        // }
        // }
        if (rawType.isPrimitive()) {
            tl = tl.wrap();
        } else if (rawType == Optional.class) {
            optionallaity = Optionality.OPTIONAL;
            Type cl = ((ParameterizedType) getParameterizedType).getActualTypeArguments()[0];
            tl = BasePackageAccess.base().toTypeLiteral(cl);
            if (ClassUtil.isOptional(tl.rawType())) {
                throw new BuildException(ErrorMessageBuilder.of(parameter).cannot("have multiple layers of optionals such as " + cl));
            }
        } else if (rawType == OptionalLong.class) {
            optionallaity = Optionality.OPTIONAL_LONG;
            tl = TypeToken.of(Long.class);
        } else if (rawType == OptionalInt.class) {
            optionallaity = Optionality.OPTIONAL_INT;
            tl = TypeToken.of(Integer.class);
        } else if (rawType == OptionalDouble.class) {
            optionallaity = Optionality.OPTIONAL_DOUBLE;
            tl = TypeToken.of(Double.class);
        }

        if (parameter.isAnnotationPresent(Nullable.class)) {
            if (optionallaity != null) {
                // TODO fix name() to something more readable
                throw new BuildException(ErrorMessageBuilder.of(parameter).cannot("both be of type " + optionallaity.name() + " and annotated with @Nullable")
                        .toResolve("remove the @Nullable annotation, or make it a non-optional type"));
            }
            optionallaity = Optionality.OPTIONAL_NULLABLE;
        }

        if (optionallaity == null) {
            optionallaity = Optionality.REQUIRED;
        }
        // TL is free from Optional
        Key<?> key = Key.convertTypeLiteralNullableAnnotation(parameter, tl, qualifiers);

        return new DependencyDescriptor(getParameterizedType, key, optionallaity, parameter);
    }

    /**
     * Returns a service dependency on the specified class.
     *
     * @param key
     *            the class to return a dependency for
     * @return a service dependency for the specified class
     */
    public static DependencyDescriptor of(Class<?> key) {
        requireNonNull(key, "key is null");
        return CLASS_CACHE.get(key);
    }

    public static DependencyDescriptor of(Key<?> key) {
        requireNonNull(key, "key is null");
        if (!key.hasQualifiers()) {
            TypeToken<?> tl = key.typeToken();
            if (tl.type() == tl.rawType()) {
                return CLASS_CACHE.get(tl.rawType());
            }
        }
        return new DependencyDescriptor(key.typeToken().type(), key, Optionality.REQUIRED, null);
    }

    private enum Optionality {
        REQUIRED {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                throw new UnsupportedOperationException("This dependency is not optional, dependency = " + dependency);
            }
        },
        OPTIONAL {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                return Optional.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return Optional.of(object);
            }
        },
        OPTIONAL_INT {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                return OptionalInt.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalInt.of((Integer) object);
            }
        },
        OPTIONAL_LONG {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                return OptionalLong.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalLong.of((Long) object);
            }
        },
        OPTIONAL_DOUBLE {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                return OptionalDouble.empty();
            }

            @Override
            public Object wrapIfOptional(Object object) {
                return OptionalDouble.of((Double) object);
            }
        },
        OPTIONAL_NULLABLE {
            @Override
            public Object empty(DependencyDescriptor dependency) {
                return null;
            }
        };

        public abstract Object empty(DependencyDescriptor dependency);

        public Object wrapIfOptional(Object object) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> rawType() {
        return (Class<?>) type;
    }
}

/// **
// * Returns the optional container type ({@link Optional}, {@link OptionalInt}, {@link OptionalDouble},
// * {@link OptionalLong} or {@link Nullable}) that was used to create this dependency or {@code null} if this
/// dependency
// * is not optional.
// *
// * @return the optional container type
// * @see #isOptional()
// */
// @Nullable
// public Class<?> optionalContainerType() {
// return optionalType;
// }

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
///**
//* If this dependency represents a parameter to a constructor or method. This method will return the index of the
//* parameter, otherwise {@code -1}.
//* 
//* @apiNote While it would be natural for this method to return OptionalInt. We have found that in most use cases it has
//*          already been established whether a parameter is present via the optional return by {@link #variable()}.
//* 
//* @return the optional parameter index of the dependency
//*/
//public int parameterIndex() {
// return variable instanceof ParameterDescriptor ? variable.index() : -1;
//}