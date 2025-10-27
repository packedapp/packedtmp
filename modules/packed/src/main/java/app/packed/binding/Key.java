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

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
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

import app.packed.util.AnnotationList;
import internal.app.packed.util.AnnotationUtil;
import internal.app.packed.util.PackedAnnotationList;
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
 * <li>Contain any {@link TypeVariable type variables}</li>
 * </ul>
 *
 * Furthermore, the qualifier part of the key cannot have:
 * <ul>
 * <li>Multiple qualifiers of the same {@link Annotation#annotationType() annotation type}.
 * <li>Multiple qualifiers with the same {@link Class#getCanonicalName() canonical} name. (This can only happen if a key
 * uses qualifiers from different class loaders)
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
 * <li>If there are multiple qualifiers on the key. They will be ordered lexically, first according to their
 * {@link Class#getSimpleName()} and secondary to their {@link Class#getCanonicalName()}.</li>
 * </ul>
 *
 * <p>
 * This class is heavily inspired by the similar named class in
 * <a href="https://google.github.io/guice/api-docs/latest/javadoc/com/google/inject/Key.html">Guice</a>.
 */

//Created by user a.la
//Key.of
//Created while parsing a bean a.la.
//public void foo(@SomeInvalidQualifier Void k)

// Open questions
// how does adding qualifiers work. Replace vs fail on already existing qualifier
@SuppressWarnings("unused")
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

            Type type = convertType(v.type(), TYPE_CAPTURE, this);
            PackedAnnotationList annotations = convertQualifiers(v.annotations().toArray(), true, TYPE_CAPTURE, this);
            return new CanonicalizedKey<>(type, annotations);
        }
    };

    /** A cache of keys used by {@link #of(Class)}. */
    private static final ClassValue<Key<?>> CLASS_TO_KEY_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected Key<?> computeValue(Class<?> key) {
            Type type = convertType(key, OF_CLASS, null);
            return new CanonicalizedKey<>(type, PackedAnnotationList.EMPTY);
        }
    };
    /** Various classes that are not allowed as the type part of a key. */
    private static final Set<Class<?>> FORBIDDEN_KEY_TYPES = Set.of(Optional.class, OptionalDouble.class, OptionalInt.class, OptionalLong.class, Void.class,
            Provider.class, Key.class);

    private static final int FROM_BEAN_CLASS = 4;
    private static final int FROM_BEAN_FIELD = 6;
    private static final int FROM_BEAN_METHOD_RETURN_TYPE = 8;
    private static final int FROM_BEAN_VARIABLE = 10;
    private static final int FROM_CLASS = 3;
    private static final int FROM_FIELD = 5;
    private static final int FROM_METHOD_RETURN_TYPE = 7;
    private static final int FROM_VARIABLE = 9;

    private static final int LATER_CONVERSION = 0;

    private static final int OF_CLASS = 2;

    private static final int TYPE_CAPTURE = 1;

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
     * Canonicalizes the key.
     * <p>
     * When constructing a key using type capture the compiler might choose to capture an enclosing instance of the key's
     * declaring class. This method creates a new key instance without any reference to the instance that defined the
     * anonymous class.
     * <p>
     * As an end-user there is rarely any no reason to use this method. However, extensions that plans to maintain
     * references to keys for a long time should always call this method before storing the key.
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
    public final boolean equals(Object obj) {
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
        return qualifiers.isPresent(qualifier);
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
        return qualifiers.isPresent(qualifierType);
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

    /** {@return the number of qualifiers on this key} */
    public final int qualifierCount() {
        return qualifiers.size();
    }

    /** {@return a list of any qualifiers that are part of this key.} */
    public final AnnotationList qualifiers() {
        return qualifiers;
    }

    /** {@return the raw type of this key.} */
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

    /**
     * Returns a key without a qualifier of the specified type.
     * <p>
     * This method return this if no qualifier of the specified type is present on the key.
     *
     * @param qualifierType
     *            the type of qualifier
     * @return a key without a qualifier of the specified type
     */
    public final Key<T> withoutQualifierOfType(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType");
        Annotation[] annotations = qualifiers.annotations();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == qualifierType) {
                if (annotations.length == 1) {
                    return new CanonicalizedKey<>(type, PackedAnnotationList.EMPTY);
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
        return this;
    }

    /**
     * Returns a key with same type as this key but with no qualifiers.
     * <p>
     * If this key has no qualifiers ({@code isQualified() == false}), returns this key.
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
    public final Key<T> withQualifier(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        if (!isQualifierAnnotationPresent(qualifier)) {
            throw new IllegalArgumentException(
                    "@" + StringFormatter.format(qualifier.getClass()) + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
        }
        if (!isQualified()) {
            return new CanonicalizedKey<>(type, new PackedAnnotationList(new Annotation[] { qualifier }));
        }
        // Check if we are just replacing an existing qualifier.
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
        return new CanonicalizedKey<>(type, convertQualifiers(an, false /* already checked */, 0, this));
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
    // The problem is instantiating them. We need a MethodHandles.Lookup...
    final Key<T> withQualifier(Class<? extends Annotation> qualifierType /* , Map<String, Object> attributes */) {
        requireNonNull(qualifierType, "qualifierType is null");
        AnnotationUtil.validateRuntimeRetentionPolicy(qualifierType);
        if (!qualifierType.isAnnotationPresent(Qualifier.class)) {
            throw new IllegalArgumentException(
                    "@" + qualifierType.getSimpleName() + " is not a valid qualifier. The annotation must be annotated with @Qualifier");
        }
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
        record TaggedAnno(Class<? extends Annotation> annotationType, String value) implements StringQualifier {}
        return withQualifier(new TaggedAnno(StringQualifier.class, name));
    }

    /**
     * Returns a new key where the type part of this key is replaced by the specified class.
     *
     * @param type
     *            the class type of the new key
     * @return the new key
     * @throws InvalidKeyException
     *             if the specified class cannot be used as the type part of a key
     * @see #withType(Type)
     */
    public final <E> Key<E> withType(Class<E> type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedKey<>(convertType(type, LATER_CONVERSION, null), qualifiers);
    }

    /**
     * Returns a new key where the type part of this key is replaced by the specified type.
     *
     * @param type
     *            the type of the new key
     * @return the new key
     * @throws InvalidKeyException
     *             if the specified type cannot be used as the type part of a key
     * @see #withType(Class)
     */
    public final Key<?> withType(Type type) {
        requireNonNull(type, "type is null");
        return new CanonicalizedKey<>(convertType(type, LATER_CONVERSION, null), qualifiers);
    }

    private static Key<?> convert(Type type, Annotation[] annotations, boolean onlyQualifierAnnotationsAllowed, int sourceKind, Object source) {
        Type t = convertType(type, sourceKind, source);
        PackedAnnotationList apl = convertQualifiers(annotations, onlyQualifierAnnotationsAllowed, sourceKind, source);
        return new CanonicalizedKey<>(t, apl);
    }

    /**
     * Sorts the array of qualifiers while at same time checking some invariants.
     *
     * @param sourceKind
     *            the source kind
     * @param source
     *            the type of source
     * @param annotations
     *            the annotations to sort
     * @return the sorted annotations
     * @throws InvalidKeyException
     *             if the specified array contains multiple qualifiers of the same type or with the same canonical name
     */
    private static PackedAnnotationList convertQualifiers(Annotation[] annotations, boolean onlyQualifierAnnotationsAllowed, int sourceKind, Object source) {
        return switch (annotations.length) {
        case 0 -> PackedAnnotationList.EMPTY;
        case 1 -> {
            PackedAnnotationList result = PackedAnnotationList.EMPTY;
            if (isQualifierAnnotationPresent(annotations[0])) {
                yield new PackedAnnotationList(annotations);
            } else if (onlyQualifierAnnotationsAllowed) {
                throw new InvalidKeyException(makeMsg(sourceKind, source, "Only annotations with @Qualifier allowed for this method"));
            } else {
                yield PackedAnnotationList.EMPTY;
            }
        }
        default -> {
            if (onlyQualifierAnnotationsAllowed) {
                for (Annotation a : annotations) {
                    if (!isQualifierAnnotationPresent(a)) {
                        throw new InvalidKeyException("asdasd");
                    }
                }
            } else {
                int count = 0;
                for (int i = 0; i < annotations.length; i++) {
                    // Array is safe to modify. Or is it?
                    // I think we are sharing it
                    Annotation a = annotations[i];
                    if (isQualifierAnnotationPresent(a)) {
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
            }
            // Sort any qualifiers
            Arrays.sort(annotations, (a1, a2) -> {
                Class<? extends Annotation> a1t = a1.annotationType();
                Class<? extends Annotation> a2t = a2.annotationType();
                if (a1t == a2t) {
                    // We do not allow multiple qualifiers of the same type.
                    // Because we want a total order of the qualifiers based on their name
                    throw new InvalidKeyException(makeMsg(sourceKind, source, "Cannot use multiple qualifiers of the same type"));
                }
                int c = a1t.getSimpleName().compareTo(a2t.getSimpleName());
                if (c != 0) {
                    return c;
                }
                c = a1t.getCanonicalName().compareTo(a2t.getCanonicalName());
                if (c != 0) {
                    return c;
                }
                // We do not allow multiple qualifiers with different class loaders but with the same canonical name.
                // This is mainly to allow have a total order between qualifiers
                // This is only ever a problem if manually constructing the key
                throw new InvalidKeyException(makeMsg(sourceKind, source, "Cannot use multiple qualifiers with the same canonical name"));
            });
            yield new PackedAnnotationList(annotations);
        }
        };
    }

    private static Type convertType(Type t, int conversionType, Object source) {
        if (t instanceof Class<?> cl) {
            if (cl.isPrimitive()) {
                t = ClassUtil.wrap(cl);
            }
        }
        Class<?> rawType = TypeUtil.rawTypeOf(t);
        if (FORBIDDEN_KEY_TYPES.contains(rawType)) {
            throw new InvalidKeyException(t + " ");
        }

        if (!TypeUtil.isFreeFromTypeVariables(t)) {
            throw new InvalidKeyException("Can only convert type literals that are free from type variables to a Key, however TypeVariable<" + t.toString()
                    + "> defined: " + TypeUtil.typeVariableNamesOf(t));
        }

        // TODO reduce wildcards

        return convertType0(source, t, t);
    }

    static Type convertType0(Object source, Type originalType, Type type) {
        requireNonNull(type, "type is null");
        if (type instanceof Class<?>) {
            return type;
        } else if (type instanceof ParameterizedType pt) {
            Type rawType = convertType0(source, originalType, pt.getRawType());

            Type[] args = pt.getActualTypeArguments();
            for (int i = 0; i < args.length; i++) {
                args[i] = convertType0(source, originalType, args[i]);
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
     *
     * <p>
     * The main distinction between this method and {@link #of(Class)} is that these method also reads any qualifier
     * annotations on the specified class.
     *
     * @param <T>
     *            the type of key
     * @param clazz
     *            the clazz to convert to a key
     * @return the new key
     *
     * @see #of(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> Key<T> fromClass(Class<T> clazz) {
        return (Key<T>) convert(clazz, clazz.getAnnotations(), false, FROM_CLASS, clazz);
    }

    /**
     * Returns a key matching the type of the specified field and any qualifiers that may be present on the field.
     * <p>
     * The type of the returned key is determined by {@link Field#getType()} and qualifiers are read from
     * {@link Field#getAnnotations()}. Ignoring any annotations that do not have a {@link Qualifier} meta annotation.
     *
     * @param field
     *            the field to return a key for
     * @return a key representing the type of the field and any qualifiers that may be present on the field
     * @throws InvalidKeyException
     *             if the field does not represent a valid key. For example, if the field's type is an optional type such as
     *             {@link Optional} or {@link OptionalInt}.
     * @see Field#getType()
     * @see Field#getAnnotations()
     */
    public static Key<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        return convert(field.getGenericType(), field.getAnnotations(), false, FROM_FIELD, field);
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
    public static Key<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        return convert(method.getGenericReturnType(), method.getAnnotations(), false, FROM_METHOD_RETURN_TYPE, method);
    }

    public static Key<?> fromParamaterizedTypes(Class<?> rawType, Type... typeArguments) {
        ParameterizedType p = Types.createNewParameterizedType(rawType, typeArguments);
        Variable v = Variable.of(p);
        return v.toKey();
    }

    public static Key<?> fromVariable(Variable variable) {
        return convert(variable.type(), variable.annotations().toArray(), false, FROM_VARIABLE, variable);
    }

    private static boolean isQualifierAnnotationPresent(Annotation a) {
        // Is here because we might use a ClassValue at some point
        return a.annotationType().isAnnotationPresent(Qualifier.class);
    }

    private static String makeMsg(int sourceKind, Object source, String msg) {
        return msg;
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
     *             if the specified class does not represent a valid key\\
     * @see #fromClass(Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> key) {
        requireNonNull(key, "key is null");
        return (Key<T>) CLASS_TO_KEY_CACHE.get(key);
    }

    static <T> Key<T> of(Class<T> key, Annotation... qualifiers) {
        // maybe just have of(key).withQualifiers(...)
        Key<T> k = of(key);
        PackedAnnotationList pal = convertQualifiers(qualifiers.clone(), true, FROM_CLASS, null);
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

    /** A key that has not been explicitly constructed using {@link Key#Key()}. */
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
///**
// * Sorts the array of qualifiers while at same time checking some invariants.
// *
// * @param sourceKind
// *            the source kind
// * @param source
// *            the type of source
// * @param annotations
// *            the annotations to sort
// * @return the sorted annotations
// * @throws InvalidKeyException
// *             if the specified array contains multiple qualifiers of the same type or with the same canonical name
// */
//private static Annotation[] qualifiersSort(Annotation[] annotations, int sourceKind, Object source) {
//    Arrays.sort(annotations, (a1, a2) -> {
//        Class<? extends Annotation> a1t = a1.annotationType();
//        Class<? extends Annotation> a2t = a2.annotationType();
//        if (a1t == a2t) {
//            // We do not allow multiple qualifiers of the same type.
//            // Because we want a total order of the qualifiers based on their name
//            throw new InvalidKeyException(makeMsg(sourceKind, source, "Cannot use multiple qualifiers of the same type"));
//        }
//        int c = a1t.getSimpleName().compareTo(a2t.getSimpleName());
//        if (c != 0) {
//            return c;
//        }
//        c = a1t.getCanonicalName().compareTo(a2t.getCanonicalName());
//        if (c != 0) {
//            return c;
//        }
//        // We do not allow multiple qualifiers with different class loaders but with the same canonical name.
//        // This is mainly to allow have a total order between qualifiers
//        // This is only ever a problem if manually constructing the key
//        throw new InvalidKeyException(makeMsg(sourceKind, source, "Cannot use multiple qualifiers with the same canonical name"));
//    });
//    return annotations;
//}

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