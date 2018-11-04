/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import packed.internal.inject.InjectSupport;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.inject.factory.InternalFactory;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/**
 * A TypeLiteral represents a generic type {@code T}. This class is used to work around the limitation that Java does
 * not provide a way to represent generic types. It does so by requiring user to create a subclass of this class which
 * enables retrieval of the type information even at runtime. Usage:
 *
 * <pre> {@code
 * TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};
 * TypeLiteral<Map<Integer, List<Integer>>> list = new TypeLiteral<>() {};}
 * </pre>
 */
public class TypeLiteral<T> {

    static {
        InjectSupport.Helper.init(new InjectSupport.Helper() {

            /** {@inheritDoc} */
            @Override
            protected <T> InternalFactory<T> toInternalFactory(Factory<T> factory) {
                return factory.factory;
            }

            /** {@inheritDoc} */
            @Override
            protected Key<?> toKeyNullableQualifier(Type type, Annotation qualifier) {
                return null;
            }

            /** {@inheritDoc} */
            @Override
            protected TypeLiteral<?> toTypeLiteral(Type type) {
                return TypeLiteral.fromJavaImplementationType(type);
            }
        });
    }

    /**
     * We cache the hash code of the type, as most Type implementations calculates it every time. See, for example,
     * https://github.com/frohoff/jdk8u-jdk/blob/master/src/share/classes/sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java
     */
    private int hash;

    /** The raw type. */
    private final Class<? super T> rawType;

    /** The underlying type. */
    private final Type type;

    /**
     * Constructs a new type literal by deriving the actual type from the type parameter of the extending class.
     * 
     * @throws RuntimeException
     *             if the type could not be determined
     */
    @SuppressWarnings("unchecked")
    protected TypeLiteral() {
        this.type = TypeVariableExtractorUtil.findTypeParameterFromSuperClass(getClass(), TypeLiteral.class, 0);
        this.rawType = (Class<? super T>) TypeUtil.findRawType(type);
    }

    /**
     * Constructs a type literal from the specific type.
     * 
     * @param type
     *            the type to create a type literal from
     */
    @SuppressWarnings("unchecked")
    private TypeLiteral(Type type) {
        this.type = requireNonNull(type, "type is null");
        this.rawType = (Class<? super T>) TypeUtil.findRawType(this.type);
    }

    /**
     * If this type literal is a {@link Class#isPrimitive() primitive type}, returns a boxed type literal. Otherwise returns
     * this.
     * 
     * @return if this type literal is a primitive returns the boxed version, otherwise returns this
     */
    @SuppressWarnings("unchecked")
    public final TypeLiteral<T> box() {
        if (getRawType().isPrimitive()) {
            return (TypeLiteral<T>) of(TypeUtil.boxClass(getRawType()));
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof TypeLiteral && type.equals(((TypeLiteral<?>) obj).type);
    }

    /**
     * Returns the raw (non-generic) type.
     *
     * @return the raw (non-generic) type
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Returns the underlying type instance.
     *
     * @return the underlying type instance
     */
    public final Type getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        int h = hash;
        if (h != 0) {
            return h;
        }
        return hash = type.hashCode();
    }

    /**
     * Returns a key with no qualifier and the same type as this instance.
     * 
     * @return a key with no qualifier and the same type as this instance
     */
    public final Key<T> toKey() {
        return toKeyNullableAnnotation(null);
    }

    public final Key<T> toKey(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return toKeyNullableAnnotation(qualifier);
    }

    private Key<T> toKeyNullableAnnotation(Annotation qualifier) {
        if (TypeUtil.isOptionalType(rawType)) {
            throw new UnsupportedOperationException("Cannot convert an optional type (" + toShortString() + ") to a Key, as keys cannot be optional");
        } else if (!TypeUtil.isFreeFromTypeVariables(type)) {
            throw new UnsupportedOperationException("Can only convert type literals that are free from type variables to a Key, however '" + toShortString()
                    + "' defined: " + TypeUtil.findTypeVariableNames(type));
        }
        TypeLiteral<T> tl = this;
        // We So we do not inadvertely hold on to the class that defines the anonumous.
        // To avoid inadvertely holding on to the class that defines a anonymous class, we cananolize the this type literal.
        // When creating the key
        if (this.getClass() != TypeLiteral.class) {
            tl = new TypeLiteral<>(type);
        }
        return Key.fromCheckedTypeAndCheckedNullableAnnotation(tl, qualifier);
    }

    /**
     * Returns a string where all the class names are not fully specified. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as the {@link #toString()} method does.
     * 
     * @return a short string
     */
    public final String toShortString() {
        return TypeUtil.toShortString(type);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (type instanceof Class) {
            return ((Class<?>) type).getCanonicalName(); // strip 'class/interface' from it
        }
        return type.toString();
    }

    /**
     * Returns the type of the specified field as a type literal.
     * 
     * @param field
     *            the field to return a type literal for
     * @return the type literal for the field
     * @see Field#getGenericType()
     */
    public static TypeLiteral<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        return fromJavaImplementationType(field.getGenericType());
    }

    /**
     * Returns a type literal from a type that is implemented by a class located in java.base, as these are known to be
     * intra-comparable.
     *
     * @apiNote this method is not available publically because you can really pass anything in like a Type. Since there are
     *          no standard way to create hash codes for something like {@link ParameterizedType}, we need to make a copy of
     *          every specified type to make sure different implementations calculates the same hash code. For example,
     *          {@code BlueParameterizedType<String>} can have a different hashCode then
     *          {@code GreenParameterizedType<String>} because {@link ParameterizedType} does not specify how the hash code
     *          is calculated. As a result we need to transform both of them into instances of the same
     *          InternalParameterizedType. While this is not impossible, it is just a lot of work, and has some overhead.
     * @param type
     *            the type to return a type literal for
     * @return a type literal from the specified type
     * @see #of(Class)
     */
    static TypeLiteral<?> fromJavaImplementationType(Type type) {
        return new TypeLiteral<>(type);
    }

    /**
     * Returns the type of the specified method's return type as a type literal.
     * 
     * @param method
     *            the method whose return type to return a type literal for
     * @return the type literal for the return type of the specified method
     * @see Method#getGenericReturnType()
     */
    public static TypeLiteral<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        return fromJavaImplementationType(method.getGenericReturnType());
    }

    /**
     * Returns the type of the specified parameter as a type literal.
     * 
     * @param parameter
     *            the parameter to return a type literal for
     * @return the type literal for the parameter
     * @see Parameter#getParameterizedType()
     */
    public static TypeLiteral<?> fromParameter(Parameter parameter) {
        requireNonNull(parameter, "parameter is null");
        return fromJavaImplementationType(InternalParameterDescriptor.of(parameter).getParameterizedType());
    }

    /**
     * Creates a new type literal by reading extracting information for a type variable.
     *
     * @param <T>
     *            the base type to read the type variables from
     * @param subClass
     *            the sub class
     * @param superClass
     *            the base class that defines the type
     * @param parameterIndex
     *            the index in the signature of superClass of the type variable to extract
     * @return a type literal matching the type variable
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> fromTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        Type t = TypeVariableExtractorUtil.findTypeParameterUnsafe(subClass, superClass, parameterIndex);
        return (TypeLiteral<T>) fromJavaImplementationType(t);
    }

    /**
     * Returns a type literal of the specified class type.
     *
     * @param <T>
     *            the type
     * @param type
     *            the class instance to return a type literal of
     * @return a type literal of the specified class type
     */
    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return (TypeLiteral<T>) fromJavaImplementationType(type);
    }
}
