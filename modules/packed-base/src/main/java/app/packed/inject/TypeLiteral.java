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
import static packed.util.Formatter.format;
import static packed.util.Types.canonicalize;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.StringJoiner;

import packed.inject.JavaXInjectSupport;
import packed.util.Types;
import packed.util.Types.GenericArrayTypeImpl;

/**
 * Supports inline instantiation of objects that represent parameterized types with actual type parameters.
 *
 * An object that represents any parameterized type may be obtained by subclassing TypeLiteral.
 *
 * <pre>
 *     &#64;TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};
 * </pre>
 */
public class TypeLiteral<T> extends TypeLiteralOrKey<T> {

    /**
     * We cache the hashCode of the type field, as most Type implementations calculates it every time. See, for example,
     * https://github.com/frohoff/jdk8u-jdk/blob/master/src/share/classes/sun/reflect/generics/reflectiveObjects/ParameterizedTypeImpl.java
     */
    /** Cache the hash code for the string */
    private final int hash; // Default to 0

    /** The raw type. */
    private final Class<? super T> rawType;

    /** The underlying type. */
    private final Type type;

    /**
     * Constructs a new type literal. Derives represented class from type parameter.
     *
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type parameter in the anonymous class's type
     * hierarchy so we can reconstitute it at runtime despite erasure.
     */
    @SuppressWarnings("unchecked")
    protected TypeLiteral() {
        this.type = Types.getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) Types.findRawType(type);
        this.hash = type.hashCode();
    }

    /** Unsafe. Constructs a type literal manually. */
    @SuppressWarnings("unchecked")
    TypeLiteral(Type type) {
        this.type = canonicalize(requireNonNull(type, "type is null"));
        this.rawType = (Class<? super T>) Types.findRawType(this.type);
        this.hash = this.type.hashCode();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object obj) {
        return obj instanceof TypeLiteral && Types.equals(type, ((TypeLiteral<?>) obj).type);
    }

    /**
     * Returns the resolved generic exception types thrown by {@code constructor}.
     *
     * @param executable
     *            an executable defined by this or any of its super types.
     */
    public final List<TypeLiteral<?>> getExceptionTypes(Executable executable) {
        return resolveMultiple(executable, executable.getGenericExceptionTypes());
    }

    /**
     * Returns the resolved generic type of the specified {@code field}.
     *
     * @param field
     *            a field defined by this, or any or its superclasses
     * @throws IllegalArgumentException
     *             if the specified field is not defined by this, or any or its superclasses
     */
    public final TypeLiteral<?> getFieldType(Field field) {
        if (!field.getDeclaringClass().isAssignableFrom(rawType)) {
            throw new IllegalArgumentException(format(field) + " is not defined by a supertype of " + type);
        }
        return of(resolveType(field.getGenericType()));
    }

    /**
     * Returns the resolved generic parameter types of the specified {@code executable}.
     *
     * @param executable
     *            a executable defined by this, or any or its super types
     * @throws IllegalArgumentException
     *             if the specified executable is not defined by this, or any of its super types
     */
    public final List<TypeLiteral<?>> getParameterTypes(Executable executable) {
        return resolveMultiple(executable, executable.getGenericParameterTypes());
    }

    /**
     * Returns the raw (non-generic) type.
     *
     * @return the raw (non-generic) type
     */
    @Override
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /**
     * Returns the resolved generic return type of the specified {@code method}.
     *
     * @param method
     *            a method defined by this, or any or its super types
     * @throws IllegalArgumentException
     *             if the specified method is not defined by this, or any of its super types
     */

    // class Stuff<T> {
    // @Provides T create();
    // }
    // .install(new TypeLiteral<Stuff<Integer>>).. Provides makes lists of ints..

    public final TypeLiteral<?> getReturnType(Method method) {
        if (!method.getDeclaringClass().isAssignableFrom(rawType)) {
            throw new IllegalArgumentException(format(method) + " is not defined by " + format(getRawType()) + " or any of its supertypes");
        }
        return of(resolveType(method.getGenericReturnType()));
    }

    /**
     * Returns the generic form of {@code supertype}. For example, if this is {@code
     * ArrayList<String>}, this returns {@code Iterable<String>} given the input {@code
     * Iterable.class}.
     *
     * @param supertype
     *            a superclass of, or interface implemented by, this.
     * @since 2.0
     */
    public final TypeLiteral<?> getSupertype(Class<?> supertype) {
        if (!supertype.isAssignableFrom(rawType)) {
            throw new IllegalArgumentException(supertype + " is not a supertype of " + type);
        }

        return of(resolveType(Types.getGenericSupertype(type, rawType, supertype)));
    }

    /**
     * Returns the underlying type instance.
     *
     * @return the underlying type instance
     */
    public final Type getType() {
        return type;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return hash;
    }

    /** Returns an immutable list of the resolved types. */
    private List<TypeLiteral<?>> resolveMultiple(Executable executable, Type[] types) {
        if (!executable.getDeclaringClass().isAssignableFrom(rawType)) {
            if (executable instanceof Method) {
                throw new IllegalArgumentException(executable + " is not defined by a supertype of " + type);
            } else {
                throw new IllegalArgumentException(executable + " does not construct a supertype of " + type);
            }
        }
        TypeLiteral<?>[] result = new TypeLiteral<?>[types.length];
        for (int t = 0; t < types.length; t++) {
            result[t] = of(resolveType(types[t]));
        }
        return List.of(result);
    }

    Type resolveType(Type type) {
        // this implementation is made a little more complicated in an attempt to avoid object-creation
        Type toResolve = type;
        while (true) {
            if (toResolve instanceof TypeVariable) {
                TypeVariable<?> original = (TypeVariable<?>) toResolve;
                toResolve = Types.resolveTypeVariable(type, rawType, original);
                if (toResolve == original) {
                    return toResolve;
                }

            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType original = (GenericArrayType) toResolve;
                Type componentType = original.getGenericComponentType();
                Type newComponentType = resolveType(componentType);
                return componentType == newComponentType ? original : new GenericArrayTypeImpl(componentType);

            } else if (toResolve instanceof ParameterizedType) {
                ParameterizedType original = (ParameterizedType) toResolve;
                Type ownerType = original.getOwnerType();
                Type newOwnerType = resolveType(ownerType);
                boolean changed = newOwnerType != ownerType;

                Type[] args = original.getActualTypeArguments();
                for (int t = 0, length = args.length; t < length; t++) {
                    Type resolvedTypeArgument = resolveType(args[t]);
                    if (resolvedTypeArgument != args[t]) {
                        if (!changed) {
                            args = args.clone();
                            changed = true;
                        }
                        args[t] = resolvedTypeArgument;
                    }
                }

                return changed ? Types.createNewParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args) : original;

            } else if (toResolve instanceof WildcardType) {
                WildcardType original = (WildcardType) toResolve;
                Type[] originalLowerBound = original.getLowerBounds();
                Type[] originalUpperBound = original.getUpperBounds();

                if (originalLowerBound.length == 1) {
                    Type lowerBound = resolveType(originalLowerBound[0]);
                    if (lowerBound != originalLowerBound[0]) {
                        return Types.createSupertypeOf(lowerBound);
                    }
                } else if (originalUpperBound.length == 1) {
                    Type upperBound = resolveType(originalUpperBound[0]);
                    if (upperBound != originalUpperBound[0]) {
                        return Types.createSubtypeOf(upperBound);
                    }
                }
                return original;

            } else {
                return toResolve;
            }
        }
    }

    /**
     * Returns a canonical representation of this type literal. This method works indentical to {@link String#intern()}.
     *
     * @return a type literal that has the same contents as this type literal, but is guaranteed to be from a pool of unique
     *         type literal.
     */
    final TypeLiteral<T> intern() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Key<T> toKey() {
        return new Key<T>(this, null) {};
    }

    public Key<T> toKey(Annotation qualifier) {
        requireNonNull(qualifier, "qualifier is null");
        JavaXInjectSupport.checkQualifierAnnotationPresent(qualifier);
        return new Key<T>(this, qualifier) {};
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return Types.typeToString(type);
    }

    /**
     * Returns a type literal from the specified class.
     *
     * @param type
     *            the class instance to return a type literal for
     * @return a type literal from the specified class
     * @see #of(Type)
     */
    public static <T> TypeLiteral<T> of(Class<T> type) {
        return new TypeLiteral<>(type);
    }

    /**
     * Returns a type literal from the specified type.
     *
     * @param type
     *            the class instance to return a type literal for
     * @return a type literal from the specified type
     * @see #of(Class)
     */
    public static TypeLiteral<?> of(Type type) {
        return new TypeLiteral<>(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> TypeLiteral<T> getTypeOfArgument(Class<T> superClass, int parameterIndex, Class<? extends T> subClass) {
        Type t = GenericsUtil.getTypeOfArgument(superClass, subClass, parameterIndex);
        if (t instanceof TypeVariable) {
            Class<?> component = subClass.getSuperclass();
            StringBuilder sb = new StringBuilder();
            TypeVariable<?>[] typeparms = component.getTypeParameters();
            if (typeparms.length > 0) {
                StringJoiner sj = new StringJoiner(",", "<", ">");
                for (TypeVariable<?> typeparm : typeparms) {
                    sj.add(typeparm.getTypeName());
                }
                sb.append(sj.toString());
            }
            throw new IllegalArgumentException(
                    "Could not determine the type variable <" + t + "> of " + component.getSimpleName() + sb + " for " + format(subClass));
        }
        return (TypeLiteral<T>) TypeLiteral.of(t);
    }
    //
    // public static TypeLiteral<?> fromTypeVariable(Class<?> superClass, int index, Class<?> subClass) {
    // if (subClass.getSuperclass() != superClass) {
    // throw new IllegalArgumentException(
    // format(subClass) + " must extend " + format(superClass) + " directly, extended " + format(subClass.getSuperclass()) +
    // " instead");
    // }
    //
    // Type superclass = subClass.getGenericSuperclass();
    // if (!(superclass instanceof ParameterizedType)) {
    // throw new IllegalArgumentException(format(subClass) + " does not specify any type parameters");
    // }
    // // TODO fail if we have @XXX, cannot use annotations on TypeLiterals
    // Type t = Types.canonicalize(((ParameterizedType) superclass).getActualTypeArguments()[index]);
    // return of(t);
    // }
}
