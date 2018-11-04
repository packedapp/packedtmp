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
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.util.ConstructorDescriptor;
import app.packed.util.ExecutableDescriptor;
import app.packed.util.FieldDescriptor;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import app.packed.util.VariableDescriptor;
import pckd.internal.inject.JavaXInjectSupport;
import pckd.internal.util.TypeVariableExtractorUtil;
import pckd.internal.util.descriptor.AbstractVariableDescriptor;
import pckd.internal.util.descriptor.InternalFieldDescriptor;
import pckd.internal.util.descriptor.InternalParameterDescriptor;

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
public final class Dependency {

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

    Dependency(Key<?> key, AbstractVariableDescriptor variable, Class<?> optionalType) {
        this.key = requireNonNull(key);
        this.index = variable.getIndex();
        this.optionalType = optionalType;
        this.variable = variable;
    }

    Dependency(Key<?> key, Class<?> optionalType) {
        this.key = requireNonNull(key, "key is null");
        this.optionalType = optionalType;
        this.index = 0;
        this.variable = null;
    }

    /**
     * Returns the object indicating that an optional dependency could not be fulfilled. For example, this method will
     * return {@link Optional#empty()} if the dependency was created from an {@link Optional} object. And {@code null} if a
     * parameter is annotated with {@link Nullable}.
     * <p>
     * If this dependency is not optional this method throws an {@link UnsupportedOperationException}.
     * 
     * @return a matching optional type
     * @throws UnsupportedOperationException
     *             if this dependency is not optional
     * @see Nullable
     * @see Optional#empty()
     * @see OptionalLong#empty()
     * @see OptionalInt#empty()
     * @see OptionalDouble#empty()
     */
    @Nullable
    public Object unresolvedValue() {
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
        throw new UnsupportedOperationException("Not a valid optional: " + key.getRawType());
    }

    /**
     * Returns the index of the dependency. If the dependency is created from a method or constructor, the index refers to
     * index of the parameter. If the dependency is created from a field, this method return 0.
     *
     * @return the index of the dependency
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the key of this dependency.
     *
     * @return the key of this dependency
     */
    public Key<?> getKey() {
        return key;
    }

    /**
     * Returns a {@link FieldDescriptor} in case of field injection, A {@link MethodDescriptor} in case of method injection
     * or a {@link ConstructorDescriptor} in case of constructor injection.
     * 
     * @return the member that is being injected
     */
    public Member getMember() {
        if (variable instanceof FieldDescriptor) {
            return ((FieldDescriptor) variable);
        } else {
            return ((ParameterDescriptor) variable).getDeclaringExecutable();
        }
    }

    /**
     * Returns the optional container type ({@link Optional}, {@link OptionalInt}, {@link OptionalDouble} or
     * {@link OptionalLong}) that was used to create this dependency or {@code null} if this dependency is not optional.
     *
     * @return the optional container type
     * @see #isOptional()
     */
    @Nullable
    public Class<?> getOptionalContainerType() {
        return optionalType;
    }

    public TypeLiteral<?> getTypeLiteral() {
        return key.getTypeLiteral();
    }

    /**
     * Returns whether or not this dependency is optional.
     *
     * @return whether or not this dependency is optional
     * @see #getOptionalContainerType()
     */
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
     * Returns a {@link FieldDescriptor} in case of field injection, or a {@link ParameterDescriptor} in case of method or
     * constructor injection.
     * 
     * @return the variable that is being injected
     */
    public VariableDescriptor variable() {
        return variable;
    }

    /**
     * TODO add nullable... Returns the specified object if not optional, or a the specified object in an optional type
     * (either {@link Optional}, {@link OptionalDouble}, {@link OptionalInt} or {@link OptionalLong}) if optional.
     *
     * @param o
     *            the object to potentially wrap in an optional type
     * @return the specified object if not optional, or a the specified object in an optional type if optional.
     * 
     * @throws ClassCastException
     *             if this dependency is an optional type and type of this dependency does not match the specified object.
     */
    @Nullable
    public Object wrapIfOptional(Object o) {
        if (optionalType == Optional.class) {
            return Optional.of(o);
        } else if (optionalType == OptionalLong.class) {
            return OptionalLong.of((Long) o);
        } else if (optionalType == OptionalInt.class) {
            return OptionalInt.of((Integer) o);
        } else if (optionalType == OptionalDouble.class) {
            return OptionalDouble.of((Double) o);
        }
        return o;
    }

    /**
     * Returns a list of dependencies from the specified executable
     * 
     * @param executable
     *            the executable to return a list of dependencies for
     * @return a list of dependencies from the specified executable
     */
    public static List<Dependency> fromExecutable(Executable executable) {
        requireNonNull(executable, "executable is null");
        throw new UnsupportedOperationException();
    }

    public static List<Dependency> fromExecutable(ExecutableDescriptor executable) {
        return executable.toDependencyList();
    }

    /**
     * Returns the type of the specified field as a key.
     * 
     * @param field
     *            the field to return a type literal for
     * @return the type literal for the field
     * @see Field#getGenericType()
     */
    public static Dependency fromField(Field field) {
        requireNonNull(field, "field is null");
        return ofVariable(InternalFieldDescriptor.of(field));
    }

    public static <T> Dependency fromTypeVariable(Class<? extends T> actualClass, Class<T> baseClass, int baseClassTypeVariableIndex) {
        Type type = TypeVariableExtractorUtil.findTypeParameterUnsafe(actualClass, baseClass, baseClassTypeVariableIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) actualClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation qa = null;
        if (annotations.length > 0) {
            for (Annotation a : annotations) {
                if (JavaXInjectSupport.isQualifierAnnotationPresent(a.annotationType())) {
                    if (qa != null) {
                        throw new IllegalArgumentException("More than 1 qualifier on " + actualClass);
                    }
                    qa = a;
                }
            }
        }
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
        return new Dependency(Key.internalOf(type, qa), optionalType);
    }

    public static <T> List<Dependency> fromTypeVariables(Class<? extends T> actualClass, Class<T> baseClass, int... baseClassTypeVariableIndexes) {
        ArrayList<Dependency> result = new ArrayList<>();
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
    public static Dependency of(Class<?> type) {
        requireNonNull(type, "type is null");
        if (type == Optional.class) {
            throw new IllegalArgumentException("Cannot determine type variable <T> for type Optional<T>");
        } else if (type == OptionalInt.class) {
            return new Dependency(Key.of(Integer.class), OptionalInt.class);
        } else if (type == OptionalLong.class) {
            return new Dependency(Key.of(Long.class), OptionalLong.class);
        } else if (type == OptionalDouble.class) {
            return new Dependency(Key.of(Double.class), OptionalDouble.class);
        }
        return of(Key.of(type));
    }

    public static <T> Dependency of(FieldDescriptor field) {
        return ofVariable(InternalFieldDescriptor.of(field));
    }

    public static <T> Dependency of(Key<?> key) {
        return new Dependency(key, null);
    }

    public static <T> Dependency of(ParameterDescriptor parameter) {
        return ofVariable(InternalParameterDescriptor.of(parameter));
    }

    private static Dependency ofVariable(AbstractVariableDescriptor variable) {
        TypeLiteral<?> tl = variable.getTypeLiteral();

        Optional<Annotation> q = variable.findQualifiedAnnotation();

        Class<?> optionalType = null;
        Class<?> rawType = tl.getRawType();
        if (rawType.isPrimitive()) {
            tl = tl.box();
        } else if (rawType == Optional.class) {
            optionalType = Optional.class;
            // TODO proper error msg
            Type cl = ((ParameterizedType) variable.getParameterizedType()).getActualTypeArguments()[0];
            tl = TypeLiteral.fromJavaImplementationType(cl);
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
                // Cannot use both nullable and optional
            }
            optionalType = Nullable.class;
        }

        Key<?> key;
        if (q.isPresent()) {
            key = tl.toKey(q.get());
        } else {
            key = tl.toKey();
        }
        return new Dependency(key, variable, optionalType);
    }

    // ofOptional istedet for tror jeg
    public static <T> Dependency optionalOf(Key<T> key, T defaultValue) {
        throw new UnsupportedOperationException();
    }

    public static Dependency optionalOfInt(int defaultValue) {
        throw new UnsupportedOperationException();
    }
}
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
