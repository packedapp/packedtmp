/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Type;

import org.jspecify.annotations.Nullable;
import internal.app.packed.util.StringFormatter;

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

// I think implement type (Variable shouldn't do it though)
public abstract class GenericType<T> {

    /** A cache of generic types. */
    private static final ClassValue<GenericType<?>> CAPTURED_CACHE = new ClassValue<>() {

        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(GenericType.class);

        /** {@inheritDoc} */
        @Override
        protected GenericType<?> computeValue(Class<?> implementation) {
            Type t = EXTRACTOR.extractType(implementation, IllegalArgumentException::new);
            return new CanonicalizedGenericType<>(t);
        }
    };

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
    protected GenericType() {
        GenericType<?> tl = CAPTURED_CACHE.get(getClass());
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
    GenericType(Type type) {
        // This was a test to make sure all types are canonicalized
        // assert (type.getClass().getModule() == null || type.getClass().getModule().getName().equals("java.base"));
        this.type = Types.canonicalize(requireNonNull(type, "type is null"));
        this.rawType = (Class<? super T>) TypeUtil.rawTypeOf(type);
    }

    /**
     * To avoid accidentally holding on to any instance that defines this type token as an anonymous class. This method
     * creates a new type token instance without any reference to the instance that defined the anonymous class.
     *
     * @return the type token
     */
    // Not sure we want this public
    public final GenericType<T> canonicalize() {
        if (getClass() == CanonicalizedGenericType.class) {
            return this;
        }
        return new CanonicalizedGenericType<>(type);
    }

    /**
     * Casts the specified object to the type represented by this {@code GenericType} object.
     *
     * @param obj
     *            instance to cast
     * @return the object after casting, or null if obj is null
     * @throws ClassCastException
     *             if the specified object is not of the expected type
     */
    // Maaske egentlig drop den...
    @SuppressWarnings("unchecked")
    public @Nullable T cast(@Nullable Object obj) {
        return (T) rawType.cast(obj);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj instanceof GenericType<?> t && type.equals(t.type);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return type.hashCode();
    }

    /** {@return whether or not this generic type represents a Class instance.} */
    public final boolean isClass() {
        return type instanceof Class;
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
        return StringFormatter.format(type);
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as {@link #toString()} does.
     *
     * @return a simple string representation of this type
     */
    public final String toStringSimple() {
        return StringFormatter.formatSimple(type);
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
     * Returns a type token for the specified class.
     *
     * @param <T>
     *            the type
     * @param type
     *            the class to return a type token for
     * @return a type token for the specified class type
     */
    public static <T> GenericType<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedGenericType<>(type);
    }

    /**
     * Returns the type of the specified parameter as a type token.
     *
     * @param type
     *            the parameter to return a type token for
     * @return the type token for the parameter
     * @see Parameter#getParameterizedType()
     */
    public static GenericType<?> ofType(Type type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedGenericType<>(type);
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
    public static final class CanonicalizedGenericType<T> extends GenericType<T> {

        /**
         * Creates a new type token instance
         *
         * @param type
         *            the type
         */
        public CanonicalizedGenericType(Type type) {
            super(type);
        }
    }
}
