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
package app.packed.binding;

import static internal.app.packed.util.StringFormatter.format;
import static internal.app.packed.util.StringFormatter.formatSimple;
import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import app.packed.framework.Nullable;
import internal.app.packed.util.AnnotationUtil;
import internal.app.packed.util.QualifierUtil;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.ClassUtil;
import internal.app.packed.util.types.GenericType.CanonicalizedGenericType;
import internal.app.packed.util.types.TypeUtil;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 * A key defines a unique identifier with two parts: a {@link Type type} part and an optional set of special annotations
 * called {@link Qualifier qualifiers}.
 * <p>
 * It does so by requiring users to create a subclass of this class which enables retrieval of the type information even
 * at runtime. Some examples of non-qualified keys are:
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
 * <li>Not be one of the following types: {@link Provider}, {@link Void}, {@link Key}, {@link Optional},
 * {@link OptionalInt}, {@link OptionalLong} or {@link OptionalDouble} as they are reserved types.</li>
 * <li>Have free type thingies</li>
 * </ul>
 * <p>
 * Keys do not differentiate between primitive types (long, double, etc.) and their corresponding wrapper types (Long,
 * Double, etc.). When construction a key, primitive types will automatically be replaced with their wrapper types. This
 * means, for example, that calling {@code Key.of(int.class)} will return the equivalent of
 * {@code Key.of(Integer.class)}.
 * <p>
 * When creating a key, any usage of {@link WildcardType wildcard types} will automatically be replaced by its
 * respective {@link WildcardType#getUpperBounds() upper} or {@link WildcardType#getLowerBounds() lower} bound. This
 * means, for example, that {@code new Key<Map<? extends String, ? super Long>>} is equivalent to
 * {@code Key<Map<String, Long>>}. This is done in order to avoid various
 * <a href= "https://github.com/google/guice/issues/1282">issues</a> in relation to other JVM languages.
 * <p>
 * This class is heavily inspired by a similar named class in the
 * <a href="https://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/Key.html">Guice</a> project.
 */

//Fail on
////TypeVariables
////Provider, Lazy, Optional, ..., keys
////Void.class
////Non-qualifting annotation

//Changes
////Reduce wildcards
////wrap primitives

//Created by user a.la
//Key.of 
//Created while parsing a bean a.la.
//public void foo(@SomeInvalidQualifier Void k)

public abstract class Key<T> {

    /** A cache of keys created by {@link #Key()}. */
    private static final ClassValue<Key<?>> CAPTURED_KEY_CACHE = new ClassValue<>() {

        private static final TypeVariableExtractor TVE = TypeVariableExtractor.of(Key.class);

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            Variable v = TVE.extractVariable(implementation, IllegalArgumentException::new);
            return convertCaptured(v);
        }
    };

    /** A cache of keys used by {@link #of(Class)}. */
    private static final ClassValue<Key<?>> CLASS_TO_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> key) {
            Class<?> cl = ClassUtil.wrap(key);
            return Key.convertTypeNullableAnnotation(cl, cl, QualifierUtil.NO_QUALIFIERS);
        }
    };

    /** Various classes that are not allowed as the type part of a key. */
    private static final Set<Class<?>> FORBIDDEN_TYPES = Set.of(Optional.class, OptionalDouble.class, OptionalInt.class, OptionalLong.class, Void.class,
            Provider.class, Key.class);

    /** We eagerly compute the hash code, as we assume most keys are going to be used in some kind of hash table. */
    private final int hash;

    /**
     * Qualifiers for this key.
     * 
     * Object,
     * 
     * null = no annotation,
     * 
     * Annotation = 1,
     * 
     * Annotation[] -> multiple annotations...
     */
    private final Annotation[] qualifiers;

    /** The raw type of the key. */
    private final Class<?> rawType;

    /** The type part of the key. */
    private final Type type;

    /** Constructs a new key. Derives the type from the type parameter {@code <T>} of the extending class. */
    protected Key() {
        Key<?> cached = CAPTURED_KEY_CACHE.get(getClass());
        this.type = cached.type;
        this.rawType = cached.rawType;
        this.qualifiers = cached.qualifiers;
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
    private Key(Type type, Annotation[] qualifiers) {
        this.qualifiers = requireNonNull(qualifiers);
        this.type = type;
        this.rawType = TypeUtil.rawTypeOf(type);
        int h = type.hashCode();
        h ^= Arrays.hashCode(qualifiers);
        this.hash = h;
    }

    /**
     * When constructing a key using type capture
     * <p>
     * As an end-user there is rarely any no reason to use this method. All extensions that takes keys must canonicalize
     * them is storing them
     * <p>
     * To avoid accidentally holding on to any instance that defines this key as an anonymous class. This method creates a
     * new key instance without any reference to the instance that defined the anonymous class.
     * 
     * @return the canonicalized key
     */
    public final Key<T> canonicalize() {
        if (getClass() == CanonicalizedKey.class) {
            return this;
        }
        return new CanonicalizedKey<>(type, qualifiers);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Key<?> key && type.equals(key.type)) {
            Annotation[] a = key.qualifiers;
            if (qualifiers.length == a.length) {
                if (qualifiers.length == 0) {
                    return true;
                } else if (qualifiers.length == 1) {
                    return a[0].equals(qualifiers[0]);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }
        return false;
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
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].annotationType() == qualifierType) {
                return true;
            }
        }
        return false;
    }

    /** {@return whether or not this key has any qualifiers.} */
    // isQualified + isQualifiedWith
    public final boolean hasQualifiers() {
        return qualifiers.length > 0;
    }

    /**
     * Returns whether or not this key is equivalent to a key with no qualifiers of the specified type. Is shorthand for
     * {@code key.equals(Key.of(c))}.
     * 
     * @param c
     *            the class
     * @return true if a class key, otherwise false
     */
    // better name
    // A simple key is a key without any qualifiers
    // A class key is a key where the type part is a Class
    // a simple class key is a key of
    final boolean isSimpleClassKey(Class<?> c) {
        return qualifiers == null && type == c;
    }

    /** {@return an immutable set of any qualifiers that are part of this key.} */
    public final Set<Annotation> qualifiers() {
        return qualifiers == null ? Set.of() : Set.of(qualifiers);
    }

    /** {@return the raw type of the type part of this key} */
    public final Class<?> rawType() {
        return rawType;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (qualifiers.length == 0) {
            return StringFormatter.format(type);
        }
        // TODO fix formatting
        return format(qualifiers[0]) + " " + StringFormatter.format(type);
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as returned by {@link #toString()}.
     * 
     * @return a simple string
     */
    // Omvendt tror jeg vil have en lille key, men mulighed for at lave en lang
    // toStringLong or toStringFull
    public final String toStringSimple() {
        if (qualifiers.length == 0) {
            return StringFormatter.formatSimple(type);
        }
        // TODO fix
        return formatSimple(qualifiers[0]) + " " + StringFormatter.formatSimple(type);
    }

    /** {@return the type part of this key} */
    public final Type type() {
        return type;
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
            return new CanonicalizedKey<>(type, qualifier);
        }
        for (int i = 0; i < qualifiers.length; i++) {
            if (qualifiers[i].annotationType() == qualifier.annotationType()) {
                if (qualifiers[i].equals(qualifier)) {
                    return this;
                } else {
                    Annotation[] an = Arrays.copyOf(qualifiers, qualifiers.length);
                    an[i] = qualifier;
                    return new CanonicalizedKey<>(type, an);
                }
            }
        }
        Annotation[] an = Arrays.copyOf(qualifiers, qualifiers.length + 1);
        an[an.length - 1] = qualifier;
        return new CanonicalizedKey<>(type, an);
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
        return qualifiers.length == 0 ? this : new CanonicalizedKey<>(type, QualifierUtil.NO_QUALIFIERS);
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

    static <T> Key<?> convertCaptured(Variable v) {

        Annotation[] qa = QualifierUtil.findQualifier(v.getAnnotations());
        //// TODO check no non-qualifting annotation...
        //// This is allowed elsewheres (ops, beans), just not when creating the Key directly.
        //// Or via TypeCapture

        Type t = convertType(v.getType());

        return new CanonicalizedKey<T>(t, qa);
    }

    private static Type convertType(Type t) {
        if (t instanceof Class<?> cl) {
            if (cl.isPrimitive()) {
                t = ClassUtil.wrap(cl);
            }
        }
        Class<?> rawType = TypeUtil.rawTypeOf(t);

        if (FORBIDDEN_TYPES.contains(rawType)) {
            if (ClassUtil.isOptionalType(rawType)) {
                throw new RuntimeException("Cannot convert an optional type (" + t.toString() + ") to a Key, as keys cannot be optional");
            }
        }

        if (!TypeUtil.isFreeFromTypeVariables(t)) {
            throw new RuntimeException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<" + t.toString()
                    + "> defined: " + TypeUtil.typeVariableNamesOf(t));
        }

        // TODO reduce wildcards

        return t;
    }

    public static <T> Key<T> convertTypeNullableAnnotation(Object source, Type type, Annotation... qualifier) {
        requireNonNull(type, "typeLiteral is null");
        // From field, fromTypeLiteral, from Variable, from class, arghhh....

        Type t = convertType(type);

        return new CanonicalizedKey<T>(t, qualifier);
    }

    public static Type convertx(Object source, Type originalType, Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return type;
        } else if (type instanceof ParameterizedType pt) {
            throw new UnsupportedOperationException();
//            pt.get
//            if (pt.getOwnerType() != null && !isFreeFromTypeVariables(pt.getOwnerType())) {
//                return false;
//            }
//            for (Type t : pt.getActualTypeArguments()) {
//                if (!isFreeFromTypeVariables(t)) {
//                    return false;
//                }
//            }
//            // To be safe we check the raw type as well, I expect it should always be a class, but the method signature says
//            // something else
//            return isFreeFromTypeVariables(pt.getRawType());
        } else if (type instanceof GenericArrayType gat) {
            return gat;
        } else if (type instanceof TypeVariable) {
            throw new InvalidKeyException("opps");
        } else if (type instanceof WildcardType wt) {
            Type t = wt.getLowerBounds()[0];
            if (t == null) {
                t = wt.getUpperBounds()[0];
                if (t == null) {
                    throw new InvalidKeyException("opps");
                }
            }
            return convertx(source, originalType, t);
        } else {
            throw new InvalidKeyException("Unknown type: " + type);
        }
    }

    /**
     * Returns a class key with no qualifiers from the specified class.
     *
     * @param <T>
     *            the type to construct a key of
     * @param key
     *            the class key to return a key from
     * @return a key matching the specified class with no qualifiers
     * 
     * @throws InvalidKeyException
     *             if the specified class does not represent a valid key
     */
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> key) {
        requireNonNull(key, "key is null");
        return (Key<T>) CLASS_TO_KEY_CACHE.get(key);
    }

    public static Key<?>[] ofAll(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Key<?>[] result = new Key<?>[keys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = of(keys[i]);
        }
        return result;
    }

    /** See {@link CanonicalizedGenericType}. */
    private static final class CanonicalizedKey<T> extends Key<T> {

        /**
         * Creates a new canonicalized key.
         * 
         * @param typeLiteral
         *            the type literal
         * @param qualifiers
         *            a nullable qualifier annotation
         */
        private CanonicalizedKey(Type type, Annotation... qualifiers) {
            super(type, qualifiers);
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