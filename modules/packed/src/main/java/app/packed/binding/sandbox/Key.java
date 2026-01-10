package app.packed.binding.sandbox;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.packed.bean.BeanIntrospector.OnVariable;
import app.packed.binding.InvalidKeyException;
import app.packed.binding.Qualifier;
import app.packed.binding.Variable;
import app.packed.binding.sandbox.KeySource.ClassSource;
import app.packed.binding.sandbox.KeySource.FieldSource;
import app.packed.binding.sandbox.KeySource.MethodReturnSource;
import app.packed.binding.sandbox.KeySource.TypeCaptureSource;
import app.packed.binding.sandbox.KeySource.VariableSource;
import app.packed.util.AnnotationList;
import internal.app.packed.bean.scanning.IntrospectorOnVariable;
import internal.app.packed.util.PackedAnnotationList;
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
 * <li>Not be one of the following types: {@link Provider}, {@link Void}, {@link Key}, {@link Optional},
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

// En reacto

// Maybe split it up in
// KeyFactory
// KeyTransformer (With)
// InternalKey
// KeyInfo

//Created by user a.la
//Key.of
//Created while parsing a bean a.la.
//public void foo(@SomeInvalidQualifier Void k)

// Open questions
// how does adding qualifiers work. Replace vs fail on already existing qualifier

// To refactor

// I think assume safe types for now

// We need to add Javadoc back
public abstract class Key<T> {

    private static final ClassValue<Key<?>> CAPTURED_KEY_CACHE = new ClassValue<>() {
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(Key.class);

        @Override
        protected Key<?> computeValue(Class<?> implementation) {
            try {
                Variable v = EXTRACTOR.extractVariable(implementation, e -> new InvalidKeyException(e));
                KeySource source = new TypeCaptureSource(implementation);
                return buildKey(v.type(), v.annotations().toArray(), source);
            } catch (Exception e) {
                throw new InvalidKeyException("Failed to extract type information from anonymous Key subclass", e);
            }
        }
    };

    private static final ClassValue<Key<?>> CLASS_TO_KEY_CACHE = new ClassValue<>() {
        @Override
        protected Key<?> computeValue(Class<?> key) {
            return buildKey(key, new Annotation[0], new ClassSource(key));
        }
    };

    private final KeyInternal internal;

    protected Key() {
        Key<?> cached = CAPTURED_KEY_CACHE.get(getClass());
        this.internal = cached.internal;
    }

    private Key(KeyInternal internal) {
        this.internal = requireNonNull(internal);
    }

    private static Key<?> buildKey(Type type, Annotation[] annotations, KeySource source) {
        try {
            Type processedType = validateAndProcessType(type, source);
            PackedAnnotationList qualifiers = processQualifiers(annotations, source);
            int hash = processedType.hashCode() ^ qualifiers.hashCode();
            return new CanonicalizedKey<>(new KeyInternal(processedType, qualifiers, hash));
        } catch (Exception e) {
            throw new InvalidKeyException("Failed to create key from " + source.describe(), e);
        }
    }

    private static Type validateAndProcessType(Type type, KeySource source) {
        if (type instanceof Class<?> cl && cl.isPrimitive()) {
            type = ClassUtil.box(cl);
        }

        Class<?> rawType = TypeUtil.rawTypeOf(type);
        if (KeyInternal.FORBIDDEN_KEY_TYPES.contains(rawType)) {
            throw new InvalidKeyException("Type " + type + " is not allowed in " + source.describe());
        }

        if (!TypeUtil.isFreeFromTypeVariables(type)) {
            throw new InvalidKeyException("Type variables not allowed in " + source.describe() +
                                       ". Found: " + TypeUtil.typeVariableNamesOf(type));
        }

        return convertType0(type, type);
    }

    private static PackedAnnotationList processQualifiers(Annotation[] annotations, KeySource source) {
        if (annotations.length == 0) {
            return PackedAnnotationList.EMPTY;
        }

        List<Annotation> qualifiers = new ArrayList<>();
        Set<Class<?>> seenTypes = new HashSet<>();
        Set<String> seenNames = new HashSet<>();

        for (Annotation ann : annotations) {
            if (ann.annotationType().isAnnotationPresent(Qualifier.class)) {
                Class<? extends Annotation> type = ann.annotationType();

                if (!seenTypes.add(type)) {
                    throw new InvalidKeyException("Multiple qualifiers of type @" +
                        type.getSimpleName() + " found in " + source.describe());
                }

                String canonicalName = type.getCanonicalName();
                if (!seenNames.add(canonicalName)) {
                    throw new InvalidKeyException("Multiple qualifiers with canonical name " +
                        canonicalName + " found in " + source.describe());
                }

                qualifiers.add(ann);
            }
        }

        if (qualifiers.isEmpty()) {
            return PackedAnnotationList.EMPTY;
        }

        qualifiers.sort((a1, a2) -> {
            int c = a1.annotationType().getSimpleName()
                     .compareTo(a2.annotationType().getSimpleName());
            return c != 0 ? c :
                a1.annotationType().getCanonicalName()
                  .compareTo(a2.annotationType().getCanonicalName());
        });

        return new PackedAnnotationList(qualifiers.toArray(new Annotation[0]));
    }

    private static Type convertType0(Type originalType, Type type) {
        requireNonNull(type, "type is null");
        return switch (type) {
            case Class<?> cl -> type;
            case ParameterizedType pt -> {
                Type rawType = convertType0(originalType, pt.getRawType());
                Type[] args = pt.getActualTypeArguments();
                for (int i = 0; i < args.length; i++) {
                    args[i] = convertType0(originalType, args[i]);
                }
                yield Types.createNewParameterizedType(rawType, args);
            }
            case GenericArrayType gat -> gat;
            case TypeVariable<?> tv -> throw new InvalidKeyException("Type variables not allowed: " + type);
            case WildcardType wt -> {
                Type[] lowerBounds = wt.getLowerBounds();
                Type t = lowerBounds.length == 0 ? wt.getUpperBounds()[0] : lowerBounds[0];
                if (t == null) {
                    throw new InvalidKeyException("Invalid wildcard bounds in " + originalType);
                }
                yield convertType0(originalType, t);
            }
            default -> throw new InvalidKeyException("Unknown type: " + type);
        };
    }

    public static Key<?> fromBindableVariable(OnVariable variable) {
        IntrospectorOnVariable v = (IntrospectorOnVariable) variable;
        return buildKey(v.variable().type(), v.variable().annotations().toArray(),
                       new VariableSource(v.variable()));
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> fromClass(Class<T> clazz) {
        requireNonNull(clazz, "class is null");
        return (Key<T>) buildKey(clazz, clazz.getAnnotations(), new ClassSource(clazz));
    }

    public static Key<?> fromField(Field field) {
        requireNonNull(field, "field is null");
        return buildKey(field.getGenericType(), field.getAnnotations(), new FieldSource(field));
    }

    public static Key<?> fromMethodReturnType(Method method) {
        requireNonNull(method, "method is null");
        return buildKey(method.getGenericReturnType(), method.getAnnotations(),
                       new MethodReturnSource(method));
    }

    public static Key<?> fromVariable(Variable variable) {
        return buildKey(variable.type(), variable.annotations().toArray(),
                       new VariableSource(variable));
    }

    @SuppressWarnings("unchecked")
    public static <T> Key<T> of(Class<T> key) {
        requireNonNull(key, "key is null");
        return (Key<T>) CLASS_TO_KEY_CACHE.get(key);
    }

    public final Key<T> canonicalize() {
        return getClass() == CanonicalizedKey.class ? this : new CanonicalizedKey<>(internal);
    }

    @Override
    public final boolean equals(Object obj) {
        return obj == this || obj instanceof Key<?> key && internal.equals(key.internal);
    }

    @Override
    public final int hashCode() {
        return internal.hash();
    }

    public final boolean isQualified() {
        return internal.isQualified();
    }

    public final boolean isQualifiedWith(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        return qualifiers().isPresent(qualifier);
    }

    public final boolean isQualifiedWithType(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType is null");
        return qualifiers().isPresent(qualifierType);
    }

    final boolean isSimpleClassKey(Class<?> c) {
        return internal.type() == c && !isQualified();
    }

    public final int qualifierCount() {
        return qualifiers().size();
    }

    public final AnnotationList qualifiers() {
        return internal.qualifiers();
    }

    public final Class<?> rawType() {
        return TypeUtil.rawTypeOf(internal.type());
    }

    @Override
    public final String toString() {
        return internal.toString(false);
    }

    public final String toStringLong() {
        return internal.toString(true);
    }

    public final Type type() {
        return internal.type();
    }

    public final Key<T> withoutQualifierOfType(Class<? extends Annotation> qualifierType) {
        requireNonNull(qualifierType, "qualifierType");
        Annotation[] annotations = internal.qualifiers().annotations();
        for (int i = 0; i < annotations.length; i++) {
            if (annotations[i].annotationType() == qualifierType) {
                if (annotations.length == 1) {
                    return new CanonicalizedKey<>(new KeyInternal(internal.type(),
                        PackedAnnotationList.EMPTY, internal.type().hashCode()));
                }
                Annotation[] newAnnotations = new Annotation[annotations.length - 1];
                System.arraycopy(annotations, 0, newAnnotations, 0, i);
                System.arraycopy(annotations, i + 1, newAnnotations, i, annotations.length - i - 1);
                PackedAnnotationList newQualifiers = new PackedAnnotationList(newAnnotations);
                int newHash = internal.type().hashCode() ^ newQualifiers.hashCode();
                return new CanonicalizedKey<>(new KeyInternal(internal.type(), newQualifiers, newHash));
            }
        }
        return this;
    }

    public final Key<T> withoutQualifiers() {
        return isQualified() ? new CanonicalizedKey<>(internal.withoutQualifiers()) : this;
    }

    @SuppressWarnings("unchecked")
    public final Key<T> withQualifier(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        if (!qualifier.annotationType().isAnnotationPresent(Qualifier.class)) {
            throw new IllegalArgumentException("@" + qualifier.annotationType().getSimpleName() +
                " is not a valid qualifier - missing @Qualifier annotation");
        }

        Annotation[] current = internal.qualifiers().annotations();
        Annotation[] newQuals = Arrays.copyOf(current, current.length + 1);
        newQuals[current.length] = qualifier;

        return (Key<T>) buildKey(internal.type(), newQuals,
            new ClassSource(qualifier.annotationType())).canonicalize();
    }

    @SuppressWarnings("unchecked")
    public final <E> Key<E> withType(Class<E> type) {
        requireNonNull(type, "type is null");
        return (Key<E>) buildKey(type, internal.qualifiers().annotations(),
                                new ClassSource(type));
    }

    private static final class CanonicalizedKey<T> extends Key<T> {
        private CanonicalizedKey(KeyInternal internal) {
            super(internal);
        }
    }
}
