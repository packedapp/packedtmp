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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import packed.inject.JavaXInjectSupport;
import packed.util.ClassUtil;
import packed.util.GenericsUtil;
import packed.util.TypeUtil;

/**
 * A TypeLiteral represents a generic type {@code T}. This class is used to work around the limitation that Java does
 * not provide a way to represent generic types. It does so by requiring user to create a subclass of this class which
 * enables retrieval of the type information even at runtime. Usage:
 *
 * <pre> {@code
 * TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}
 * TypeLiteral<Map<Integer, List<Integer>>> list = new TypeLiteral<>() {};}
 * </pre>
 */
// TODO test this from other packages....
// concrete class, public constructor -> People can instantiate
// concrete class, protected constructor
// abstract class, public constructor
// abstract class, protected constructor
public class TypeLiteral<T> extends TypeLiteralOrKey<T> {

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
     * Constructs a new type literal by deriving the actual type from the type parameter.
     * 
     * @throws IllegalArgumentException
     *             if the type parameter could not decided
     */
    @SuppressWarnings("unchecked")
    protected TypeLiteral() {
        this.type = (Type) GenericsUtil.getTypeOfArgumentX(TypeLiteral.class, 0, getClass());
        this.rawType = (Class<? super T>) TypeUtil.findRawType(type);
    }

    /**
     * Constructs a type literal from a specific type.
     * 
     * @param type
     *            the type to create a type literal from
     */
    @SuppressWarnings("unchecked")
    TypeLiteral(Type type) {
        this.type = requireNonNull(type, "type is null");
        this.rawType = (Class<? super T>) TypeUtil.findRawType(this.type);
    }

    /**
     * If this type literal is a private type, returns the boxed version. Otherwise returns this.
     * 
     * @return if this type literal is a primitive returns the boxed version, otherwise returns this
     */
    public final TypeLiteral<T> box() {
        if (getRawType().isPrimitive()) {
          return new TypeLiteral<>(ClassUtil.boxClass(getRawType()));
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
    @Override
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

    /** {@inheritDoc} */
    @Override
    public final Key<T> toKey() {
        return new Key<T>(null, this) {};
    }

    public final Key<T> toKey(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key<T>(qualifier, this) {};
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
     * Returns a type literal from the specified class.
     *
     * @param type
     *            the class instance to return a type literal for
     * @return a type literal from the specified class
     */
    public static <T> TypeLiteral<T> of(Class<T> type) {
        return new TypeLiteral<>(type);
    }

    /**
     * Returns a type literal from the specified type.
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
    static TypeLiteral<?> of(Type type) {
        return new TypeLiteral<>(type);
    }
}
