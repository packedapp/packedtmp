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

package packed.internal.thirdparty.guice;

import static java.util.Objects.requireNonNull;
import static packed.internal.thirdparty.guice.GMoreTypes.canonicalize;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to represent generic types, so this class does.
 * Forces clients to create a subclass of this class which enables retrieval of the type information even at runtime.
 *
 * <p>
 * For example, to create a type literal for {@code List<String>}, you can create an empty anonymous inner class:
 *
 * <p>
 * {@code TypeLiteral<List<String>> list = new TypeLiteral<List<String>>() {};}
 *
 * <p>
 * Along with modeling generic types, this class can resolve type parameters. For example, to figure out what type
 * {@code keySet()} returns on a {@code Map<Integer, String>}, use this code:
 *
 * <pre>{@code
 * TypeLiteral<Map<Integer, String>> mapType
 *     = new TypeLiteral<Map<Integer, String>>() {};
 * TypeLiteral<?> keySetType
 *     = mapType.getReturnType(Map.class.getMethod("keySet"));
 * System.out.println(keySetType); // prints "Set<Integer>"
 * }</pre>
 *
 * @author crazybob@google.com (Bob Lee)
 * @author jessewilson@google.com (Jesse Wilson)
 */
public class GTypeLiteral<T> {

    final Class<? super T> rawType;
    final Type type;
    final int hashCode;

    /**
     * Constructs a new type literal. Derives represented class from type parameter.
     *
     * <p>
     * Clients create an empty anonymous subclass. Doing so embeds the type parameter in the anonymous class's type
     * hierarchy so we can reconstitute it at runtime despite erasure.
     */
    @SuppressWarnings("unchecked")
    protected GTypeLiteral() {
        this.type = getSuperclassTypeParameter(getClass());
        this.rawType = (Class<? super T>) GMoreTypes.getRawType(type);
        this.hashCode = type.hashCode();
    }

    /** Unsafe. Constructs a type literal manually. */
    @SuppressWarnings("unchecked")
    GTypeLiteral(Type type) {
        this.type = canonicalize(requireNonNull(type, "type"));
        this.rawType = (Class<? super T>) GMoreTypes.getRawType(this.type);
        this.hashCode = this.type.hashCode();
    }

    /**
     * Returns the type from super class's type parameter in {@link GMoreTypes#canonicalize(Type) canonical form}.
     */
    static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("Missing type parameter.");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    /** Gets type literal from super class's type parameter. */
    static GTypeLiteral<?> fromSuperclassTypeParameter(Class<?> subclass) {
        return new GTypeLiteral<Object>(getSuperclassTypeParameter(subclass));
    }

    /**
     * Returns the raw (non-generic) type for this type.
     *
     * @since 2.0
     */
    public final Class<? super T> getRawType() {
        return rawType;
    }

    /** Gets underlying {@code Type} instance. */
    public final Type getType() {
        return type;
    }

    // /** Gets the type of this type's provider. */
    // @SuppressWarnings("unchecked")
    // final TypeLiteralGuice<Provider<T>> providerType() {
    // // This cast is safe and wouldn't generate a warning if Type had a type
    // // parameter.
    // return (TypeLiteralGuice<Provider<T>>) get(Types.providerOf(getType()));
    // }

    @Override
    public final int hashCode() {
        return this.hashCode;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public final boolean equals(Object o) {
        return o instanceof GTypeLiteral<?> && GMoreTypes.equals(type, ((GTypeLiteral) o).type);
    }

    @Override
    public final String toString() {
        return GMoreTypes.typeToString(type);
    }

    /** Gets type literal for the given {@code Type} instance. */
    public static GTypeLiteral<?> get(Type type) {
        return new GTypeLiteral<Object>(type);
    }

    /** Gets type literal for the given {@code Class} instance. */
    public static <T> GTypeLiteral<T> get(Class<T> type) {
        return new GTypeLiteral<T>(type);
    }

    /** Returns an immutable list of the resolved types. */
    private List<GTypeLiteral<?>> resolveAll(Type[] types) {
        GTypeLiteral<?>[] result = new GTypeLiteral<?>[types.length];
        for (int t = 0; t < types.length; t++) {
            result[t] = resolve(types[t]);
        }
        return List.of(result);
    }

    /** Resolves known type parameters in {@code toResolve} and returns the result. */
    GTypeLiteral<?> resolve(Type toResolve) {
        return GTypeLiteral.get(resolveType(toResolve));
    }

    public Type resolveType(Type toResolve) {
        // this implementation is made a little more complicated in an attempt to avoid object-creation
        while (true) {
            if (toResolve instanceof TypeVariable) {
                @SuppressWarnings("rawtypes")
                TypeVariable original = (TypeVariable) toResolve;
                toResolve = GMoreTypes.resolveTypeVariable(type, rawType, original);
                if (toResolve == original) {
                    return toResolve;
                }

            } else if (toResolve instanceof GenericArrayType) {
                GenericArrayType original = (GenericArrayType) toResolve;
                Type componentType = original.getGenericComponentType();
                Type newComponentType = resolveType(componentType);
                return componentType == newComponentType ? original : GTypes.arrayOf(newComponentType);

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

                return changed ? GTypes.newParameterizedTypeWithOwner(newOwnerType, original.getRawType(), args) : original;

            } else if (toResolve instanceof WildcardType) {
                WildcardType original = (WildcardType) toResolve;
                Type[] originalLowerBound = original.getLowerBounds();
                Type[] originalUpperBound = original.getUpperBounds();

                if (originalLowerBound.length == 1) {
                    Type lowerBound = resolveType(originalLowerBound[0]);
                    if (lowerBound != originalLowerBound[0]) {
                        return GTypes.supertypeOf(lowerBound);
                    }
                } else if (originalUpperBound.length == 1) {
                    Type upperBound = resolveType(originalUpperBound[0]);
                    if (upperBound != originalUpperBound[0]) {
                        return GTypes.subtypeOf(upperBound);
                    }
                }
                return original;

            } else {
                return toResolve;
            }
        }
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
    public GTypeLiteral<?> getSupertype(Class<?> supertype) {
        // checkArgument(
        // supertype.isAssignableFrom(rawType), "%s is not a supertype of %s", supertype, this.type);
        return resolve(GMoreTypes.getGenericSupertype(type, rawType, supertype));
    }

    /**
     * Returns the resolved generic type of {@code field}.
     *
     * @param field
     *            a field defined by this or any superclass.
     * @since 2.0
     */
    public GTypeLiteral<?> getFieldType(Field field) {
        // checkArgument(
        // field.getDeclaringClass().isAssignableFrom(rawType),
        // "%s is not defined by a supertype of %s",
        // field,
        // type);
        return resolve(field.getGenericType());
    }

    /**
     * Returns the resolved generic parameter types of {@code methodOrConstructor}.
     *
     * @param methodOrConstructor
     *            a method or constructor defined by this or any supertype.
     * @since 2.0
     */
    public List<GTypeLiteral<?>> getParameterTypes(Member methodOrConstructor) {
        Type[] genericParameterTypes;

        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            // checkArgument(
            // method.getDeclaringClass().isAssignableFrom(rawType),
            // "%s is not defined by a supertype of %s",
            // method,
            // type);
            genericParameterTypes = method.getGenericParameterTypes();

        } else if (methodOrConstructor instanceof Constructor) {
            Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
            // checkArgument(
            // constructor.getDeclaringClass().isAssignableFrom(rawType),
            // "%s does not construct a supertype of %s",
            // constructor,
            // type);
            genericParameterTypes = constructor.getGenericParameterTypes();

        } else {
            throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
        }

        return resolveAll(genericParameterTypes);
    }

    /**
     * Returns the resolved generic exception types thrown by {@code constructor}.
     *
     * @param methodOrConstructor
     *            a method or constructor defined by this or any supertype.
     * @since 2.0
     */
    public List<GTypeLiteral<?>> getExceptionTypes(Member methodOrConstructor) {
        Type[] genericExceptionTypes;

        if (methodOrConstructor instanceof Method) {
            Method method = (Method) methodOrConstructor;
            // checkArgument(
            // method.getDeclaringClass().isAssignableFrom(rawType),
            // "%s is not defined by a supertype of %s",
            // method,
            // type);
            genericExceptionTypes = method.getGenericExceptionTypes();

        } else if (methodOrConstructor instanceof Constructor) {
            Constructor<?> constructor = (Constructor<?>) methodOrConstructor;
            // checkArgument(
            // constructor.getDeclaringClass().isAssignableFrom(rawType),
            // "%s does not construct a supertype of %s",
            // constructor,
            // type);
            genericExceptionTypes = constructor.getGenericExceptionTypes();

        } else {
            throw new IllegalArgumentException("Not a method or a constructor: " + methodOrConstructor);
        }

        return resolveAll(genericExceptionTypes);
    }

    /**
     * Returns the resolved generic return type of {@code method}.
     *
     * @param method
     *            a method defined by this or any supertype.
     * @since 2.0
     */
    public GTypeLiteral<?> getReturnType(Method method) {
        // checkArgument(
        // method.getDeclaringClass().isAssignableFrom(rawType),
        // "%s is not defined by a supertype of %s",
        // method,
        // type);
        return resolve(method.getGenericReturnType());
    }
}
