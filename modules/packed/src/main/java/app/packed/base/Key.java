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
package app.packed.base;

import static java.util.Objects.requireNonNull;
import static packed.internal.util.StringFormatter.format;
import static packed.internal.util.StringFormatter.formatSimple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import app.packed.base.TypeToken.CanonicalizedTypeToken;
import packed.internal.util.AnnotationUtil;
import packed.internal.util.ClassUtil;
import packed.internal.util.QualifierUtil;
import packed.internal.util.TypeUtil;

/**
 * A key defines a unique identifier with two parts: a mandatory type literal and an optional annotation called a
 * qualifier. It does so by requiring users to create a subclass of this class which enables retrieval of the type
 * information even at runtime. Some examples of non-qualified keys are:
 *
 * <pre> {@code
 * Key<List<String>> list = new Key<List<String>>() {};
 * Key<Map<Integer, List<Integer>>> list = new Key<>() {};}
 * </pre>
 * 
 * Given a custom defined qualifier: <pre> {@code
 * &#64;Qualifier
 * public @interface Name {
 *    String value() default "noname";
 * }}
 * </pre> Some examples of qualified keys: <pre> {@code
 * Key<List<String>> list = new Key<@Named("foo") List<String>>() {};
 * Key<List<String>> list = new Key<@Named List<String>>() {}; //uses default value}
 * </pre>
 * 
 * In order for a key to be valid, it must:
 * <ul>
 * <li><b>Not be an optional type.</b> The key cannot be of type {@link Optional}, {@link OptionalInt},
 * {@link OptionalLong} or {@link OptionalDouble} as they are a reserved type.</li>
 * <li><b>Have none or a single qualifier.</b> A valid key cannot have more than 1 annotations whose type is annotated
 * with {@link Qualifier}</li>
 * </ul>
 * <p>
 * Keys do <b>not</b> differentiate between primitive types (long, double, etc.) and their corresponding wrapper types
 * (Long, Double, etc.). Primitive types will be replaced with their wrapper types when keys are created. This means
 * that, for example, {@code Key.of(int.class) is equivalent to Key.of(Integer.class)}.
 */
public abstract class Key<T> {

    /** A cache of keys used by {@link #of(Class)}. */
    private static final ClassValue<Key<?>> CLASS_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> key) {
            return Key.convertTypeLiteral(TypeToken.of(key).wrap());
        }
    };

    /** A cache of keys computed from type variables. */
    private static final ClassValue<Key<?>> TYPE_VARIABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            return convertTypeVariable((Class) implementation, Key.class, 0);
        }
    };

    /** We eagerly compute the hash code, as we assume most keys are going to be used in some kind of hash table. */
    private final int hash;

    /** Qualifiers for this key. */
    @Nullable
    // Object, null->no annotation, Annotation ->1, Annotation[] -> multiple annotations...
    private final Annotation[] qualifiers;

    /** The (canonicalized) type literal for this key. */
    private final CanonicalizedTypeToken<T> typeToken;

    /** Constructs a new key. Derives the type from this class's type parameter. */
    @SuppressWarnings("unchecked")
    protected Key() {
        Key<T> cached = (Key<T>) TYPE_VARIABLE_CACHE.get(getClass());
        this.qualifiers = cached.qualifiers;
        this.typeToken = cached.typeToken;
        this.hash = cached.hash;
    }

    /**
     * Creates a new key.
     * 
     * @param typeToken
     *            the checked type literal
     * @param qualifiers
     *            the (optional) qualifier
     */
    private Key(CanonicalizedTypeToken<T> typeToken, Annotation[] qualifiers) {
        this.typeToken = typeToken;
        this.qualifiers = qualifiers;
        if (qualifiers == null) {
            this.hash = typeToken.hashCode();
        } else {
            this.hash = typeToken.hashCode() ^ Arrays.hashCode(qualifiers);
        }
    }

    /**
     * To avoid accidentally holding on to any instance that defines this key as an anonymous class. This method creates a
     * new key instance without any reference to the instance that defined the anonymous class.
     * 
     * @return the canonicalized key
     */
    public final Key<T> canonicalize() {
        if (getClass() == CanonicalizedKey.class) {
            return this;
        }
        return new CanonicalizedKey<>(typeToken, qualifiers);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj instanceof Key<?> key && Arrays.equals(qualifiers, key.qualifiers) && typeToken.equals(key.typeToken);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return hash;
    }

    /**
     * Returns whether or not this key has a qualifier equivalent to the specified qualifier.
     * 
     * @param qualifier
     *            the qualifier to test
     * @return whether or not this key has any qualifiers of the specified type
     * @implNote this method does not test whether or not the specified annotation is annotated with {@link Qualifier}
     */
    public final boolean hasQualifier(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        if (qualifiers == null) {
            return false;
        }
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].equals(qualifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether or not this key has any qualifiers of the specified type.
     * 
     * @param qualifierType
     *            the type of qualifier
     * @return whether or not this key has any qualifiers of the specified type
     * @implNote this method does not test whether or not the specified annotation type is annotated with {@link Qualifier}
     */
    public final boolean hasQualifier(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType is null");
        if (qualifiers == null) {
            return false;
        }
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].annotationType() == qualifierType) {
                return true;
            }
        }
        return false;
    }

    /** {@return whether or not this key has any qualifiers.} */
    public final boolean hasQualifiers() {
        return qualifiers != null;
    }

    /**
     * Returns whether or not this key is equivalent to a key with no qualifiers of the specified type. Is shorthand for
     * {@code key.equals(Key.of(c))}.
     * 
     * @param c
     *            the class
     * @return true if a class key, otherwise false
     */
    public final boolean equalsTo(Class<?> c) {
        return qualifiers == null && typeToken.type() == c;
    }

    /** {@return an immutable set of any qualifiers that are part of this key.} */
    public final Set<Annotation> qualifiers() {
        return qualifiers == null ? Set.of() : Set.of(qualifiers);
    }

    /** {@return the raw type of the key} */
    public final Class<?> rawType() {
        return typeToken.rawType();
    }

    public boolean isAccessibleBy(Module module) {
        // All type List<Foo> Foo must also be accessible by the target
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (qualifiers == null) {
            return typeToken.toString();
        }
        // TODO fix formatting
        return format(qualifiers[0]) + " " + typeToken.toString();
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as returned by {@link #toString()}.
     * 
     * @return a simple string
     */
    public final String toStringSimple() {
        if (qualifiers == null) {
            return typeToken.toStringSimple();
        }
        // TODO fix
        return formatSimple(qualifiers[0]) + " " + typeToken.toStringSimple();
    }

    /** {@return the generic type of this key.} */
    public final TypeToken<T> typeToken() {
        return typeToken;
    }

    /**
     * Returns a new key retaining its original type but with the specified qualifier.
     * 
     * @param qualifier
     *            the new key's qualifier
     * @return the new key
     * @throws IllegalArgumentException
     *             if the specified annotation is not annotated with {@link Qualifier}.
     */
    // repeatable annotations??? forbidden? or overwrite.
    // We are not going to multiple qualifiers of the same type
    // Taenker det er er fint man ikke kan tilfoeje repeatable annoteringer...

    // rename to with?
    // with(Annotation... qualifiers) altsaa hvor tit har man brug for det?????
    public final Key<T> with(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        QualifierUtil.checkQualifierAnnotationPresent(qualifier);
        if (qualifiers == null) {
            return new CanonicalizedKey<>(typeToken, qualifier);
        }
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].annotationType() == qualifier.annotationType()) {
                if (qualifiers[i].equals(qualifier)) {
                    return this;
                } else {
                    Annotation[] an = Arrays.copyOf(qualifiers, qualifiers.length);
                    an[i] = qualifier;
                    return new CanonicalizedKey<>(typeToken, an);
                }
            }
        }
        Annotation[] an = Arrays.copyOf(qualifiers, qualifiers.length + 1);
        an[an.length - 1] = qualifier;
        return new CanonicalizedKey<>(typeToken, an);
    }

    /**
     * Calling this method will replace any existing qualifier.
     * 
     * @param name
     *            the qualifier name
     * @return the new key
     */
    public final Key<T> withTag(String name) {
        requireNonNull(name, "name is null");
        
        @SuppressWarnings("all")
        class TaggedAnno implements Tag {

            @Override
            public Class<? extends Annotation> annotationType() {
                return Tag.class;
            }

            @Override
            public String value() {
                return name;
            }

            
        }
        return with(new TaggedAnno());
    }
    

    public final Key<T> without(Class<? extends Annotation> qualifierType) {
        throw new UnsupportedOperationException();
    }

    public final Key<T> withoutName() {
        return without(Tag.class);
    }

    /**
     * Returns a key with no qualifier but retaining this key's type. If this key has no qualifier
     * ({@code hasQualifier() == false}), returns this key.
     * 
     * @return this key with no qualifier
     */
    public final Key<T> withoutQualifiers() {
        return qualifiers == null ? this : new CanonicalizedKey<>(typeToken, (Annotation[]) null);
    }

    /**
     * Returns a new key retaining its original type but with a qualifier of the specified type iff the specified qualifier
     * type has default values for every attribute.
     *
     * @param qualifierType
     *            the type of qualifier for the new key
     * @return the new key
     * @throws IllegalArgumentException
     *             if the specified qualifier type does not have default values for every attribute. Or if the specified
     *             qualifier type is not annotated with {@link Qualifier}.
     */
    final Key<T> withQualifier(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType is null");
        AnnotationUtil.validateRuntimeRetentionPolicy(qualifierType);
        if (!qualifierType.isAnnotationPresent(Qualifier.class)) {
            throw new IllegalArgumentException(
                    "@" + qualifierType.getSimpleName() + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
        }
        // Problemet er hvordan vi instantiere den...
        // Hvis Packed nu ikke kan laese annoteringen...
        //
        throw new UnsupportedOperationException();
    }

    // Takes any qualifier annotation on the typeLiteral
    // withQualifier(new TypeLiteral<@Named("dddd") Void>() {});
    final Key<T> withQualifier(TypeToken<Void> typeLiteral) {
        throw new UnsupportedOperationException();
    }

//    public final <S> Key<S> withType(Class<S> type) {
//        return of(type).withQualifier(qualifier);
//    }
//
//    public final <S> Key<S> withType(TypeLiteral<S> typeLiteral) {
//        return fromTypeLiteral(typeLiteral, qualifier);
//    }

    /**
     * Returns a key matching the type of the specified field and any qualifier that may be present on the field.
     * 
     * @param field
     *            the field to return a key for
     * @return a key matching the type of the field and any qualifier that may be present on the field
     * @throws RuntimeException
     *             if the field does not represent a valid key. For example, if the type is an optional type such as
     *             {@link Optional} or {@link OptionalInt}. Or if there are more than 1 qualifier present on the field
     * @see Field#getType()
     * @see Field#getGenericType()
     */
    // I think throw IAE. And then have package private methods that take a ThrowableFactory.
    // RuntimeException -> should be ConversionException
    public static Key<?> convertField(Field field) {
        TypeToken<?> tl = TypeToken.fromField(field).wrap(); // checks null
        Annotation[] annotation = QualifierUtil.findQualifier(field.getAnnotations());
        return convertTypeLiteralNullableAnnotation(field, tl, annotation);
    }

    public static Key<?> convertParameter(Parameter parameter) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a key matching the return type of the specified method and any qualifier that may be present on the method.
     * 
     * @param method
     *            the method for to return a key for
     * @return the key matching the return type of the method and any qualifier that may be present on the method
     * @throws RuntimeException
     *             if the specified method has a void return type. Or returns an optional type such as {@link Optional} or
     *             {@link OptionalInt}. Or if there are more than 1 qualifier present on the method
     * @see Method#getReturnType()
     * @see Method#getGenericReturnType()
     */
    public static Key<?> convertMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        if (method.getReturnType() == void.class) {
            throw new RuntimeException("@Provides method " + method + " cannot have void return type");
        }
        TypeToken<?> tl = TypeToken.fromMethodReturnType(method).wrap();
        Annotation[] annotation = QualifierUtil.findQualifier(method.getAnnotations());
        return convertTypeLiteralNullableAnnotation(method, tl, annotation);
    }

//    public static Key<?> fromParameter(Parameter parameter) {
//        requireNonNull(parameter, "parameter is null");
//        TypeToken<?> tl = TypeToken.fromParameter(parameter).box();
//        Annotation[] annotation = QualifierHelper.findQualifier(parameter.getAnnotations());
//        return fromTypeLiteralNullableAnnotation(parameter, tl, annotation);
//    }

    /**
     * Returns a key with no qualifier and the same type as this instance.
     * 
     * @param <T>
     *            the type of key
     * @param typeLiteral
     *            the type literal
     * @return a key with no qualifier and the same type as this instance
     * @throws RuntimeException
     *             if the type literal could not be converted to a key, for example, if it is an {@link Optional}. Or if the
     *             specified type literal it not free from type parameters
     */
    public static <T> Key<T> convertTypeLiteral(TypeToken<T> typeLiteral) {
        return convertTypeLiteralNullableAnnotation(typeLiteral, typeLiteral, (Annotation[]) null);
    }

    /**
     * Returns a key with the specified qualifier and the same type as this instance.
     * 
     * @param <T>
     *            the type of key
     * @param typeLiteral
     *            the typeLiteral of the new
     * @param qualifier
     *            the qualifier of the new
     * @return a key with the specified qualifier and the same type as this instance
     * @throws RuntimeException
     *             if the type literal could not be converted to a key, for example, if it is an {@link Optional}.
     * @throws IllegalArgumentException
     *             if the qualifier type is not annotated with {@link Qualifier}.
     */
    public static <T> Key<T> convertTypeLiteral(TypeToken<T> typeLiteral, Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        QualifierUtil.checkQualifierAnnotationPresent(qualifier);
        return convertTypeLiteralNullableAnnotation(typeLiteral, typeLiteral, qualifier);
    }

    public static <T> Key<T> convertTypeLiteralNullableAnnotation(Object source, TypeToken<T> typeLiteral, Annotation... qualifier) {
        requireNonNull(typeLiteral, "typeLiteral is null");
        // From field, fromTypeLiteral, from Variable, from class, arghhh....

        typeLiteral = typeLiteral.wrap();
        if (ClassUtil.isOptional(typeLiteral.rawType())) {
            throw new RuntimeException("Cannot convert an optional type (" + typeLiteral.toStringSimple() + ") to a Key, as keys cannot be optional");
        } else if (!TypeUtil.isFreeFromTypeVariables(typeLiteral.type())) {
            throw new RuntimeException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<"
                    + typeLiteral.toStringSimple() + "> defined: " + TypeUtil.typeVariableNamesOf(typeLiteral.type()));
        }
        return new CanonicalizedKey<T>(typeLiteral.canonicalize(), qualifier);
    }

    public static <T> Key<?> convertTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        TypeToken<?> t = TypeToken.fromTypeVariable(subClass, superClass, parameterIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) subClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[parameterIndex].getAnnotations();
        Annotation[] qa = QualifierUtil.findQualifier(annotations);
        return Key.convertTypeLiteralNullableAnnotation(superClass, t, qa);
    }

    public static <T> Key<T> ofTypeToken(TypeToken<T> token) {
        return new CanonicalizedKey<>(token.canonicalize(), (Annotation[]) null);
    }

    public static Key<?>[] of(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Key<?>[] result = new Key<?>[keys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = of(keys[i]);
        }
        return result;
    }

    /**
     * Returns a class key with no qualifiers from the specified class.
     *
     * @param <T>
     *            the type to construct a key of
     * @param key
     *            the class key to return a key from
     * @return a key matching the specified class with no qualifiers
     */
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> key) {
        requireNonNull(key, "key is null");
        return (Key<T>) CLASS_KEY_CACHE.get(key);
    }

    /**
     * Returns a key of the specified type and with the specified qualifier.
     *
     * @param <T>
     *            the type to construct a key of
     * @param type
     *            the type to construct a key of
     * @param qualifier
     *            the qualifier of the key
     * @return a key of the specified type with the specified qualifier
     */
    public static <T> Key<T> of(Class<T> type, Annotation qualifier) {
        return of(type).with(qualifier);
    }

    /** See {@link CanonicalizedTypeToken}. */
    private static final class CanonicalizedKey<T> extends Key<T> {

        /**
         * Creates a new canonicalized key.
         * 
         * @param typeLiteral
         *            the type literal
         * @param qualifiers
         *            a nullable qualifier annotation
         */
        private CanonicalizedKey(CanonicalizedTypeToken<T> typeLiteral, Annotation... qualifiers) {
            super(typeLiteral, qualifiers);
        }
    }
}
//
//// Tror vi dropper det her concept...
//// Vi brugte den i forbindelse med ServiceSelection.
//// Men nu bruger vi bare type token...
//final boolean isSuperKeyOf(Key<?> key) {
//  requireNonNull(key, "key is null");
//  if (!typeToken.equals(key.typeToken)) {
//      return false;
//  }
//  if (qualifiers != null) {
//      for (Annotation a : qualifiers) {
//          if (!key.hasQualifier(a)) {
//              return false;
//          }
//      }
//  }
//  return true;
//}