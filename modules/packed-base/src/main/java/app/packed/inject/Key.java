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
import static packed.util.Formatter.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import packed.inject.JavaXInjectSupport;
import packed.util.GenericsUtil;
import packed.util.TypeUtil;

/**
 * <ul>
 * <li><b>Not an optional type.<b> The key cannot be of type {@link Optional}, {@link OptionalInt}, {@link OptionalLong}
 * or {@link OptionalDouble} as they are reserved.</li>
 * <li><b>0 or 1 qualifier.<b> A valid key cannot have more than 1 annotations whose type is annotated with
 * {@link Qualifier}</li>
 * <li></li>
 * </ul>
 * Furthermore, keys do <b>not</b> differentiate between primitive types (long, double, etc.) and their corresponding
 * wrapper types (Long, Double, etc.). Primitive types will be replaced with their wrapper types when keys are created.
 * This means that, for example, {@code Key.of(int.class) equals Key.of(Integer.class)}.
 */
public abstract class Key<T> extends TypeLiteralOrKey<T> {

    /** The lazily computed hash code. */
    private int hash;

    /** An (optional) qualifier of this key. */
    private final Annotation qualifier;

    /** The generic type of this key. */
    private final TypeLiteral<T> typeLiteral;

    /**
     * Constructs a new key. Derives the type from this class's type parameter.
     */
    protected Key() {
        Type tt = (Type) GenericsUtil.getTypeOfArgumentX(Key.class, 0, getClass());
        typeLiteral = new TypeLiteral<>(tt);
        if (TypeUtil.isOptionalType(typeLiteral.getRawType())) {
            // cannot be parameterized with Optional
            throw new IllegalArgumentException();
        }

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) getClass().getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[0].getAnnotations();
        Annotation qa = null;
        if (annotations.length > 0) {
            for (Annotation a : annotations) {
                if (JavaXInjectSupport.isQualifierAnnotationPresent(a.annotationType())) {
                    if (qa != null) {
                        throw new IllegalArgumentException("More than 1 qualifier on " + getClass());
                    }
                    qa = a;
                }
            }
        }
        this.qualifier = qa;
    }

    /**
     * Creates a new key.
     * 
     * @param qualifier
     *            the qualifier
     * @param typeLiteral
     *            the generic type
     */
    Key(Annotation qualifier, TypeLiteral<T> typeLiteral) {
        this.qualifier = qualifier;
        this.typeLiteral = requireNonNull(typeLiteral, "typeLiteral is null");
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Key)) {
            return false;
        }
        Key<?> other = (Key<?>) obj;
        return Objects.equals(qualifier, other.qualifier) && typeLiteral.equals(other.typeLiteral);
    }

    /**
     * Returns any qualifier this key might have, or null if this key has no qualifier.
     *
     * @return any qualifier this key might have, or null if this key has no qualifier
     */
    public final Annotation getQualifier() {
        return qualifier;
    }

    /**
     * Returns the type of qualifier this key have, or null if this key has no qualifier.
     *
     * @return the type of qualifier this key have, or null if this key has no qualifier
     */
    public final Class<? extends Annotation> getQualifierType() {
        return qualifier == null ? null : qualifier.annotationType();
    }

    @Override
    public Class<? super T> getRawType() {
        return typeLiteral.getRawType();
    }

    /** Returns the genetic type of this key. */
    public final TypeLiteral<T> getTypeLiteral() {
        return typeLiteral;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        int h = hash;
        if (h != 0) {
            return h;
        }
        return hash = Objects.hashCode(qualifier) ^ typeLiteral.hashCode();
    }

    /**
     * Returns whether or not this key has a qualifier.
     * 
     * @return whether or not this key has a qualifier
     */
    public final boolean hasQualifier() {
        return qualifier != null;
    }

    @Override
    public Key<T> toKey() {
        return this;
    }

    public final String toShortString() {
        return "foo";
    }

    @Override
    public final String toString() {
        return toString(true);
    }

    final String toString(boolean appendKey) {
        StringBuilder sb = new StringBuilder();
        if (appendKey) {
            sb.append("Key<");
        }
        if (qualifier != null) {
            Class<? extends Annotation> annotationType = qualifier.annotationType();
            if (annotationType != null) {
                String shortDescription = qualifier.toString().replace(annotationType.getPackageName() + ".", "");
                sb.append(shortDescription);
                // sb.append("@");
                // sb.append(annotationType.getSimpleName());
                // System.out.println(Annotations.nameOf(this));
                // System.out.println(qualifier);
                sb.append(" ");
            }
        }
        sb.append(typeLiteral);
        if (appendKey) {
            sb.append('>');
        }
        return sb.toString();
    }

    public final String toStringNoKey() {
        return toString(false);
    }

    public static <T> Key<T> getKeyOfArgument(Class<T> superClass, int parameterIndex, Class<? extends T> subClass) {
        TypeLiteral<T> t = GenericsUtil.getTypeOfArgument(superClass, parameterIndex, subClass);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) subClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[parameterIndex].getAnnotations();
        Annotation qa = null;
        if (annotations.length > 0) {
            for (Annotation a : annotations) {
                if (JavaXInjectSupport.isQualifierAnnotationPresent(a.annotationType())) {
                    if (qa != null) {
                        throw new IllegalArgumentException("More than 1 qualifier on " + format(subClass));
                    }
                    qa = a;
                }
            }
        }
        return qa == null ? t.toKey() : t.toKey(qa);
    }

    static Key<?> internalOf(Type type, Annotation optionalQualifier) {
        if (optionalQualifier == null) {
            return of(type);
        }
        return of(type, optionalQualifier);
    }

    /**
     * Returns a key matching the specified type.
     *
     * @param type
     *            the type to return a key for
     * @return a key matching the specified type
     */
    public static <T> Key<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return new Key<>(null, TypeLiteral.of(type)) {};
    }

    /**
     * Returns a key matching the specified type and qualifier.
     *
     * @param type
     *            the type to return a key for
     * @param qualifier
     *            the qualifier of the key
     * @return a key matching the specified type and qualifier
     */
    public static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        Key<T> withAnnotation = of(type);
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key<>(qualifier, withAnnotation.typeLiteral) {};
    }

    /**
     * Returns a key matching the specified type.
     *
     * @param type
     *            the type to return a key for
     * @return a key matching the specified type
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Key<T> of(Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class) {
            return of((Class<T>) type);
        }
        return new Key(null, TypeLiteral.of(type)) {};
    }

    /**
     * Returns a key matching the specified type and qualifier.
     *
     * @param type
     *            the type to return a key for
     * @param qualifier
     *            the qualifier of the key
     * @return a key matching the specified type and qualifier
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T> Key<T> of(Type type, Annotation qualifier) {
        requireNonNull(type, "type is null");
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key(qualifier, TypeLiteral.of(type)) {};
    }
}
