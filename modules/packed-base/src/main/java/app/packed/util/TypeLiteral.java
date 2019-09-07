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

package app.packed.util;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

import app.packed.reflect.ParameterDescriptor;
import packed.internal.access.AppPackedUtilAccess;
import packed.internal.access.SharedSecrets;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 * A TypeLiteral represents a generic type {@code T}. This class is used to work around the limitation that Java does
 * not provide a way to represent generic types. It does so by requiring user to create a subclass of this class which
 * enables retrieval of the type information even at runtime.
 * <p>
 * Sample usage:
 *
 * <pre> {@code
 * TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};
 * TypeLiteral<Map<Integer, List<Integer>>> list = new TypeLiteral<>() {};}
 * </pre>
 */
public abstract class TypeLiteral<T> {

    /** A cache of factories used by. */
    private static final ClassValue<TypeLiteral<?>> TYPE_VARIABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeLiteral<?> computeValue(Class<?> implementation) {
            return fromTypeVariable((Class) implementation, TypeLiteral.class, 0);
        }
    };

    static {
        SharedSecrets.initialize(AppPackedUtilAccess.class, new AppPackedUtilAccess() {

            @Override
            public boolean isCanonicalized(TypeLiteral<?> typeLiteral) {
                return typeLiteral.getClass() == CanonicalizedTypeLiteral.class;
            }

            /** {@inheritDoc} */
            @Override
            public Key<?> toKeyNullableQualifier(Type type, Annotation qualifier) {
                TypeLiteral<?> tl = new TypeLiteral.CanonicalizedTypeLiteral<>(type);
                return Key.fromTypeLiteralNullableAnnotation(type, tl, qualifier);
            }

            /** {@inheritDoc} */
            @Override
            public TypeLiteral<?> toTypeLiteral(Type type) {
                return new CanonicalizedTypeLiteral<>(type);
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
        TypeLiteral<?> tl = TYPE_VARIABLE_CACHE.get(getClass());
        this.type = tl.type;
        this.rawType = (Class<? super T>) tl.rawType;
    }

    /**
     * Constructs a type literal from the specific type.
     * 
     * @param type
     *            the type to create a type literal from
     */
    @SuppressWarnings("unchecked")
    TypeLiteral(Type type) {
        assert (type.getClass().getModule() == null || type.getClass().getModule().getName().equals("java.base"));
        this.type = requireNonNull(type, "type is null");
        this.rawType = (Class<? super T>) TypeUtil.findRawType(type);
    }

    /**
     * If this type literal is a {@link Class#isPrimitive() primitive type}, returns a boxed type literal. Otherwise returns
     * this.
     * 
     * @return if this type literal is a primitive returns the boxed version, otherwise returns this
     */
    @SuppressWarnings("unchecked")
    public final TypeLiteral<T> box() {
        if (rawType().isPrimitive()) {
            return (TypeLiteral<T>) of(TypeUtil.boxClass(rawType()));
        }
        return this;
    }

    /**
     * To avoid accidentally holding on to any instance that defines this type literal as an anonymous class. This method
     * creates a new type literal instance without any reference to the instance that defined the anonymous class.
     * 
     * @return the type literal
     */
    final CanonicalizedTypeLiteral<T> canonicalize() {
        if (getClass() == CanonicalizedTypeLiteral.class) {
            return (CanonicalizedTypeLiteral<T>) this;
        }
        return new CanonicalizedTypeLiteral<>(type);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj instanceof TypeLiteral && type.equals(((TypeLiteral<?>) obj).type);
    }

    /**
     * Returns the raw (non-generic) type.
     *
     * @return the raw (non-generic) type
     */
    public final Class<? super T> rawType() {
        return rawType;
    }

    /**
     * Returns the underlying type instance.
     *
     * @return the underlying type instance
     */
    public final Type type() {
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
     * @throws InvalidDeclarationException
     *             if the type literal could not be converted to a key, for example, if it is an {@link Optional}. Or if
     *             this type literal it not free from type parameters
     */
    public final Key<T> toKey() {
        return Key.fromTypeLiteral(this);
    }

    /**
     * Returns a key with the specified qualifier and the same type as this instance.
     * 
     * @param qualifier
     *            the qualifier of the new
     * @return a key with the specified qualifier and the same type as this instance
     * @throws InvalidDeclarationException
     *             if the type literal could not be converted to a key, for example, if it is an {@link Optional}. Or if the
     *             qualifier type is not annotated with {@link Qualifier}.
     */
    public final Key<T> toKey(Annotation qualifier) {
        return Key.fromTypeLiteral(this, qualifier);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return format(type);
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as {@link #toString()} does.
     * 
     * @return a simple string representation of this type
     */
    public final String toStringSimple() {
        return formatSimple(type);
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
        return new CanonicalizedTypeLiteral<>(field.getGenericType());
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
        return new CanonicalizedTypeLiteral<>(method.getGenericReturnType());
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
        return new CanonicalizedTypeLiteral<>(ParameterDescriptor.of(parameter).getParameterizedType());
    }

    /**
     * Creates a new type literal by extracting information from a type variable.
     * <p>
     * Given a class:
     * 
     * <pre> {@code
     * public abstract class MyConsumer extends HashMap<String, List<String>>} 
     * </pre>
     * <p>
     * The hash maps value type parameter can be extracted as a type literal by calling:
     * 
     * <pre> {@code
     * TypeLiteral<?> tl = TypeLiteral.fromTypeVariable(MyConsumer.class, HashMap.class, 1);
     * System.out.println(tl); //prints List<String>}
     * </pre>
     * 
     * @param <T>
     *            the base type to read the type variables from
     * @param subClass
     *            the sub class
     * @param superClass
     *            the base class that defines the type variable we want to get information on
     * @param parameterIndex
     *            the index in the signature of superClass of the type variable to extract
     * @return a type literal matching the type variable
     * @throws UnsupportedOperationException
     *             this method does not currently support extracting type information from interfaces
     * @throws IllegalArgumentException
     *             if the extraction could not be performed for some other reason
     */
    public static <T> TypeLiteral<?> fromTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        Type t = TypeVariableExtractorUtil.findTypeParameterUnsafe(subClass, superClass, parameterIndex);
        return new CanonicalizedTypeLiteral<>(t);
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
        return (TypeLiteral<T>) new CanonicalizedTypeLiteral<>(type);
    }

    /**
     * The default implementation of TypeLiteral. This is also the type we normally maintain a reference to internally. As
     * it is guaranteed to not retain any references to, for example, an instance that defined an anonymous class.
     * <p>
     * Returns a type literal from a type that is implemented by a class located in java.base, as these are known to be
     * intra-comparable.
     * <p>
     * This method is not available publically because you can really pass anything in like a Type. Since there are no
     * standard way to create hash codes for something like {@link ParameterizedType}, we need to make a copy of every
     * specified type to make sure different implementations calculates the same hash code. For example,
     * {@code BlueParameterizedType<String>} can have a different hashCode then {@code GreenParameterizedType<String>}
     * because {@link ParameterizedType} does not specify how the hash code is calculated. As a result we need to transform
     * both of them into instances of the same InternalParameterizedType. While this is not impossible, it is just a lot of
     * work, and has some overhead.
     * 
     */
    static final class CanonicalizedTypeLiteral<T> extends TypeLiteral<T> {

        /**
         * Creates a new type literal instance
         * 
         * @param type
         *            the type
         */
        CanonicalizedTypeLiteral(Type type) {
            super(type);
        }
    }
}
