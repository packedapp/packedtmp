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
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.TypeLiteral.CanonicalizedTypeLiteral;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Nullable;
import packed.internal.inject.JavaXInjectSupport;

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
// Extract from TypeVariable
// of(Class)
// toKey()
public abstract class Key<T> {

    /** A cache of factories used by {@link #findInjectable(Class)}. */
    private static final ClassValue<Key<?>> TYPE_VARIABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            return fromTypeVariable((Class) implementation, Key.class, 0);
        }
    };

    /**
     * The computed hash code, not lazy because we are probably always going to use it for comparison against other keys.
     */
    private final int hash;

    /** An (optional) qualifier of this key. */
    @Nullable
    private final Annotation qualifier;

    /** The generic type of this key. */
    private final CanonicalizedTypeLiteral<T> typeLiteral;

    /** Constructs a new key. Derives the type from this class's type parameter. */
    @SuppressWarnings("unchecked")
    protected Key() {
        Key<?> key = TYPE_VARIABLE_CACHE.get(getClass());
        this.qualifier = key.qualifier;
        this.typeLiteral = (CanonicalizedTypeLiteral<T>) key.typeLiteral;
        this.hash = key.hash;
    }

    /**
     * Creates a new key.
     * 
     * @param typeLiteral
     *            the checked type literal
     * @param qualifier
     *            the (optional) qualifier
     */
    Key(CanonicalizedTypeLiteral<T> typeLiteral, @Nullable Annotation qualifier) {
        this.typeLiteral = typeLiteral;
        this.qualifier = qualifier;
        this.hash = Objects.hashCode(qualifier) ^ typeLiteral.hashCode();
    }

    /**
     * To avoid inadvertently holding on to the instance that defines an anonymous class, this method creates a new instance
     * without any reference to the instance that defined the anonymous class.
     * 
     * @return the key
     */
    final CanonicalizedKey<T> canonicalize() {
        if (getClass() == CanonicalizedKey.class) {
            return (CanonicalizedKey<T>) this;
        }
        return new CanonicalizedKey<>(typeLiteral, qualifier);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
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
    public final Optional<Annotation> getQualifier() {
        return Optional.ofNullable(qualifier);
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
        return hash;
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

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (qualifier == null) {
            return typeLiteral.toString();
        }
        return format(qualifier) + " " + typeLiteral.toString();
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as {@link #toString()} does.
     * 
     * @return a simple string
     */
    public final String toStringSimple() {
        if (qualifier == null) {
            return typeLiteral.toStringSimple();
        }
        return formatSimple(qualifier) + " " + typeLiteral.toStringSimple();
    }

    /**
     * Returns a new key that retain its type but with the specified qualifier.
     * 
     * @param qualifier
     *            the new key's qualifier
     * @return the new key
     */
    public final Key<T> withAnnotation(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new CanonicalizedKey<>(typeLiteral, qualifier);
    }

    static <T> Key<T> fromCheckedTypeAndCheckedNullableAnnotation(CanonicalizedTypeLiteral<T> typeLiteral, Annotation qualifier) {
        return new CanonicalizedKey<T>(typeLiteral, qualifier);
    }

    /**
     * Returns a key matching the return type of the method and any qualifier that may be present on the method.
     * 
     * @param method
     *            the method for whose return type to return a key for
     * @return the key matching the return type of the method
     * @throws InvalidDeclarationException
     *             if the specified method has a void return type. Or returns one of {@link Optional},
     * @see Method#getReturnType()
     * @see Method#getGenericReturnType()
     */
    // What about Nullable-> ignore it
    public static Key<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        TypeLiteral<?> tl = TypeLiteral.fromMethodReturnType(method);

        if (method.getReturnType() == void.class || method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("@Provides method " + method + " cannot have void return type");
        }
        return tl.toKey();
    }

    public static <T> Key<?> fromTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        TypeLiteral<?> t = TypeLiteral.fromTypeVariable(subClass, superClass, parameterIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) subClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[parameterIndex].getAnnotations();
        Annotation qa = JavaXInjectSupport.findQualifier(pta, annotations);

        return t.toKeyNullableAnnotation(qa);
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
        return TypeLiteral.of(type).box().toKey();
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
        return TypeLiteral.of(type).box().toKey(qualifier);
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
    static <T> Key<T> of(Type type) {
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
    static <T> Key<T> of(Type type, Annotation qualifier) {
        requireNonNull(type, "type is null");
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return (Key<T>) fromCheckedTypeAndCheckedNullableAnnotation(TypeLiteral.fromJavaImplementationType(type), qualifier);
    }

    static final class CanonicalizedKey<T> extends Key<T> {

        /**
         * Creates a new key
         * 
         * @param typeLiteral
         *            the type literal
         * 
         * @param qualifier
         *            an optional qualifier
         */
        CanonicalizedKey(CanonicalizedTypeLiteral<T> typeLiteral, @Nullable Annotation qualifier) {
            super(typeLiteral, qualifier);
        }
    }
}

//
// /**
// * Returns the type of qualifier this key have, or null if this key has no qualifier.
// *
// * @return the type of qualifier this key have, or null if this key has no qualifier
// */
// @Nullable
// public final Class<? extends Annotation> getQualifierType() {
// return qualifier == null ? null : qualifier.annotationType();
// }
// An easy way to create annotations with one value, or maybe put it on TypeLiteral
// withNamedAnnotations(type, String name, Object value)
// withNamedAnnotations(type, String name1, Object value1, String name2, Object value2)
// withNamedAnnotations(type, String name1, Object value1, String name2, Object value2, String name3, Object value3);
// public static <T> Key<T> withAnnotation(Type type, Class<? extends Annotation> cl, Object value) {
// withAnnotation(Integer.class, Named.class, "left");
// throw new UnsupportedOperationException();
// }
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
