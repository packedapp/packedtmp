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
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
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
import internal.app.packed.util.types.GenericType;
import internal.app.packed.util.types.TypeUtil;

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

    // TODO I think want something similar to PackedOp. A small wrapper

    /** A cache of keys used by {@link #of(Class)}. */
    private static final ClassValue<Key<?>> CLASS_TO_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> key) {
            Class<?> cl = ClassUtil.wrap(key);
            return Key.convertTypeNullableAnnotation(cl, cl, (Annotation[]) null);
        }
    };

    // Fail on type variables.
    // Strip wildcards
    // fail on void, optional*, Provider
    // I think we need to test this
    static final List<Class<?>> FORBIDDEN = List.of(Optional.class/* , ....., */);

    /** A cache of keys computed from type variables. */
    private static final ClassValue<Key<?>> CAPTURED_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            return convertTypeVariable((Class) implementation, Key.class, 0);
        }
    };

    /**
     * We eagerly compute the hash code, as we assume most keys are going to be used in some kind of hash table. We cache
     * the hash code of the type, as many Type implementations calculate it every time. See, for example,
     * https://github.com/frohoff/jdk8u-jdk/blob/master/src/share/classes/sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java
     */
    private final int hash;

    /** Qualifiers for this key. */
    @Nullable
    // Object, null->no annotation, Annotation ->1, Annotation[] -> multiple annotations...
    private final Annotation[] qualifiers;

    private final Class<?> rawType;

    private final Type type;

    /** Constructs a new key. Derives the type from this class's type parameter. */
    @SuppressWarnings("unchecked")
    protected Key() {
        Key<T> cached = (Key<T>) CAPTURED_KEY_CACHE.get(getClass());
        this.qualifiers = cached.qualifiers;
        this.hash = cached.hash;
        this.rawType = cached.rawType;
        this.type = cached.type;
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
        this.qualifiers = qualifiers;
        this.type = type;
        this.rawType = TypeUtil.rawTypeOf(type);

        if (qualifiers == null) {
            this.hash = type.hashCode();
        } else {
            this.hash = type.hashCode() ^ Arrays.hashCode(qualifiers);
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
        return new CanonicalizedKey<>(type, qualifiers);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj instanceof Key<?> key && Arrays.equals(qualifiers, key.qualifiers) && type.equals(key.type);
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
    public final boolean equalsTo(Class<?> c) {
        return qualifiers == null && type == c;
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
    // isQualified + isQualifiedWith
    public final boolean hasQualifiers() {
        return qualifiers != null;
    }

    // Not sure we want to check this
    // No, we def do not want to go there
//    public boolean isAccessibleBy(Module module) {
//        // All type List<Foo> Foo must also be accessible by the target
//        throw new UnsupportedOperationException();
//    }

    /** {@return an immutable set of any qualifiers that are part of this key.} */
    public final Set<Annotation> qualifiers() {
        return qualifiers == null ? Set.of() : Set.of(qualifiers);
    }

    /** {@return the raw type of the key} */
    public final Class<?> rawType() {
        return rawType;
    }

    public final Type type() {
        return type;
    }
    
    /** {@inheritDoc} */
    @Override
    public final String toString() {
        if (qualifiers == null) {
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
    public final String toStringSimple() {
        if (qualifiers == null) {
            return StringFormatter.formatSimple(type);
        }
        // TODO fix
        return formatSimple(qualifiers[0]) + " " + StringFormatter.formatSimple(type);
    }

    /** {@return the generic type of this key.} */
    @SuppressWarnings("unchecked")
    final GenericType<T> typeToken() {
        return (GenericType<T>) GenericType.ofType(type);
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
        return qualifiers == null ? this : new CanonicalizedKey<>(type, (Annotation[]) null);
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

    public static <T> Key<T> convertTypeNullableAnnotation(Object source, Type type, Annotation... qualifier) {
        requireNonNull(type, "typeLiteral is null");
        // From field, fromTypeLiteral, from Variable, from class, arghhh....

        @SuppressWarnings("unchecked")
        GenericType<T> typeLiteral = (GenericType<T>) GenericType.ofType(type);

        typeLiteral = typeLiteral.wrap();
        if (ClassUtil.isOptionalType(typeLiteral.rawType())) {
            throw new RuntimeException("Cannot convert an optional type (" + typeLiteral.toStringSimple() + ") to a Key, as keys cannot be optional");
        } else if (!TypeUtil.isFreeFromTypeVariables(typeLiteral.type())) {
            throw new RuntimeException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<"
                    + typeLiteral.toStringSimple() + "> defined: " + TypeUtil.typeVariableNamesOf(typeLiteral.type()));
        }
        return new CanonicalizedKey<T>(typeLiteral.canonicalize().type(), qualifier);
    }

    static <T> Key<?> convertTypeVariable(Class<? extends T> subClass, Class<T> superClass, int parameterIndex) {
        GenericType<?> t = GenericType.fromTypeVariable(subClass, superClass, parameterIndex);

        // Find any qualifier annotation that might be present
        AnnotatedParameterizedType pta = (AnnotatedParameterizedType) subClass.getAnnotatedSuperclass();
        Annotation[] annotations = pta.getAnnotatedActualTypeArguments()[parameterIndex].getAnnotations();
        Annotation[] qa = QualifierUtil.findQualifier(annotations);
        return Key.convertTypeVariable0(superClass, t, qa);
    }

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
    private static <T> Key<T> convertTypeVariable0(Object source, GenericType<T> typeLiteral, Annotation... qualifier) {
        requireNonNull(typeLiteral, "typeLiteral is null");
        // From field, fromTypeLiteral, from Variable, from class, arghhh....

        typeLiteral = typeLiteral.wrap();
        if (ClassUtil.isOptionalType(typeLiteral.rawType())) {
            throw new RuntimeException("Cannot convert an optional type (" + typeLiteral.toStringSimple() + ") to a Key, as keys cannot be optional");
        } else if (!TypeUtil.isFreeFromTypeVariables(typeLiteral.type())) {
            throw new RuntimeException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<"
                    + typeLiteral.toStringSimple() + "> defined: " + TypeUtil.typeVariableNamesOf(typeLiteral.type()));
        }
        return new CanonicalizedKey<T>(typeLiteral.canonicalize().type(), qualifier);
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