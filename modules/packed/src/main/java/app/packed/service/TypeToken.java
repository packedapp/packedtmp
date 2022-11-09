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
package app.packed.service;

import static internal.app.packed.util.StringFormatter.format;
import static internal.app.packed.util.StringFormatter.formatSimple;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import app.packed.framework.Nullable;
import internal.app.packed.util.BasePackageAccess;
import internal.app.packed.util.BasePackageAccess.AppPackedBaseAccess;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.TypeUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/**
 * A TypeLiteral represents a generic type {@code T}. This class is used to work around the limitation that Java does
 * not provide a way to represent generic types. It does so by requiring user to create a subclass of this class which
 * enables retrieval of the type information even at runtime.
 * <p>
 * Sample usage:
 *
 * <pre> {@code
 * TypeToken<List<String>> list = new TypeToken<List<String>>() {};
 * TypeToken<Map<Integer, List<Integer>>> list = new TypeToken<>() {};}
 * </pre>
 */
//https://www.reddit.com/r/java/comments/6b9zvl/do_you_think_we_should_have_a_typetoken_class/
//http://mail.openjdk.java.net/pipermail/valhalla-dev/2017-January/002150.html

// Take a look at helidons
// https://helidon.io/docs/v2/apidocs/io.helidon.common/io/helidon/common/GenericType.html
// I like the cast method

// Maybe this should die so we only have one version which also captures annotations
// We always va

// I don't think we will touch until we see where Valhalla goes
public abstract class TypeToken<T> {

    /** A cache of factories used by. */
    private static final ClassValue<TypeToken<?>> TYPE_VARIABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected TypeToken<?> computeValue(Class<?> implementation) {
            return fromTypeVariable((Class) implementation, TypeToken.class, 0);
        }
    };

    static {
        BasePackageAccess.initialize(AppPackedBaseAccess.class, new AppPackedBaseAccess() {

            /** {@inheritDoc} */
            @Override
            public Key<?> toKeyNullableQualifier(Type type, Annotation[] qualifier) {
                TypeToken<?> tl = new TypeToken.CanonicalizedTypeToken<>(type);
                return Key.convertTypeLiteralNullableAnnotation(type, tl, qualifier);
            }

            /** {@inheritDoc} */
            @Override
            public TypeToken<?> toTypeLiteral(Type type) {
                return new CanonicalizedTypeToken<>(type);
            }
        });
    }

    /**
     * We cache the hash code of the type, as many Type implementations calculate it every time. See, for example,
     * https://github.com/frohoff/jdk8u-jdk/blob/master/src/share/classes/sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java
     */
    private int hash;

    /** The raw type. */
    private final Class<? super T> rawType; // create it on demand???

    /** The underlying type. */
    private final Type type;

    /**
     * Constructs a new type token by deriving the actual type from the type parameter of the extending class.
     * 
     * @throws RuntimeException
     *             if the type could not be determined
     */
    @SuppressWarnings("unchecked")
    protected TypeToken() {
        TypeToken<?> tl = TYPE_VARIABLE_CACHE.get(getClass());
        this.type = tl.type;
        this.rawType = (Class<? super T>) tl.rawType;
    }

    /**
     * Constructs a type token from the specific type.
     * 
     * @param type
     *            the type to create a type token from
     */
    @SuppressWarnings("unchecked")
    TypeToken(Type type) {
        // This was a test to make sure all types are canonicalized
        // assert (type.getClass().getModule() == null || type.getClass().getModule().getName().equals("java.base"));
        this.type = requireNonNull(type, "type is null");
        this.rawType = (Class<? super T>) TypeUtil.rawTypeOf(type);
    }

    /**
     * To avoid accidentally holding on to any instance that defines this type token as an anonymous class. This method
     * creates a new type token instance without any reference to the instance that defined the anonymous class.
     * 
     * @return the type token
     */
    // Not sure we want this public
    final CanonicalizedTypeToken<T> canonicalize() {
        if (getClass() == CanonicalizedTypeToken.class) {
            return (CanonicalizedTypeToken<T>) this;
        }
        return new CanonicalizedTypeToken<>(type);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj instanceof TypeToken<?> tt && type.equals(tt.type);
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

    public final boolean isCanonicalized() {
        return getClass() == CanonicalizedTypeToken.class;
    }

    /**
     * Returns the raw (non-generic) type.
     *
     * @return the raw (non-generic) type
     */
    public final Class<?> rawType() {
        return rawType;
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
     * Returns the underlying type instance.
     *
     * @return the underlying type instance
     */
    public final Type type() {
        return type;
    }

    /**
     * If this type token is a {@link Class#isPrimitive() primitive type}, returns a boxed type token. Otherwise returns
     * this.
     * 
     * @return if this type token is a primitive returns the boxed version, otherwise returns this
     */
    // wrap instead of box
    @SuppressWarnings("unchecked")
    public final TypeToken<T> wrap() {
        // TODO fix for Valhalla? reference type, inline type...
        if (rawType().isPrimitive()) {
            return (TypeToken<T>) of(ClassUtil.wrap(rawType()));
        }
        return this;
    }

    /**
     * Returns the type of the specified field as a type token.
     * 
     * @param field
     *            the field to return a type token for
     * @return the type token for the field
     * @see Field#getGenericType()
     */
    public static TypeToken<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        return new CanonicalizedTypeToken<>(field.getGenericType());
    }

    /**
     * Returns the type of the specified method's return type as a type token.
     * 
     * @param method
     *            the method whose return type to return a type token for
     * @return the type token for the return type of the specified method
     * @see Method#getGenericReturnType()
     */
    public static TypeToken<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        return new CanonicalizedTypeToken<>(method.getGenericReturnType());
    }

    /**
     * Returns the type of the specified parameter as a type token.
     * 
     * @param type
     *            the parameter to return a type token for
     * @return the type token for the parameter
     * @see Parameter#getParameterizedType()
     */
    public static TypeToken<?> fromType(Type type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedTypeToken<>(type);
    }

    /**
     * Creates a new type token by extracting information from a type variable.
     * <p>
     * Given a class:
     * 
     * <pre> {@code
     * public abstract class MyConsumer extends HashMap<String, List<String>>} 
     * </pre>
     * <p>
     * The hash maps value type parameter can be extracted as a type token by calling:
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
     * @return a type token matching the type variable
     * @throws UnsupportedOperationException
     *             this method does not currently support extracting type information from interfaces
     * @throws IllegalArgumentException
     *             if the extraction could not be performed for some other reason
     */
    public static <T> TypeToken<?> fromTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        Type t = TypeVariableExtractor.of(superClass, parameterIndex).extract(subClass);
        return new CanonicalizedTypeToken<>(t);
    }

    /**
     * Returns a type token for the specified class.
     *
     * @param <T>
     *            the type
     * @param type
     *            the class to return a type token for
     * @return a type token for the specified class type
     */
    public static <T> TypeToken<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedTypeToken<T>(type);
    }

    /**
     * The default implementation of TypeToken. This is also the type we normally maintain a reference to internally. As it
     * is guaranteed to not retain any references to, for example, an instance that defined an anonymous class.
     * <p>
     * Returns a type token from a type that is implemented by a class located in java.base, as these are known to be
     * intra-comparable.
     * <p>
     * This method is not available publically because you can really pass anything in like a Type. Since there are no
     * standard way to create hash codes for something like {@link ParameterizedType}, we need to make a copy of every
     * specified type to make sure different implementations calculates the same hash code. For example,
     * {@code BlueParameterizedType<String>} can have a different hashCode then {@code GreenParameterizedType<String>}
     * because {@link ParameterizedType} does not specify how the hash code is calculated. As a result we need to transform
     * both of them into instances of the same InternalParameterizedType. While this is not impossible, it is just a lot of
     * work, and has some overhead.
     */
    static final class CanonicalizedTypeToken<T> extends TypeToken<T> {

        /**
         * Creates a new type token instance
         * 
         * @param type
         *            the type
         */
        CanonicalizedTypeToken(Type type) {
            super(type);
        }
    }
}
