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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import app.packed.inject.TypeLiteral.CanonicalizedTypeLiteral;
import app.packed.util.FieldDescriptor;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import app.packed.util.ParameterDescriptor;
import packed.internal.inject.JavaXInjectSupport;
import packed.internal.util.TypeUtil;

/**
 * A key defines a unique identifier for a binding in an injector. It consists of two parts: a mandatory type literal
 * and an optional annotation called a qualifier. It does so by requiring user to create a subclass of this class which
 * enables retrieval of the type information even at runtime. Some examples of non-qualified keys are:
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
 * </pre> Some examples of qualified keys are: <pre> {@code
 * Key<List<String>> list = new Key<@Name("foo") List<String>>() {};
 * Key<List<String>> list = new Key<@Name List<String>>() {}; //uses default value}
 * </pre>
 * 
 * In order for a key to be valid, it must:
 * <ul>
 * <li><b>Not be an optional type.</b> The key cannot be of type {@link Optional}, {@link OptionalInt},
 * {@link OptionalLong} or {@link OptionalDouble} as they are reserved.</li>
 * <li><b>Have 0 or 1 qualifier.</b> A valid key cannot have more than 1 annotations whose type is annotated with
 * {@link Qualifier}</li>
 * </ul>
 * Furthermore, keys do <b>not</b> differentiate between primitive types (long, double, etc.) and their corresponding
 * wrapper types (Long, Double, etc.). Primitive types will be replaced with their wrapper types when keys are created.
 * This means that, for example, {@code Key.of(int.class) is equivalent to Key.of(Integer.class)}.
 */
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
    /** A cache of factories used by {@link #findInjectable(Class)}. */
    private static final ClassValue<Key<?>> CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            return TypeLiteral.of(implementation).box().toKey();
        }
    };
    /**
     * The computed hash code, not lazy because we are probably always going to use it for comparison against other keys.
     */
    private final int hash;

    /** An (optional) qualifier for this key. */
    @Nullable
    private final Annotation qualifier;

    /** The generic type for this key. */
    private final CanonicalizedTypeLiteral<T> typeLiteral;

    /** Constructs a new key. Derives the type from this class's type parameter. */
    @SuppressWarnings("unchecked")
    protected Key() {
        Key<?> key = TYPE_VARIABLE_CACHE.get(getClass());
        this.qualifier = key.qualifier;
        this.typeLiteral = (CanonicalizedTypeLiteral<T>) key.typeLiteral;
        this.hash = key.hash;
        assert (!typeLiteral.getRawType().isPrimitive());
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
        this.hash = typeLiteral.hashCode() ^ Objects.hashCode(qualifier);
        assert (!typeLiteral.getRawType().isPrimitive());
    }

    /**
     * To avoid accidentally holding on to any instance that defines this key as an anonymous class. This method creates a
     * new key instance without any reference to the instance that defined the anonymous class.
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
     * Returns any qualifier this key might have, or an empty optional if this key has no qualifier.
     *
     * @return any qualifier this key might have, or an empty optional if this key has no qualifier
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
     * Returns a key with no qualifier but retaining this key's type. If this key has no qualifier
     * ({@code hasQualifier == null}), returns this key.
     * 
     * @return the key with no qualifier
     */
    public final Key<T> withNoQualifier() {
        return qualifier == null ? this : new CanonicalizedKey<>(typeLiteral, null);
    }

    /**
     * Returns a new key retaining its original type but with the specified qualifier.
     * 
     * @param qualifier
     *            the new key's qualifier
     * @return the new key
     * @throws InvalidDeclarationException
     *             if the specified annotation is not annotation with {@link Qualifier}.
     */
    public final Key<T> withQualifier(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);// qualifierType instead??
        return new CanonicalizedKey<>(typeLiteral, qualifier);
    }

    /**
     * Returns a new key retaining its original type but with a qualifier of the specified type iff the specified qualifier
     * type has default values for every attribute.
     *
     * @param qualifierType
     *            the type of qualifier for the new key
     * @return the new key
     * @throws IllegalArgumentException
     *             if the specified qualifier type does not have default values for every attribute
     * @throws InvalidDeclarationException
     *             if the specified qualifier type is not annotation with {@link Qualifier}.
     */
    public final Key<T> withQualifier(Class<? extends Annotation> qualifierType) {
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifierType);
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a key matching the type of the specified field and any qualifier that may be present on the field.
     * 
     * @param field
     *            the field to return a key for
     * @return a key matching the type of the field and any qualifier that may be present on the field
     * @throws InvalidDeclarationException
     *             if the type is an optional type such as {@link Optional} or {@link OptionalInt}. Or if there are more
     *             than 1 qualifier present on the field
     * @see Field#getType()
     * @see Field#getGenericType()
     */
    public static Key<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        TypeLiteral<?> tl = TypeLiteral.fromField(field).box();
        Annotation annotation = JavaXInjectSupport.findQualifier(field, field.getAnnotations());
        return fromTypeLiteralNullableAnnotation(field, tl, annotation);
    }

    /**
     * Returns a key matching the return type of the specified method and any qualifier that may be present on the method.
     * 
     * @param method
     *            the method for to return a key for
     * @return the key matching the return type of the method and any qualifier that may be present on the method
     * @throws InvalidDeclarationException
     *             if the specified method has a void return type. Or returns an optional type such as {@link Optional} or
     *             {@link OptionalInt}. Or if there are more than 1 qualifier present on the method
     * @see Method#getReturnType()
     * @see Method#getGenericReturnType()
     */
    public static Key<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        if (method.getReturnType() == void.class) {
            throw new InvalidDeclarationException("@Provides method " + method + " cannot have void return type");
        }
        TypeLiteral<?> tl = TypeLiteral.fromMethodReturnType(method).box();
        Annotation annotation = JavaXInjectSupport.findQualifier(method, method.getAnnotations());
        return fromTypeLiteralNullableAnnotation(method, tl, annotation);
    }

    static <T> Key<T> fromTypeLiteral(TypeLiteral<T> typeLiteral) {
        return fromTypeLiteralNullableAnnotation(typeLiteral, typeLiteral, null);
    }

    /**
     * Returns a key with the specified qualifier and the same type as this instance.
     * 
     * @param typeLiteral
     *            the typeLiteral of the new
     * @param qualifier
     *            the qualifier of the new
     * @return a key with the specified qualifier and the same type as this instance
     * @throws InvalidDeclarationException
     *             if the type literal could not be converted to a key, for example, if it is an {@link Optional}. Or if the
     *             qualifier type is not annotated with {@link Qualifier}.
     */
    static <T> Key<T> fromTypeLiteral(TypeLiteral<T> typeLiteral, Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return fromTypeLiteralNullableAnnotation(typeLiteral, typeLiteral, qualifier);
    }

    public static <T> Key<T> fromTypeLiteralNullableAnnotation(Object source, TypeLiteral<T> typeLiteral, @Nullable Annotation qualifier) {
        requireNonNull(typeLiteral, "typeLiteral is null");
        // From field, fromTypeLiteral, from Variable, from class, arghhh....
        assert (source instanceof Field || source instanceof Method || source instanceof ParameterDescriptor || source instanceof FieldDescriptor
                || source instanceof MethodDescriptor || source instanceof TypeLiteral || source instanceof Class);

        typeLiteral = typeLiteral.box();
        if (TypeUtil.isOptionalType(typeLiteral.getRawType())) {
            throw new InvalidDeclarationException(
                    "Cannot convert an optional type (" + typeLiteral.toStringSimple() + ") to a Key, as keys cannot be optional");
        } else if (!TypeUtil.isFreeFromTypeVariables(typeLiteral.getType())) {
            throw new InvalidDeclarationException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<"
                    + typeLiteral.toStringSimple() + "> defined: " + TypeUtil.findTypeVariableNames(typeLiteral.getType()));
        }
        return new CanonicalizedKey<T>(typeLiteral.canonicalize(), qualifier);
    }

    public static <T> Key<?> fromTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        TypeLiteral<?> t = TypeLiteral.fromTypeVariable(subClass, superClass, parameterIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) subClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[parameterIndex].getAnnotations();
        Annotation qa = JavaXInjectSupport.findQualifier(pta, annotations);
        return Key.fromTypeLiteralNullableAnnotation(superClass, t, qa);
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
    // TODO rename type to key?????
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> type) {
        requireNonNull(type, "type is null");
        return (Key<T>) CLASS_CACHE.get(type);
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

    /** See {@link CanonicalizedTypeLiteral}. */
    static final class CanonicalizedKey<T> extends Key<T> {

        /**
         * Creates a new key
         * 
         * @param typeLiteral
         *            the type literal
         * @param qualifier
         *            a nullable qualifier
         */
        CanonicalizedKey(CanonicalizedTypeLiteral<T> typeLiteral, @Nullable Annotation qualifier) {
            super(typeLiteral, qualifier);
        }
    }
}

// Maybe have an internal KeyFactory..... That makes proper error Messages

// Extract key
// Men vi skal jo have informationer om hvorfor

// Saa metoder ved hvorfor, the caller knows where/what
// WHERE/What could not because of why...
// Maybe have a isValidKey(Type) or <T> checkValidKey(T extends RuntimeException, String message) throws T;
// Maybe have a string with "%s, %s".. Maybe A consumer with the message because XYZ
// because it "xxxxxx"

/// **
// * Returns a key matching the specified type.
// *
// * @param <T>
// * the type to return a key for
// * @param type
// * the type to return a key for
// * @return a key matching the specified type
// */
// @SuppressWarnings({ "unchecked" })
// static <T> Key<T> of(Type type) {
// requireNonNull(type, "type is null");
// if (type instanceof Class) {
// return of((Class<T>) type);
// }
// return (Key<T>) fromCheckedTypeAndCheckedNullableAnnotation(new CanonicalizedTypeLiteral<>(type), null);
// }
//
/// **
// * Returns a key matching the specified type and qualifier.
// *
// * @param <T>
// * the type to return a key for
// * @param type
// * the type to return a key for
// * @param qualifier
// * the qualifier of the key
// * @return a key matching the specified type and qualifier
// */
// @SuppressWarnings({ "unchecked" })
// static <T> Key<T> of(Type type, Annotation qualifier) {
// requireNonNull(type, "type is null");
// requireNonNull(qualifier, "qualifier is null");
// JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
// return (Key<T>) fromCheckedTypeAndCheckedNullableAnnotation(new CanonicalizedTypeLiteral<>(type), qualifier);
// }

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
