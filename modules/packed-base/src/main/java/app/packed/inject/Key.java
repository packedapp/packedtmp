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
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import packed.inject.JavaXInjectSupport;
import packed.util.TypeUtil;
import packed.util.TypeVariableExtractorUtil;

/**
 * A key is unique identifier for a binding in an injector. It consists of two parts: a mandatory type literal and an
 * optional annotation called a qualifier. It does so by requiring user to create a subclass of this class which enables
 * retrieval of the type information even at runtime. Some examples of non qualified keys are:
 *
 * <pre> {@code
 * Key<List<String>> list = new Key<List<String>>() {};
 * Key<Map<Integer, List<Integer>>> list = new TypeLiteral<>() {};}
 * </pre>
 * 
 * Given a custom defined qualifier: <pre> {@code
 * &#64;Qualifier
 * public @interface Name {
 *    String value() default "noname";
 * }}
 * </pre> Some examples of qualified keys <pre> {@code
 * Key<List<String>> list = new Key<@Name("foo") List<String>>() {};
 * Key<List<String>> list = new Key<@Name List<String>>() {}; //uses default value}
 * </pre>
 * 
 * In order for a key to be valid, it must:
 * <ul>
 * <li><b>Not an optional type.</b> The key cannot be of type {@link Optional}, {@link OptionalInt},
 * {@link OptionalLong} or {@link OptionalDouble} as they are reserved.</li>
 * <li><b>Have 0 or 1 qualifier.</b> A valid key cannot have more than 1 annotations whose type is annotated with
 * {@link Qualifier}</li>
 * </ul>
 * Furthermore, keys do <b>not</b> differentiate between primitive types (long, double, etc.) and their corresponding
 * wrapper types (Long, Double, etc.). Primitive types will be replaced with their wrapper types when keys are created.
 * This means that, for example, {@code Key.of(int.class) equals Key.of(Integer.class)}.
 */

// Create a valid key for @Provides
// Extract
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
    @SuppressWarnings("unchecked")
    protected Key() {
        Type tt = (Type) TypeVariableExtractorUtil.findTypeArgument(Key.class, 0, getClass());
        typeLiteral = (TypeLiteral<T>) TypeLiteral.fromJavaImplementationType(tt);
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
                        throw new IllegalArgumentException("A key cannot define more than 1 qualifier, however '" + getClass() + "' defined multiple: "
                                + JavaXInjectSupport.getAllQualifierAnnotationPresent(pta.getAnnotatedActualTypeArguments()[0]));
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
     * @param typeLiteral
     *            the checked type literal
     * @param qualifier
     *            the (optional) qualifier
     */
    private Key(TypeLiteral<T> typeLiteral, Annotation qualifier) {
        this.typeLiteral = requireNonNull(typeLiteral, "typeLiteral is null");
        this.qualifier = qualifier;
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

    /** {@inheritDoc} */
    @Override
    public final Class<? super T> getRawType() {
        return typeLiteral.getRawType();
    }

    /** Returns the generic type of this key. */
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
    public final Key<T> toKey() {
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

    // An easy way to create annotations with one value
    // withNamedAnnotations(type, String name, Object value)
    // withNamedAnnotations(type, String name1, Object value1, String name2, Object value2)
    // withNamedAnnotations(type, String name1, Object value1, String name2, Object value2, String name3, Object value3);
    // public static <T> Key<T> withAnnotation(Type type, Class<? extends Annotation> cl, Object value) {
    // withAnnotation(Integer.class, Named.class, "left");
    // throw new UnsupportedOperationException();
    // }

    public final String toStringNoKey() {
        return toString(false);
    }

    public static <T> Key<T> getKeyOfArgument(Class<T> superClass, int parameterIndex, Class<? extends T> subClass) {
        TypeLiteral<T> t = TypeLiteral.fromTypeVariable(superClass, parameterIndex, subClass);

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
        return TypeLiteral.of(type).toKey();
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
        requireNonNull(type, "type is null");
        return TypeLiteral.of(type).toKey(qualifier);
    }

    /**
     * Returns a key matching the specified type.
     *
     * @param type
     *            the type to return a key for
     * @return a key matching the specified type
     */
    @SuppressWarnings({ "unchecked" })
    public static <T> Key<T> of(Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class) {
            return of((Class<T>) type);
        }
        return (Key<T>) fromCheckedTypeAndCheckedNullableAnnotation(TypeLiteral.fromJavaImplementationType(type), null);
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
    @SuppressWarnings({ "unchecked" })
    public static <T> Key<T> of(Type type, Annotation qualifier) {
        requireNonNull(type, "type is null");
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return (Key<T>) fromCheckedTypeAndCheckedNullableAnnotation(TypeLiteral.fromJavaImplementationType(type), qualifier);
    }

    static <T> Key<T> fromCheckedTypeAndCheckedNullableAnnotation(TypeLiteral<T> typeLiteral, Annotation qualifier) {
        return new Key<T>(typeLiteral, qualifier) {};
    }

    /**
     * Returns the type of the specified field as a key.
     * 
     * @param field
     *            the field to return a type literal for
     * @return the type literal for the field
     * @see Field#getGenericType()
     */
    public static Key<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        throw new UnsupportedOperationException();// Removes optional
    }

    /**
     * Returns a new key that maintains its type but with {@link #getQualifier()} returning the specified qualifier
     * 
     * @param qualifier
     *            the new key's qualifier
     * @return the new key
     */
    // Nullable qualifier???
    public Key<T> withAnnotation(Annotation qualifier) {
        return null;
    }
}
