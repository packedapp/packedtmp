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
package app.packed.bindings;

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

import app.packed.framework.AnnotationList;
import app.packed.framework.Nullable;
import internal.app.packed.bean.PackedAnnotationList;
import internal.app.packed.util.AnnotationUtil;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.ClassUtil;
import internal.app.packed.util.types.TypeUtil;
import internal.app.packed.util.types.TypeVariableExtractor;
import internal.app.packed.util.types.Types;

/**
 * A key defines a unique identifier with two parts: a {@link Type type} part and an optional list of special
 * annotations called {@link Qualifier qualifiers}.
 * <p>
 * A key can be constructed in a number of different ways:
 *
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
 * In addition to these, extensions also the option to create keys using:
 *
 * {@link OperationalField#toKey()} {@link BindableVariable#toKey()}
 *
 * In order for a key to be valid, the type part of the key must not:
 * <ul>
 * <li>Be an {@link Class#isAnnotation() annotation interface}.</li>
 * <li>Be one of the following types: {@link Provider}, {@link Void}, {@link Key}, {@link Optional},
 * {@link OptionalInt}, {@link OptionalLong} or {@link OptionalDouble} as they are reserved types.</li>
 * <li>Contain {@link TypeVariable type variable}</li>
 * </ul>
 *
 * Furthermore, the qualifier part of the key must not:
 * <ul>
 * <li>Have multiple qualifiers with the same {@link Annotation#annotationType() annotation type}.
 * <li>Have multiple qualifiers with the same {@link Class#getCanonicalName() canonical} name. (This can only happen if
 * a key uses qualifiers from different class loaders)
 * </ul>
 * These last two rules are in place in order to allow a canonical representation of keys with multiple qualifiers.
 * <p>
 * In addition to these rules, there are number of conversion rules for how a key is constructed:
 * <ul>
 * <li>Keys do not differentiate between primitive types (long, double, etc.) and their corresponding wrapper types
 * (Long, Double, etc.). When construction a key, primitive types will automatically be replaced with their wrapper
 * types. This means, for example, that calling {@code Key.of(int.class)} will return the equivalent of
 * {@code Key.of(Integer.class)}.</li>
 * <li>Any usage of {@link WildcardType wildcard types} will automatically be replaced by their respective
 * {@link WildcardType#getUpperBounds() upper} or {@link WildcardType#getLowerBounds() lower} bound. This means, for
 * example, that {@code new Key<Map<? extends String, ? super Long>>} is equivalent to {@code Key<Map<String, Long>>}.
 * This is done in order to avoid various <a href= "https://github.com/google/guice/issues/1282">issues</a> when
 * integrating with other JVM languages.</li>
 * <li>If there are multiple qualifiers on the key. They will be ordered lexically, first accordingling to their
 * {@link Class#getSimpleName()} and secondaly to {@link Class#getCanonicalName()}.</li>
 * </ul>
 *
 * <p>
 * This class is heavily inspired by the similar named class in the
 * <a href="https://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/Key.html">Guice</a> project.
 */

//Created by user a.la
//Key.of
//Created while parsing a bean a.la.
//public void foo(@SomeInvalidQualifier Void k)

// Key (Source)
//// TypeCapture (Super class of Key)
//// ofClass (null)
//// fromField (Field)
//// fromField (OperationalField)
//// fromMethodReturnType (OperationalMethod)
//// fromVariable (Variable)
//// fromClass (OperationalClass or class?)

// Tror vi kan supportere nogenlunde det hele

// Open questions
// how does adding qualifiers work. Replace vs fail on already existing qualifier
public abstract class Key<T> {

    /** A cache of keys created by {@link #Key()}. */
    private static final ClassValue<Key<?>> CAPTURED_KEY_CACHE = new ClassValue<>() {

        /** Type variable extractor for finding the type and optional qualifiers of an extending key. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(Key.class);

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            // We first convert it to a variable, as it contains both the type and any annotations
            Variable v = EXTRACTOR.extractVariable(implementation, e -> new InvalidKeyException(e));

            Type type = convertType(v.getType(), this);
            PackedAnnotationList annotations = qualifiersConvertExplicit(v.getAnnotations(), this);
            return new CanonicalizedKey<>(type, annotations);
        }
    };

    /** A cache of keys used by {@link #of(Class)}. */
    private static final ClassValue<Key<?>> CLASS_TO_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> key) {
            Type type = convertType(key, null);
            return new CanonicalizedKey<>(type, PackedAnnotationList.EMPTY);
        }
    };

    /** Various classes that are not allowed as the type part of a key. */
    private static final Set<Class<?>> FORBIDDEN_TYPES = Set.of(Optional.class, OptionalDouble.class, OptionalInt.class, OptionalLong.class, Void.class,
            Provider.class, Key.class);

    /** We eagerly compute the hash code, as we assume most keys are going to be used in a hash table of some kind. */
    private final int hash;

    /** Qualifiers for this key. */
    private final PackedAnnotationList qualifiers;

    /** The type part of the key. */
    private final Type type;

    /**
     * Constructs a new key. Derives the type and optional {@link Qualifier qualifiers} from the type parameter {@code <T>}
     * of the extending class.
     */
    protected Key() {
        Key<?> cached = CAPTURED_KEY_CACHE.get(getClass());
        this.type = cached.type;
        this.qualifiers = cached.qualifiers;
        this.hash = cached.hash;
    }

    /**
     * Create a new key.
     *
     * @param type
     *            the type part of the key
     * @param qualifiers
     *            optional qualifiers
     * @param hash
     *            the hash of the key
     */
    private Key(Type type, PackedAnnotationList qualifiers, int hash) {
        this.type = requireNonNull(type);
        this.qualifiers = requireNonNull(qualifiers);
        this.hash = hash;
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
        return new CanonicalizedKey<>(type, qualifiers, hash);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(@Nullable Object obj) {
        return obj == this || obj instanceof Key<?> key && type.equals(key.type) && qualifiers.equals(key.qualifiers);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return hash;
    }

    /** {@return whether or not this key has any qualifiers.} */
    public final boolean isQualified() {
        return !qualifiers.isEmpty();
    }

    /**
     * Returns whether or not this key has a qualifier equivalent to the specified qualifier.
     *
     * @param qualifier
     *            the qualifier to test
     * @return {@code true} if this key has the specified qualifier
     * @implNote this method does not test if the specified annotation is annotated with {@link Qualifier}
     */
    public final boolean isQualifiedWith(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        return qualifiers.contains(qualifier);
    }

    /**
     * Returns whether or not this key has any qualifiers of the specified type.
     *
     * @param qualifierType
     *            the type of qualifier
     *
     * @return {@code true} if this key has a qualifier of the specified type
     * @implNote this method does not test if the specified annotation type is annotated with {@link Qualifier}
     */
    public final boolean isQualifiedWithType(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType is null");
        return qualifiers.containsType(qualifierType);
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
        return type == c && !isQualified();
    }

    /** {@return the number of qualifiers on the key} */
    public final int qualifierCount() {
        return qualifiers.size();
    }

    /** {@return an annotation list of any qualifiers that are part of this key.} */
    public final AnnotationList qualifiers() {
        return qualifiers;
    }

    /** {@return the raw type of the type part of this key.} */
    public final Class<?> rawType() {
        return TypeUtil.rawTypeOf(type);
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as returned by {@link #toString()}.
     *
     * @return a simple string
     */
    @Override
    public final String toString() {
        return toString(false);
    }

    private String toString(boolean longFormat) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key<");
        if (isQualified()) {
            if (longFormat) {
                sb.append(StringFormatter.format(qualifiers.annotations()[0]));
            } else {
                sb.append(StringFormatter.formatSimple(qualifiers.annotations()[0]));
            }
        }
        if (longFormat) {
            sb.append(StringFormatter.format(type));
        } else {
            sb.append(StringFormatter.formatSimple(type));
        }
        return sb.append(">").toString();
    }

    /**
     * Returns a string where all the class names are replaced by their simple names. For example this method will return
     * {@code List<String>} instead of {@code java.util.List<java.lang.String>} as returned by {@link #toString()}.
     *
     * @return a simple string
     */
    public final String toStringLong() {
        return toString(true);
    }

    /** {@return the type part of this key} */
    public final Type type() {
        return type;
    }

    public final Key<T> withoutName() {
        return withoutQualifierOfType(Tag.class);
    }

    public final Key<T> withoutQualifierOfType(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType");
        Annotation[] annotations = qualifiers.annotations();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == qualifierType) {
                if (annotations.length == 1) {
                    return withoutQualifiers();
                }
                Annotation[] newAnnotations = new Annotation[annotations.length - 1];
                for (int j = 0; j < i; j++) {
                    newAnnotations[j] = annotations[j];
                }
                for (int j = i + 1; j < annotations.length; j++) {
                    newAnnotations[j] = annotations[j - 1];
                }
                return new CanonicalizedKey<>(type, new PackedAnnotationList(newAnnotations));
            }
        }
        return this; // we don't fail if qualifier type is not present
    }

    /**
     * Returns a key with no qualifier but retaining the type of this key. If this key has no qualifiers
     * ({@code isQualified() == false}), returns this key.
     *
     * @return this key without qualifiers
     */
    public final Key<T> withoutQualifiers() {
        return isQualified() ? new CanonicalizedKey<>(type, PackedAnnotationList.EMPTY) : this;
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
    public final Key<T> withQualifier(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        qualifiersCheckQualifierAnnotationPresent(qualifier);

        if (!isQualified()) {
            return new CanonicalizedKey<>(type, new PackedAnnotationList(new Annotation[] { qualifier }));
        }
        for (int i = 0; i < qualifierCount(); i++) {
            if (qualifiers.annotations()[i].annotationType() == qualifier.annotationType()) {
                if (qualifiers.annotations()[i].equals(qualifier)) {
                    return this;
                } else {
                    Annotation[] an = Arrays.copyOf(qualifiers.annotations(), qualifierCount());
                    an[i] = qualifier;
                    return new CanonicalizedKey<>(type, new PackedAnnotationList(an));
                }
            }
        }
        Annotation[] an = Arrays.copyOf(qualifiers.annotations(), qualifierCount() + 1);
        an[an.length - 1] = qualifier;
        return new CanonicalizedKey<>(type, new PackedAnnotationList(an));
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
        return withQualifier(new TaggedAnno());
    }

    public final <E> Key<E> withType(Class<E> type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedKey<>(convertType(type, null), qualifiers);
    }

    /**
     * @param type
     *            the type part of the new key
     * @return the new key
     * @throws InvalidKeyException
     *             if the specified type is not valid as the type part of a key
     */
    // Hmm, Ville naesten syntes at IAE er bedre en IKE
    public final Key<?> withType(Type type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedKey<>(convertType(type, null), qualifiers);
    }

    public static Key<?> convert(Type type, Annotation[] annotations, boolean ignoreNonQualifyingAnnotations, Object source) {
        Type t = convertType(type, source);
        PackedAnnotationList apl;
        if (!ignoreNonQualifyingAnnotations) {
            apl = qualifiersConvertExplicit(annotations, source);
        } else {
            apl = qualifiersConvert(annotations, source);
        }
        return new CanonicalizedKey<>(t, apl);
    }

    // How do we support adding qualifiers?
    // Maybe it is a separate method

    // If source == null we are creating the key directly (new Key<>(), or Off);
    // And we filter non-qualifyign annotations instead of failing on the them

    private static Type convertType(Type t, Object source) {
        if (t instanceof Class<?> cl) {
            if (cl.isPrimitive()) {
                t = ClassUtil.wrap(cl);
            }
        }
        Class<?> rawType = TypeUtil.rawTypeOf(t);
        if (FORBIDDEN_TYPES.contains(rawType)) {
            throw new InvalidKeyException(t + " ");
        }

        if (!TypeUtil.isFreeFromTypeVariables(t)) {
            throw new InvalidKeyException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<" + t.toString()
                    + "> defined: " + TypeUtil.typeVariableNamesOf(t));
        }

        return convertType0(source, t, t);
        // TODO reduce wildcards

        // return t;
    }

    static Type convertType0(Object source, Type originalType, Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return type;
        } else if (type instanceof ParameterizedType pt) {
            Type rawType = convertType0(source, originalType, pt.getRawType());

            Type[] args = pt.getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                args[i] = convertType0(source, originalType, args[0]);
            }
            return Types.createNewParameterizedType(rawType, args);
//            throw new UnsupportedOperationException();
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
            Type[] lowerBounds = wt.getLowerBounds();
            Type t;
            if (lowerBounds.length == 0) {
                t = wt.getUpperBounds()[0];
                if (t == null) {
                    throw new InvalidKeyException("opps");
                }

            } else {
                t = lowerBounds[0];
            }
            return convertType0(source, originalType, t);
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

    static <T> Key<T> of(Class<T> key, Annotation... qualifiers) {
        // maybe just have of(key).withQualifiers(...)
        Key<T> k = of(key);
        PackedAnnotationList pal = qualifiersConvertExplicit(qualifiers.clone(), null);
        return new CanonicalizedKey<>(k.type, pal);
    }

    public static Key<?> of(Type type, Annotation... qualifiers) {
        requireNonNull(type, "type is null");
        requireNonNull(qualifiers, "qualifiers is null");
        throw new UnsupportedOperationException();
    }

    public static Key<?>[] ofAll(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Key<?>[] result = new Key<?>[keys.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = of(keys[i]);
        }
        return result;
    }

    private static void qualifiersCheckQualifierAnnotationPresent(Annotation e) {
        Class<?> annotationType = e.annotationType();
        // TODO check also withQualifier
        // TODO is class value faster?
        if (annotationType.isAnnotationPresent(Qualifier.class)) {
            return;
        }
        // Har maaske nogle steder jeg hellere vil have IllegalArgumentException...
        // InjectExtension??? I think that's better...
        throw new IllegalArgumentException("@" + StringFormatter.format(annotationType) + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
    }

    private static PackedAnnotationList qualifiersConvert(Annotation[] annotations, Object source) {
        return switch (annotations.length) {
        case 0 -> PackedAnnotationList.EMPTY;
        case 1 -> {
            yield qualifiersIsAnnotationPresent(annotations[0]) ? new PackedAnnotationList(annotations) : PackedAnnotationList.EMPTY;
        }
        default -> {
            int count = 0;
            for (int i = 0; i < annotations.length; i++) {
                // Array is safe to modify. Or is it?
                // I think we are sharing it
                Annotation a = annotations[i];
                if (qualifiersIsAnnotationPresent(a)) {
                    if (count > 0) {
                        annotations[i - count] = a;
                    }
                } else {
                    count++;
                }
            }
            if (count > 0) {
                annotations = Arrays.copyOf(annotations, annotations.length - count);
            }
            yield new PackedAnnotationList(qualifiersSort(annotations));
        }
        };
    }

    private static PackedAnnotationList qualifiersConvertExplicit(Annotation[] annotations, Object source) {
        return switch (annotations.length) {
        case 0 -> PackedAnnotationList.EMPTY;
        case 1 -> {
            if (qualifiersIsAnnotationPresent(annotations[0])) {
                yield new PackedAnnotationList(annotations);
            } else {
                throw new InvalidKeyException("");
            }
        }
        default -> {
            for (Annotation a : annotations) {
                if (!qualifiersIsAnnotationPresent(a)) {
                    throw new InvalidKeyException("asdasd");
                }
            }
            yield new PackedAnnotationList(qualifiersSort(annotations));
        }
        };
    }

    private static boolean qualifiersIsAnnotationPresent(Annotation a) {
        return a.annotationType().isAnnotationPresent(Qualifier.class);
    }

    private static Annotation[] qualifiersSort(Annotation[] annotations) {
        Arrays.sort(annotations, (a1, a2) -> {
            Class<? extends Annotation> a1t = a1.annotationType();
            Class<? extends Annotation> a2t = a2.annotationType();
            if (a1t == a2t) {
                throw new InvalidKeyException("Cannot use multiple qualifiers of the same type");
            }
            int c = a1t.getSimpleName().compareTo(a2t.getSimpleName());
            if (c != 0) {
                return c;
            }
            c = a1t.getCanonicalName().compareTo(a2t.getCanonicalName());
            if (c != 0) {
                return c;
            }
            throw new InvalidKeyException("Cannot use multiple qualifiers with the same canonical name");
        });
        // Fails on qualifiers with same name
        return annotations;
    }

    /** See {@link CanonicalizedGenericType}. */
    private static final class CanonicalizedKey<T> extends Key<T> {

        /**
         * Creates a new canonicalized key.
         *
         * @param type
         *            the (checked) type
         * @param qualifiers
         *            optional qualifiers
         */
        private CanonicalizedKey(Type type, PackedAnnotationList qualifiers) {
            super(type, qualifiers, type.hashCode() ^ qualifiers.hashCode());
        }

        private CanonicalizedKey(Type type, PackedAnnotationList qualifiers, int hashCode) {
            super(type, qualifiers, hashCode);
        }
    }
}

//New Qualifier support
//Vi bliver noedt til at have en strategi for
//Key<@A @B Integer> vs Key<@B @A Integer>
//Key<@A @A> is not allowed
//We don't allow multiple qualifiers of the same type or with the same canonical name
//And qualifiers are always sorted accordingly to their simple name first with String.compare
//And then their canonical name if same

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