/*
 * Copyright (C) 2008 Google Inc.
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

package packed.util;

import static java.util.Objects.requireNonNull;
import static packed.util.Formatter.format;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import app.packed.inject.Provider;
import app.packed.inject.TypeLiteral;
import packed.inject.InjectAPI;

/**
 * Static methods for working with types that we aren't publishing in the public {@code Types} API.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 */
@SuppressWarnings("all")
public class Types {

    /** */
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[] {};

    private static final Map<TypeLiteral<?>, TypeLiteral<?>> PRIMITIVE_TO_WRAPPER = Map.of(TypeLiteral.of(boolean.class), TypeLiteral.of(Boolean.class),
            TypeLiteral.of(byte.class), TypeLiteral.of(Byte.class), TypeLiteral.of(short.class), TypeLiteral.of(Short.class), TypeLiteral.of(int.class),
            TypeLiteral.of(Integer.class), TypeLiteral.of(long.class), TypeLiteral.of(Long.class), TypeLiteral.of(float.class), TypeLiteral.of(Float.class),
            TypeLiteral.of(double.class), TypeLiteral.of(Double.class), TypeLiteral.of(char.class), TypeLiteral.of(Character.class), TypeLiteral.of(void.class),
            TypeLiteral.of(Void.class));

    private Types() {}

    /**
     * Returns a type that is functionally equal but not necessarily equal according to {@link Object#equals(Object)
     * Object.equals()}. The returned type is {@link Serializable}.
     */
    public static Type canonicalize(Type type) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            return c.isArray() ? new GenericArrayTypeImpl(canonicalize(c.getComponentType())) : c;
        } else if (type instanceof CompositeType) {
            return type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType p = (ParameterizedType) type;
            return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments());
        } else if (type instanceof GenericArrayType) {
            return new GenericArrayTypeImpl(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            return new WildcardTypeImpl(w.getUpperBounds(), w.getLowerBounds());
        }
        return type; // default, for example, CompositeType

    }

    /**
     * Returns an type that's appropriate for use in a key.
     *
     * <p>
     * If the raw type of {@code typeLiteral} is a {@code javax.inject.Provider}, this returns a
     * {@code org.cakeframework.reflect.Provider} with the same type parameters.
     *
     * <p>
     * If the type is a primitive, the corresponding wrapper type will be returned.
     *
     * @throws RuntimeException
     *             if {@code type} contains a type variable
     */
    public static <T> TypeLiteral<T> canonicalizeForKey(TypeLiteral<T> typeLiteral) {
        Type type = typeLiteral.getType();
        if (!isFreeFromTypeVariables(type)) {

            // Errors errors = new Errors().keyNotFullySpecified(typeLiteral);
            throw new RuntimeException("Key not fully specified, key = " + type);
        }

        if (typeLiteral.getRawType() == Provider.class) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            // the following casts are generally unsafe, but org.cakeframework.reflect.Provider extends
            // javax.inject.Provider and is covariant
            @SuppressWarnings("unchecked")
            TypeLiteral<T> guiceProviderType = (TypeLiteral<T>) InjectAPI.toTypeLiteral(createProviderOf(parameterizedType.getActualTypeArguments()[0]));
            return guiceProviderType;
        }

        @SuppressWarnings("unchecked")
        TypeLiteral<T> wrappedPrimitives = (TypeLiteral<T>) PRIMITIVE_TO_WRAPPER.get(typeLiteral);
        if (wrappedPrimitives != null) {
            return wrappedPrimitives;
        }

        // If we know this isn't a subclass, return as-is.
        if (typeLiteral.getClass() == TypeLiteral.class) {
            return typeLiteral;
        }

        // recreate the TypeLiteral to avoid anonymous TypeLiterals from holding refs to their
        // surrounding classes.
        @SuppressWarnings("unchecked")
        TypeLiteral<T> recreated = (TypeLiteral<T>) InjectAPI.toTypeLiteral(typeLiteral.getType());
        return recreated;
    }

    // /**
    // * Returns a key that doesn't hold any references to parent classes. This is necessary for anonymous keys, so ensure
    // we
    // * don't hold a ref to the containing module (or class) forever.
    // */
    // public static <T> Key<T> canonicalizeKey(Key<T> key) {
    // // If we know this isn't a subclass, return as-is.
    // // Otherwise, recreate the key to avoid the subclass
    // if (key.getClass() == Key.class) {
    // return key;
    // } else if (key.getQualifier() != null) {
    // return Key.of(key.getTypeLiteral(), key.getQualifier());
    // } else {
    // return Key.of(key.getTypeLiteral());
    // }
    //
    // }

    private static void checkNotPrimitive(Type type, String use) {
        // checkArgument(
        // !(type instanceof Class<?>) || !((Class) type).isPrimitive(),
        // "Primitive types are not allowed in %s: %s",
        // use,
        // type);
    }

    /**
     * Returns an array type whose elements are all instances of {@code componentType}.
     *
     * @return a {@link java.io.Serializable serializable} generic array type.
     */
    public static GenericArrayType createArrayOf(Type componentType) {
        return new GenericArrayTypeImpl(componentType);
    }

    /**
     * Returns a type modelling a {@link Collection} whose elements are of type {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createCollectionOf(Type elementType) {
        return createNewParameterizedType(Collection.class, elementType);
    }

    /**
     * Returns a type modelling a {@link List} whose elements are of type {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createListOf(Type elementType) {
        return createNewParameterizedType(List.class, elementType);
    }

    /**
     * Returns a type modelling a {@link Map} whose keys are of type {@code keyType} and whose values are of type
     * {@code valueType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createMapOf(Type keyType, Type valueType) {
        return createNewParameterizedType(Map.class, keyType, valueType);
    }

    /**
     * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType}. The returned type does not have
     * an owner type.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createNewParameterizedType(Type rawType, Type... typeArguments) {
        return createNewParameterizedTypeWithOwner(null, rawType, typeArguments);
    }

    /**
     * Returns a new parameterized type, applying {@code typeArguments} to {@code rawType} and enclosed by
     * {@code ownerType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createNewParameterizedTypeWithOwner(Type ownerType, Type rawType, Type... typeArguments) {
        return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);
    }

    /**
     * Returns a type modelling a {@link Provider} that provides elements of type {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createProviderOf(Type providedType) {
        return createNewParameterizedType(Provider.class, providedType);
    }

    /**
     * Returns a type modelling a {@link Set} whose elements are of type {@code elementType}.
     *
     * @return a {@link java.io.Serializable serializable} parameterized type.
     */
    public static ParameterizedType createSetOf(Type elementType) {
        return createNewParameterizedType(Set.class, elementType);
    }

    /**
     * Returns a type that represents an unknown type that extends {@code bound}. For example, if {@code bound} is
     * {@code CharSequence.class}, this returns {@code ? extends CharSequence}. If {@code bound} is {@code Object.class},
     * this returns {@code ?}, which is shorthand for {@code ?
     * extends Object}.
     */
    public static WildcardType createSubtypeOf(Type bound) {
        return new WildcardTypeImpl(new Type[] { bound }, Types.EMPTY_TYPE_ARRAY);
    }

    /**
     * Returns a type that represents an unknown supertype of {@code bound}. For example, if {@code
     * bound} is {@code String.class}, this returns {@code ? super String}.
     */
    public static WildcardType createSupertypeOf(Type bound) {
        return new WildcardTypeImpl(new Type[] { Object.class }, new Type[] { bound });
    }

    /**
     * Returns the declaring class of {@code typeVariable}, or {@code null} if it was not declared by a class.
     */
    private static Class<?> declaringClassOf(TypeVariable typeVariable) {
        GenericDeclaration genericDeclaration = typeVariable.getGenericDeclaration();
        return genericDeclaration instanceof Class ? (Class<?>) genericDeclaration : null;
    }

    /** Returns true if {@code a} and {@code b} are equal. */
    public static boolean equals(Type a, Type b) {
        if (a == b) {
            return true;
        } else if (a instanceof Class) {
            return a == b;
        } else if (a instanceof ParameterizedType) {
            if (b instanceof ParameterizedType) {
                ParameterizedType pa = (ParameterizedType) a;
                ParameterizedType pb = (ParameterizedType) b;
                return Objects.equals(pa.getOwnerType(), pb.getOwnerType()) && pa.getRawType().equals(pb.getRawType())
                        && Arrays.equals(pa.getActualTypeArguments(), pb.getActualTypeArguments());
            }
        } else if (a instanceof GenericArrayType) {
            return b instanceof GenericArrayType && equals(((GenericArrayType) a).getGenericComponentType(), ((GenericArrayType) b).getGenericComponentType());
        } else if (a instanceof WildcardType) {
            if (b instanceof WildcardType) {
                WildcardType wa = (WildcardType) a;
                WildcardType wb = (WildcardType) b;
                return Arrays.equals(wa.getUpperBounds(), wb.getUpperBounds()) && Arrays.equals(wa.getLowerBounds(), wb.getLowerBounds());
            }
        } else if (a instanceof TypeVariable) {
            if (b instanceof TypeVariable) {
                TypeVariable<?> va = (TypeVariable) a;
                TypeVariable<?> vb = (TypeVariable) b;
                return va.getGenericDeclaration().equals(vb.getGenericDeclaration()) && va.getName().equals(vb.getName());
            }
        }
        return false; // Unsupported type or a=null && b!=null
    }

    /**
     * Returns the generic supertype for {@code type}. For example, given a class {@code IntegerSet}, the result for when
     * supertype is {@code Set.class} is {@code Set<Integer>} and the result when the supertype is {@code Collection.class}
     * is {@code Collection<Integer>}.
     */
    public static Type getGenericSupertype(Type type, Class<?> rawType, Class<?> toResolve) {
        if (toResolve == rawType) {
            return type;
        }

        // we skip searching through interfaces if unknown is an interface
        if (toResolve.isInterface()) {
            Class[] interfaces = rawType.getInterfaces();
            for (int i = 0, length = interfaces.length; i < length; i++) {
                if (interfaces[i] == toResolve) {
                    return rawType.getGenericInterfaces()[i];
                } else if (toResolve.isAssignableFrom(interfaces[i])) {
                    return getGenericSupertype(rawType.getGenericInterfaces()[i], interfaces[i], toResolve);
                }
            }
        }

        // check our supertypes
        if (!rawType.isInterface()) {
            while (rawType != Object.class) {
                Class<?> rawSupertype = rawType.getSuperclass();
                if (rawSupertype == toResolve) {
                    return rawType.getGenericSuperclass();
                } else if (toResolve.isAssignableFrom(rawSupertype)) {
                    return getGenericSupertype(rawType.getGenericSuperclass(), rawSupertype, toResolve);
                }
                rawType = rawSupertype;
            }
        }

        // we can't resolve this further
        return toResolve;
    }

    /**
     * Finds the raw class type for the specified type
     *
     * @param type
     *            the type to find the raw class from
     * @return the raw type
     */
    public static Class<?> findRawType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            return Array.newInstance(findRawType(((GenericArrayType) type).getGenericComponentType()), 0).getClass();
        } else if (type instanceof TypeVariable || type instanceof WildcardType) {
            return Object.class;// Best effort, maybe just fail??
        } else {
            throw new IllegalArgumentException("Cannot extract raw type from '" + type + "' of type: " + type.getClass().getName());
        }
    }

    private static int indexOf(Object[] array, Object toFind) {
        for (int i = 0; i < array.length; i++) {
            if (toFind.equals(array[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }

    /** Returns true if {@code type} is free from type variables. */
    private static boolean isFreeFromTypeVariables(Type type) {
        if (type instanceof Class) {
            return true;
        } else if (type instanceof CompositeType) {
            return ((CompositeType) type).isFreeFromTypeVariables();
        } else if (type instanceof TypeVariable) {
            return false;
        }
        return ((CompositeType) canonicalize(type)).isFreeFromTypeVariables();
    }

    public static Type resolveTypeVariable(Type type, Class<?> rawType, TypeVariable unknown) {
        Class<?> declaredByRaw = declaringClassOf(unknown);

        if (declaredByRaw != null) {
            Type declaredBy = getGenericSupertype(type, rawType, declaredByRaw);
            if (declaredBy instanceof ParameterizedType) {
                int index = indexOf(declaredByRaw.getTypeParameters(), unknown);
                return ((ParameterizedType) declaredBy).getActualTypeArguments()[index];
            }
        }
        return unknown;
    }

    public static String typeToString(Type type) {
        return type instanceof Class ? ((Class) type).getName() : type.toString();
    }

    /** A type formed from other types, such as arrays, parameterized types or wildcard types */
    private interface CompositeType {
        /** Returns true if there are no type variables in this type. */
        boolean isFreeFromTypeVariables();
    }

    /**
     * Returns the type from super class's type parameter in {@link Types#canonicalize(Type) canonical form}.
     */
    public static Type getSuperclassTypeParameter(Class<?> subclass) {
        return getSuperclassTypeParameter(subclass, 0);
    }

    public static Type getSuperclassTypeParameter(Class<?> subclass, int index) {
        Type superclass = subclass.getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType)) {
            throw new IllegalStateException(format(subclass) + " does not specify any type parameters");
        }
        return canonicalize(((ParameterizedType) superclass).getActualTypeArguments()[index]);
    }

    public static class GenericArrayTypeImpl implements GenericArrayType, Serializable, CompositeType {
        private static final long serialVersionUID = 0;

        private final Type componentType;

        public GenericArrayTypeImpl(Type componentType) {
            this.componentType = canonicalize(componentType);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GenericArrayType && Objects.equals(componentType, ((GenericArrayType) obj).getGenericComponentType());
        }

        @Override
        public Type getGenericComponentType() {
            return componentType;
        }

        @Override
        public int hashCode() {
            return componentType.hashCode();
        }

        @Override
        public boolean isFreeFromTypeVariables() {
            return Types.isFreeFromTypeVariables(componentType);
        }

        @Override
        public String toString() {
            return typeToString(componentType) + "[]";
        }
    }

    public static class ParameterizedTypeImpl implements ParameterizedType, Serializable, CompositeType {
        private static final long serialVersionUID = 0;
        private final Type ownerType;
        private final Type rawType;

        private final Type[] typeArguments;

        public ParameterizedTypeImpl(Type ownerType, Type rawType, Type... typeArguments) {
            // require an owner type if the raw type needs it
            ensureOwnerType(ownerType, rawType);

            this.ownerType = ownerType == null ? null : canonicalize(ownerType);
            this.rawType = canonicalize(rawType);
            this.typeArguments = typeArguments.clone();
            for (int t = 0; t < this.typeArguments.length; t++) {
                requireNonNull(this.typeArguments[t], "type parameter");
                checkNotPrimitive(this.typeArguments[t], "type parameters");
                this.typeArguments[t] = canonicalize(this.typeArguments[t]);
            }
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof ParameterizedType && Types.equals(this, (ParameterizedType) other);
        }

        @Override
        public Type[] getActualTypeArguments() {
            return typeArguments.clone();
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(typeArguments) ^ rawType.hashCode() ^ Objects.hashCode(ownerType);
        }

        @Override
        public boolean isFreeFromTypeVariables() {
            if (ownerType != null && !Types.isFreeFromTypeVariables(ownerType)) {
                return false;
            }

            if (!Types.isFreeFromTypeVariables(rawType)) {
                return false;
            }

            for (Type type : typeArguments) {
                if (!Types.isFreeFromTypeVariables(type)) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(30 * (typeArguments.length + 1));
            stringBuilder.append(typeToString(rawType));

            if (typeArguments.length == 0) {
                return stringBuilder.toString();
            }

            stringBuilder.append("<").append(typeToString(typeArguments[0]));
            for (int i = 1; i < typeArguments.length; i++) {
                stringBuilder.append(", ").append(typeToString(typeArguments[i]));
            }
            return stringBuilder.append(">").toString();
        }

        private static void ensureOwnerType(Type ownerType, Type rawType) {
            if (rawType instanceof Class<?>) {
                Class rawTypeAsClass = (Class) rawType;
                // checkArgument(
                // ownerType != null || rawTypeAsClass.getEnclosingClass() == null,
                // "No owner type for enclosed %s",
                // rawType);
                // checkArgument(
                // ownerType == null || rawTypeAsClass.getEnclosingClass() != null,
                // "Owner type for unenclosed %s",
                // rawType);
            }
        }
    }

    // for other custom collections types, use newParameterizedType()

    /**
     * The WildcardType interface supports multiple upper bounds and multiple lower bounds. We only support what the Java 6
     * language needs - at most one bound. If a lower bound is set, the upper bound must be Object.class.
     */
    public static class WildcardTypeImpl implements WildcardType, Serializable, CompositeType {
        private static final long serialVersionUID = 0;
        private final Type lowerBound;

        private final Type upperBound;

        public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
            // checkArgument(lowerBounds.length <= 1, "Must have at most one lower bound.");
            // checkArgument(upperBounds.length == 1, "Must have exactly one upper bound.");

            if (lowerBounds.length == 1) {
                requireNonNull(lowerBounds[0], "lowerBound");
                checkNotPrimitive(lowerBounds[0], "wildcard bounds");
                // checkArgument(upperBounds[0] == Object.class, "bounded both ways");
                this.lowerBound = canonicalize(lowerBounds[0]);
                this.upperBound = Object.class;

            } else {
                requireNonNull(upperBounds[0], "upperBound");
                checkNotPrimitive(upperBounds[0], "wildcard bounds");
                this.lowerBound = null;
                this.upperBound = canonicalize(upperBounds[0]);
            }
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof WildcardType && Types.equals(this, (WildcardType) other);
        }

        @Override
        public Type[] getLowerBounds() {
            return lowerBound != null ? new Type[] { lowerBound } : EMPTY_TYPE_ARRAY;
        }

        @Override
        public Type[] getUpperBounds() {
            return new Type[] { upperBound };
        }

        @Override
        public int hashCode() {
            // this equals Arrays.hashCode(getLowerBounds()) ^ Arrays.hashCode(getUpperBounds());
            return (lowerBound != null ? 31 + lowerBound.hashCode() : 1) ^ 31 + upperBound.hashCode();
        }

        @Override
        public boolean isFreeFromTypeVariables() {
            return Types.isFreeFromTypeVariables(upperBound) && (lowerBound == null || Types.isFreeFromTypeVariables(lowerBound));
        }

        @Override
        public String toString() {
            if (lowerBound != null) {
                return "? super " + typeToString(lowerBound);
            } else if (upperBound == Object.class) {
                return "?";
            } else {
                return "? extends " + typeToString(upperBound);
            }
        }
    }
}
