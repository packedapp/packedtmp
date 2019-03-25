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
 * See the License from the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.util.ConstructorDescriptor;
import app.packed.util.IllegalAccessRuntimeException;
import app.packed.util.Key;
import app.packed.util.TypeLiteral;
import packed.internal.inject.InjectSupport;
import packed.internal.invokers.InstanceInvoker;
import packed.internal.invokers.InternalFunction;
import packed.internal.invokers.InternalMapperFunction;
import packed.internal.util.TypeVariableExtractorUtil;
import packed.internal.util.UtilSupport;

/**
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class currently does not publically expose any methods that actually creates new instances. This is all hidden
 * in internal classes. This might change in the future, but for now factories are created by the user and only the
 * internals of this framework can use them to create new object instances.
 */
// TODO Qualifiers on Methods, Types together with findInjectable????
public class Factory<T> {

    /** A cache of factories used by {@link #findInjectable(Class)}. */
    private static final ClassValue<Factory<?>> FIND_FROM_CLASS_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            return new Factory(FindInjectableExecutable.find(implementation));
        }
    };

    /**
     * A cache of factories used by {@link #findInjectable(TypeLiteral)}. This cache is only used by subclasses of
     * TypeLiteral, never literals that are manually constructed.
     */
    private static final ClassValue<Factory<?>> FIND_FROM_TYPE_LITERAL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            Type t = TypeVariableExtractorUtil.findTypeParameterFromSuperClass((Class) implementation, TypeLiteral.class, 0);
            return new Factory(FindInjectableExecutable.find(UtilSupport.invoke().toTypeLiteral(t)));
        }
    };

    static {
        InjectSupport.Helper.init(new InjectSupport.Helper() {

            @Override
            protected <T> InternalFunction<T> toInternalFunction(Factory<T> factory) {
                return factory.factory.function;
            }
        });
    }

    /** The internal factory that all calls are delegated to. */
    final InternalFactory<T> factory;

    /**
     * Used by {@link Factory2#Factory2(BiFunction)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function
     */
    @SuppressWarnings("unchecked")
    Factory(BiFunction<?, ?, ? extends T> function) {
        this.factory = (InternalFactory<T>) Factory2.create(function, getClass());
    }

    /**
     * Used by {@link Factory1#Factory1(Function)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function
     */
    @SuppressWarnings("unchecked")
    Factory(Function<?, ? extends T> function) {
        this.factory = (InternalFactory<T>) Factory1.create(function, getClass());
    }

    /**
     * Creates a new factory by wrapping an internal factory.
     *
     * @param factory
     *            the internal factory to wrap.
     */
    Factory(InternalFactory<T> factory) {
        this.factory = requireNonNull(factory, "factory is null");
    }

    /**
     * Used by {@link Factory0#Factory0(Supplier)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param supplier
     *            the supplier
     */
    @SuppressWarnings("unchecked")
    Factory(Supplier<? extends T> supplier) {
        this.factory = (InternalFactory<T>) Factory0.create(supplier, getClass());
    }

    /**
     * Returns the default key under which this factory will be registered, if no other key is specified.
     *
     * @return the default key under which this factory will be registered
     */
    public final Key<T> defaultKey() {
        return factory.key;
    }

    /**
     * Returns a list of all of the dependencies of this factory. Returns an empty list if this factory does not have any
     * dependencies.
     *
     * @return a list of all of the dependencies of this factory
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public final List<Dependency> dependencies() {
        return (List) factory.dependencies;
    }

    /**
     * Returns the raw type of objects this factory creates.
     *
     * @return the raw type of objects this factory creates
     */
    public final Class<? super T> rawType() {
        return typeLiteral().getRawType();
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for scanning for
     * annotations. This might differ from the
     *
     * @return
     */
    Class<? super T> scannableType() {
        return rawType();
    }

    /**
     * Returns the type of objects this factory creates.
     *
     * @return the type of objects this factory creates
     */
    public final TypeLiteral<T> typeLiteral() {
        return factory.function.getReturnType();
    }

    /**
     * Returns a new factory retaining all the properties of this factory. Except that the default key this factory will be
     * bound to will be the specified key.
     * 
     * @param key
     *            the default key under which to bind the factory
     * @return the new factory
     * @throws ClassCastException
     *             if the type of the key does not match this factory
     */
    public final Factory<T> withDefaultKey(Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    /**
     * If this factory was created from a member (field, constructor or method), this method returns a new factory that uses
     * the specified lookup object to access the underlying member whenever the factory needs to create a new object.
     * <p>
     * This method is useful, for example, to make a factory publically available for an class that does not have a public
     * constructor.
     * <p>
     * The specified lookup object will always be preferred, even when, for example, being registered with a bundle who has
     * its own lookup object.
     *
     * @param lookup
     *            the lookup object
     * @return a new factory with uses the specified lookup object when accessing the underlying member
     * @throws IllegalAccessRuntimeException
     *             if the specified lookup object does not give access to the underlying member
     * @throws UnsupportedOperationException
     *             if this factory was not created from either a field, constructor or method.
     */
    public final Factory<T> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        return new Factory<>(new InternalFactory<T>(factory.function.withLookup(lookup), factory.dependencies));
    }

    /**
     * Returns a new factory that maps the result of this factory using the specified mapper.
     * 
     * @param mapper
     *            the mapper used to map the result
     * @param type
     *            the type of the mapped value
     * @return a new mapped factory
     */
    public final <R> Factory<R> mapTo(Function<? super T, ? extends R> mapper, Class<R> type) {
        return mapTo(mapper, TypeLiteral.of(type));
    }

    public final <R> Factory<R> mapTo(Function<? super T, ? extends R> mapper, TypeLiteral<R> type) {
        InternalMapperFunction<T, R> f = new InternalMapperFunction<>(type, factory.function, mapper);
        return new Factory<>(new InternalFactory<>(f, factory.dependencies));
    }

    public Factory<T> withType(Class<? extends T> type) {
        // Ideen er lidt tænkt at man kan specifiere det på static factory methods, der ikke giver den.
        // fulde info om implementation
        // @Inject
        // SomeService create();
        // istedet for
        // @Inject
        // SomeServiceImpl create();

        // H

        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new factory from the specified class using the following rules:
     *
     * //A single static method annotated with @Inject return the same type as the specified class //Look for a single
     * constructor on the class, return it //If multiple constructor, look for one annotated with Inject (if more than 1
     * annotated with Inject to fail) //if one constructor has more parameters than any other constructor return that. //
     * Else fail with Illegal Argument Exception
     * 
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> findInjectable(Class<T> implementation) {
        // Rename to find()
        requireNonNull(implementation, "implementation is null");
        return (Factory<T>) FIND_FROM_CLASS_CACHE.get(implementation);
    }

    /**
     * This method is equivalent to {@link #findInjectable(Class)} except that it takes a type literal.
     *
     * @param <T>
     *            the implementation type
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> findInjectable(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        if (!UtilSupport.invoke().isCanonicalized(implementation)) {
            // We cache factories for all "new TypeLiteral<>(){}"
            return (Factory<T>) FIND_FROM_TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.getType();
        if (t instanceof Class) {
            return (Factory<T>) FIND_FROM_CLASS_CACHE.get((Class<?>) t);
        } else {
            return new Factory<>(FindInjectableExecutable.find(implementation));
        }
    }

    /**
     * Returns a factory that returns the specified instance every time a new instance is requested.
     *
     * @param <T>
     *            the type of instances created by the factory
     * @param instance
     *            the instance to return every time a new instance is requested
     * @return the factory
     */
    public static <T> Factory<T> ofInstance(T instance) {
        return new Factory<>(new InternalFactory<>(InstanceInvoker.of(instance), List.of()));
    }
}
//
// /**
// * Returns a new bindable factory.
// *
// * @return a new bindable factory
// */
// public BindableFactory<T> bindable() {
// // bindable, newBindable, toBindable()
// return new BindableFactory<>(factory);
// }

// Finde metoder paa statisk vs instance....
// Hvis vi kun tillader @Inject paa de statiske, bliver algoritmerne lidt for forskellige..
class XFac2 {
    // either one annotated with inject, or just one method, for example lambda..
    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType) {
        throw new UnsupportedOperationException();
    }

    static <T> Factory<T> findInstanceMethod(Object onInstance, Class<T> returnType, String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method will attempt to find exactly one static method annotated with inject. Either because there is only one
     * method that returns returnType or because there is one annotated with {@link Inject}
     *
     * @param classContainingMethods
     * @param returnType
     *            the
     * @return a factory wrapping the st
     * @throws IllegalArgumentException
     *             if there was not exactly one static method annotated with {@link Inject} or if the return type of the
     *             method did not match the specified return type
     */
    public static <T> Factory<T> findMethodStatic(Class<?> implementation, Class<T> returnType) {
        return findMethodStatic(implementation, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> findMethodStatic(Class<?> implementation, TypeLiteral<T> returnType) {
        // I think scan from exactly one 1 inject method
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> findMethodStatic(Class<T> type) {
        return findMethodStatic(requireNonNull(type), TypeLiteral.of(type));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances.
     *
     * @param constructor
     *            the constructor used for creating new instances
     * @return the new factory
     */
    public static <T> Factory<T> ofConstructor(Constructor<? extends T> constructor) {
        throw new UnsupportedOperationException();
        // MirrorOfClass<T> mirror = (MirrorOfClass<T>) MirrorOfClass.fromImplementation(requireNonNull(constructor,
        // "constructor is null").getDeclaringClass());
        // return InternalExecutableFactory.from(mirror, mirror.constructors().match(constructor.getParameterTypes()));
    }

    /**
     * Creates a new factory that uses the specified constructor to create new instances. Compared to the simpler
     * {@link #ofConstructor(Constructor)} method this method takes a type literal that can be used to create factories with
     * a generic signature:
     *
     * <pre>
     * Factory<List<String>> f = Factory.fromConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {});
     * </pre>
     *
     * @param constructor
     *            the constructor used from creating an instance
     * @param type
     *            a type literal
     * @return the new factory
     * @see #ofConstructor(Constructor)
     */
    public static <T> Factory<T> ofConstructor(Constructor<? extends T> constructor, TypeLiteral<? extends T> type) {
        return ofConstructor(constructor);
    }

    public static <T> Factory<T> ofConstructor(Constructor<?> constructor, Class<T> type) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> ofConstructor(ConstructorDescriptor<T> constructor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a factory by looking at the specified type for a constructor with the specified parameter types. For example,
     * the following example will return a factory that uses {@link String#String(StringBuilder)} to create new instances:
     *
     * <pre>
     * Factory<String> factory = fromConstructor(String.class, StringBuilder.class);
     * </pre>
     *
     * @param constructor
     *            the constructor
     * @param key
     *            the keft to register under
     * @return the new factory
     * @throws IllegalArgumentException
     *             if a constructor with the specified parameter types does not exist on the specified type
     */
    public static <T> Factory<T> ofConstructor(ConstructorDescriptor<T> constructor, Key<T> key) {
        throw new UnsupportedOperationException();
    }

    // How we skal have
    public static <T> Factory<T> ofMethodInstance(Object onInstance, Method method, Class<T> returnType) {
        requireNonNull(returnType, "returnType is null");
        return ofMethodInstance(onInstance, method, TypeLiteral.of(returnType));
    }

    public static <T> Factory<T> ofMethodInstance(Object onInstance, Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }

    // How we skal have
    public static <T> Factory<T> ofMethodInstance(Object onInstance, TypeLiteral<T> returnType, String name, Class<?>... parameterTypes) {
        throw new UnsupportedOperationException();
    }

    public static <T> Factory<T> ofMethodStatic(Class<?> implementation, Class<T> returnType, String name, Class<?>... parameters) {
        return ofMethodStatic(implementation, TypeLiteral.of(requireNonNull(returnType, "returnType is null")), name, parameters);
    }

    // How we skal have
    public static <T> Factory<T> ofMethodStatic(Class<?> implementation, TypeLiteral<T> returnType, String name, Class<?>... parameters) {
        throw new UnsupportedOperationException();
    }

    // new Factory<String>(SomeMethod);
    // How we skal have
    public static <T> Factory<T> ofMethodStatic(Method method, Class<T> returnType) {
        return ofMethodStatic(method, TypeLiteral.of(requireNonNull(returnType, "returnType is null")));
    }

    public static <T> Factory<T> ofMethodStatic(Method method, TypeLiteral<T> returnType) {
        requireNonNull(method, "method is null");
        requireNonNull(returnType, "returnType is null");
        // ClassMirror mirror = ClassMirror.fromImplementation(method.getDeclaringClass());
        // return new Factory<T>(new InternalFactory.fromExecutable<T>((Key<T>) mirror.getKey().ofType(returnType), mirror,
        // Map.of(), new MethodMirror(method)));
        throw new UnsupportedOperationException();
    }
}
