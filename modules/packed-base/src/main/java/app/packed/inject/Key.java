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
import static packed.internal.util.StringFormatter.format;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.util.Nullable;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.AnnotationUtil;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;

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
public abstract class Key<T> {

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
        Type tt = TypeVariableExtractorUtil.findTypeParameterFromSuperClass(getClass(), Key.class, 0);
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
    @Nullable
    public final Annotation getQualifier() {
        return qualifier;
    }

    /**
     * Returns the type of qualifier this key have, or null if this key has no qualifier.
     *
     * @return the type of qualifier this key have, or null if this key has no qualifier
     */
    @Nullable
    public final Class<? extends Annotation> getQualifierType() {
        return qualifier == null ? null : qualifier.annotationType();
    }

    public final Class<? super T> getRawType() {
        return typeLiteral.getRawType();
    }

    /**
     * Returns the generic type of this key.
     * 
     * @return the generic type of this key
     */
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

    /**
     * Returns whether or not this key has a qualifier of the specified type.
     * 
     * @param qualifierType
     *            the type of qualifier
     * @return whether or not this key has a qualifier of the specified type
     */
    public final boolean isQualifiedWith(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType is null");
        return qualifier != null && qualifier.annotationType() == qualifierType;
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
            sb.append(AnnotationUtil.toShortString(qualifier)).append(" ");
        }
        sb.append(typeLiteral.toShortString());
        if (appendKey) {
            sb.append('>');
        }
        return sb.toString();
    }

    // An easy way to create annotations with one value, or maybe put it on TypeLiteral
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
        TypeLiteral<T> t = TypeLiteral.fromTypeVariable(subClass, superClass, parameterIndex);

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
     * Returns a key matching the specified type with no qualifiers.
     *
     * @param <T>
     *            the type to return a key for
     * @param type
     *            the type to return a key for
     * @return a key matching the specified type with no qualifiers
     */
    public static <T> Key<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return TypeLiteral.of(type).toKey();
    }

    /**
     * Returns a key of the specified type and with the specified qualifier.
     *
     * @param <T>
     *            the type to return a key for
     * @param type
     *            the type to return a key for
     * @param qualifier
     *            the qualifier of the key
     * @return a key of the specified type wuth the specified qualifier
     */
    public static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        requireNonNull(type, "type is null");
        return TypeLiteral.of(type).toKey(qualifier);
    }

    /**
     * Returns a key matching the specified type.
     *
     * @param <T>
     *            the type to return a key for
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
     * @param <T>
     *            the type to return a key for
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

    public static Key<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        TypeLiteral<?> tl = TypeLiteral.fromMethodReturnType(method);

        if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("@Provides method " + method + " cannot have void return type");
        }
        return tl.toKey();
    }

    /**
     * Returns a new key that maintains its type but with {@link #getQualifier()} returning the specified qualifier
     * 
     * @param qualifier
     *            the new key's qualifier
     * @return the new key
     */
    // Nullable qualifier???
    public final Key<T> withAnnotation(Annotation qualifier) {
        return null;
    }
}

// private Class<?> getReturnTypeIgnoringOptionalBoxed() {
// InternalMethodDescriptor method = this.descriptor();
// Class<?> type = method.getReturnType();
// if (type == Optional.class) {
// return (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
// } else if (type == OptionalLong.class) {
// return Long.class;
// } else if (type == OptionalInt.class) {
// return Integer.class;
// } else if (type == OptionalDouble.class) {
// return Double.class;
// } else {
// return TypeUtil.boxClass(type);
// }
// }

//// @Provides method cannot have void return type.
// if (descriptor().getReturnType() == void.class) {
// throw new IllegalArgumentException("@Provides method " + description + " cannot have void return type");
// }
//
//// TODO check not reserved return types
//
//// TODO check return type is not optional
//// Or maybe they can.
//// If a Provides wants to provide null to someone the return type of the method should be Optional<XXXXX>
//// Null indicates look in next injector...
//
/// ****************** SERVICEKEY ******************/
//
//// Optional<Annotation> qualifier = method.findQualifiedAnnotation();
////
//// Class<? extends Annotation> qualifierAttribute = p.wildcardQualifier() == Qualifier.class ? null :
//// p.wildcardQualifier();
//// // Make we do not both have a qualifying annotation and qualifying attribute
//// if (qualifier.isPresent() && qualifierAttribute != null) {
//// throw new InjectionException(provideHasBothQualifierAnnotationAndQualifierAttribute(this, qualifier.get(),
//// qualifierAttribute));
//// }
// if (qualifierAttribute != null) {
// AnnotationUtil.validateRuntimeRetentionPolicy(qualifierAttribute);
// JavaXInjectSupport.checkQualifierAnnotationPresent(qualifierAttribute);
// // ????? Here we previously used a type
// // TODO fik support for wildcards...
// // key = Key.ofWildcardAnnotation(qualifierAttribute);
// throw new UnsupportedOperationException("Wilcard annotation not currently supported");
// // key = Key.of(method.getReturnTypeIgnoringOptionalBoxed(), qualifier.getAnnotation());
// } else if (qualifier.isPresent()) {
// key = Key.of(getReturnTypeIgnoringOptionalBoxed(), qualifier.get());
// } else {
// key = Key.of(getReturnTypeIgnoringOptionalBoxed());
// }
//
//// After we have specific bindings it should not be a problem
//// if (qualifier == null && getReturnTypeIgnoringOptionalBoxed() == Object.class) {
//// throw new IllegalArgumentException("The return type of " + this + " cannot be Object, unless a qualifying
//// annotation
//// is specified");
//// }
////
//// Well if we support chaining it could
//// if (qualifier == Name.class) {
//// throw new IllegalArgumentException("The annotation type cannot be @" + Name.class.getSimpleName() + ", as it is
//// reserved for other use");
//// }
