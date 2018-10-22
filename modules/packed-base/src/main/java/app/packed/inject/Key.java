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

import packed.inject.JavaXInjectSupport;
import packed.util.ClassUtil;
import packed.util.Types;

/**
 *
 *
 * Unlike TypeLiteral, keys do <b>not</b> differentiate between primitive types (long, double, etc.) and their
 * corresponding wrapper types (Long, Double, etc.). Primitive types will be replaced with their wrapper types
 * when keys are created. This means that, for example, {@code Key.of(int.class) equals Key.of(Integer.class)}
 */
public abstract class Key<T> extends TypeLiteralOrKey<T> {
    
    /** A cache of keys created from a {@link Class}. */
    private static final ClassValue<Key<?>> CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> type) {
            return new Key<>(type, null, null) {};
        }
    };

    /** The precomputed hash code. */
    private final int hashCode;

    /** Any qualifier that might be present on the key. */
    private final Annotation qualifier;

    /** The (lazily computed) string representation of this key. */
    private String toString;

    private final TypeLiteral<T> typeLiteral;

    final Class<?> wildcardAnnotation;

    /**
     * Constructs a new key. Derives the type from this class's type parameter.
     *
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type parameter in the anonymous class's type
     * hierarchy so we can reconstitute it at runtime despite erasure.
     *
     * <p>
     * Example usage for a binding of type {@code Foo}:
     *
     * <p>
     * {@code new Key<Foo>() {}}.
     */
    @SuppressWarnings("unchecked")
    protected Key() {
        // Get Type Literal
        TypeLiteral<?> t = TypeLiteral.of(Types.getSuperclassTypeParameter(getClass()));
        this.typeLiteral = Types.canonicalizeForKey((TypeLiteral<T>) t);

        // TODO check not Optional, OptionalLong, OptionalDouble, OptionalInteger

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

        // AnnotatedType[] annotatedActualTypeArguments = pta.getAnnotatedActualTypeArguments();

        this.hashCode = computeHashCode();
        this.wildcardAnnotation = null;
    }

    /** Unsafe. Constructs a key from a manually specified type. */
    @SuppressWarnings("unchecked")
    Key(Type type, Annotation qualifier, Class<? extends Annotation> wildcardAnnotation) {
        this.qualifier = qualifier;
        this.typeLiteral = Types.canonicalizeForKey((TypeLiteral<T>) TypeLiteral.of(type));
        this.hashCode = computeHashCode();
        this.wildcardAnnotation = wildcardAnnotation;
    }

    /** Constructs a key from a manually specified type. */
    Key(TypeLiteral<T> typeLiteral, Annotation qualifier) {
        this.qualifier = qualifier;
        this.typeLiteral = Types.canonicalizeForKey(typeLiteral);
        this.hashCode = computeHashCode();
        this.wildcardAnnotation = null;
    }

    Key(Annotation qualifier, TypeLiteral<T> typeLiteral) {
        this.qualifier = qualifier;
        this.typeLiteral = typeLiteral;
        this.hashCode = computeHashCode();
        this.wildcardAnnotation = null;
    }

    /** Computes the hash code for this key. */
    private int computeHashCode() {
        return (typeLiteral.hashCode() * 31 + Objects.hashCode(qualifier)) * 31 + Objects.hashCode(wildcardAnnotation);
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (!(obj instanceof Key<?>)) {
            return false;
        }
        Key<?> other = (Key<?>) obj;
        return Objects.equals(qualifier, other.qualifier) && Objects.equals(wildcardAnnotation, other.wildcardAnnotation)
                && typeLiteral.equals(other.typeLiteral);
    }

    public <S> Key<?> fromTypeVariable(Class<S> baseClass, Class<? extends S> actualClass, int baseTypeVariableIndex) {
        Type t = GenericsUtil.getTypeOfArgument(baseClass, actualClass, baseTypeVariableIndex);
        // Check not Optional?????
        // TODO check that there are no qualifier annotations on the type.
        return Key.of(t);
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
     * Returns the type of any qualifier this key might have, or null if this key has no qualifier.
     *
     * @return the type of any qualifier this key might have, or null if this key has no qualifier
     */
    public final Class<? extends Annotation> getQualifierType() {
        return qualifier == null ? null : qualifier.annotationType();
    }

    @Override
    public Class<? super T> getRawType() {
        return typeLiteral.getRawType();
    }

    /** Gets the key type. */
    public final TypeLiteral<T> getTypeLiteral() {
        return typeLiteral;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return hashCode;
    }

    public boolean isWildcard() {
        return wildcardAnnotation != null;
    }

    boolean matches(Class<?> other) {
        return matches(Key.of(other));
    }

    boolean matches(Key<?> other) {
        // The name needs to be pretty bong on... So we do not end in a situation like Class.isAssignableFrom

        // bindableFrom <- Still needs to think
        return true;
    }

    @Override
    public Key<T> toKey() {
        return this;
    }

    @Override
    public final String toString() {
        return toString(true);
    }

    public final String toStringNoKey() {
        return toString(false);
    }

    final String toString(boolean appendKey) {
        String toString = this.toString;
        if (toString == null) {
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
            return this.toString = sb.toString();
        }
        return toString;
    }

    public static <T> Key<T> getKeyOfArgument(Class<T> superClass, int parameterIndex, Class<? extends T> subClass) {
        TypeLiteral<T> t = TypeLiteral.getTypeOfArgument(superClass, parameterIndex, subClass);

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
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return (Key<T>) CACHE.get(ClassUtil.boxClass(type));
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
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class) {
            return of((Class<T>) type);
        }
        return new Key<T>(type, null, null) {};
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
    public static <T> Key<T> of(Type type, Annotation qualifier) {
        requireNonNull(type, "type is null");
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key<T>(type, qualifier, null) {};
    }

    public static Key<Object> ofWildcardAnnotation(Class<? extends Annotation> wildcardAnnotationType) {
        throw new UnsupportedOperationException();
    }
}
