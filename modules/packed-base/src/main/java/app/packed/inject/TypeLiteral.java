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
import packed.util.GenericsUtil;
import packed.util.TypeUtil;

/**
 * A TypeLiteral represents a generic type {@code T}.
 * 
 * 
 * 
 * Supports inline instantiation of objects that represent parameterized types with actual type parameters.
 *
 * An object that represents any parameterized type may be obtained by subclassing TypeLiteral.
 *
 * <pre> {@code
 * TypeLiteral<Integer> list = new TypeLiteral<Integer>() {};
 * TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}
 * </pre>
 */
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
     * Constructs a new type literal. Derives represented class from type parameter.
     *
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type parameter in the anonymous class's type
     * hierarchy so we can reconstitute it at runtime despite erasure.
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
    private TypeLiteral(Type type) {
        this.type = requireNonNull(type, "type is null");
        this.rawType = (Class<? super T>) TypeUtil.findRawType(this.type);
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
        return new Key<T>(this, null) {};
    }

    public final Key<T> toKey(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key<T>(this, qualifier) {};
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
     * @see #of(Type)
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
     *          is calculated. As a result we need to transform them both into instances of the same
     *          InternalParameterizedType.
     * @param type
     *            the class instance to return a type literal for
     * @return a type literal from the specified type
     * @see #of(Class)
     */
    static TypeLiteral<?> of(Type type) {
        return new TypeLiteral<>(type);
    }
}
