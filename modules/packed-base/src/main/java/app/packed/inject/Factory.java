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
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.util.ConstructorDescriptor;
import packed.inject.factory.InternalFactory;
import packed.inject.factory.InternalFactory0;
import packed.inject.factory.InternalFactory1;
import packed.inject.factory.InternalFactory2;
import packed.inject.factory.InternalFactoryExecutable;
import packed.inject.factory.InternalFactoryInstance;

/**
 * Factories are used for creating new instances of a particular type.
 *
 * <p>
 * This class currently does not publically expose any methods that actually creates new instances. This is all hidden
 * in internal classes. This might change in the future, but for now factories are created by a user and can only be
 * consumed the the internals of Packed.
 */
// Finde metoder paa statisk vs instance....
// Hvis vi kun tillader @Inject paa de statiske, bliver algoritmerne lidt for forskellige..
public class Factory<T> {

    /** A cache of supplier factory definitions. */
    static final ClassValue<Factory<?>> CACHE_FROM_CLASS = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> type) {
            return new Factory(InternalFactoryExecutable.from(type));
        }
    };

    /** The internal factory that all calls are delegated to. */
    final InternalFactory<T> factory;

    /**
     * Used by {@link Factory2#Factory2(BiFunction)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function
     */
    Factory(BiFunction<?, ?, ? extends T> function) {
        this.factory = InternalFactory2.of(function, getClass());
    }

    /**
     * Used by {@link Factory1#Factory1(Function)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function
     */
    Factory(Function<?, ? extends T> function) {
        this.factory = InternalFactory1.of(function, getClass());
    }

    /**
     * The default constructor.
     *
     * @param function
     *            the function
     */
    Factory(InternalFactory<T> factory) {
        this.factory = requireNonNull(factory);
    }

    /**
     * Used by {@link Factory0#Factory0(Supplier)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param supplier
     *            the supplier
     */
    Factory(Supplier<? extends T> supplier) {
        this.factory = InternalFactory0.of(supplier, getClass());
    }

    /**
     * Returns a list of this factory's dependencies. Returns the empty list if this factory does not have any dependencies.
     *
     * @return a list of this factory's dependencies
     */
    public final List<Dependency> getDependencies() {
        return factory.getDependencies();
    }

    /**
     * This is the key under which a factory will
     *
     * @return
     */
    public final Key<T> getKey() {
        return factory.getKey();
    }

    /**
     * Returns the raw type of objects this factory creates.
     *
     * @return the raw type of objects this factory creates
     */
    public final Class<? super T> getRawType() {
        return getTypeLiteral().getRawType();
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for scanning for
     * annotations. This might differ from the
     *
     * @return
     */
    Class<? super T> getScannableType() {
        return getRawType();
    }

    /**
     * Returns the type of objects this factory creates.
     *
     * @return the type of objects this factory creates
     */
    public final TypeLiteral<T> getTypeLiteral() {
        return factory.getType();
    }

    /**
     * Returns a new bindable factory.
     * @return a new bindable factory
     */
    // bindable
    // newBindable
    // toBindable()
    public BindableFactory<T> bindable() {
        return new BindableFactory<>(factory);
    }

    public final boolean isAccessibleWith(Lookup lookup) {
        return factory.isAccessibleWith(lookup);
    }
    
    public final void checkAccessWith(Lookup lookup) {
        
    }
    
    /**
     * Returns a new factory that caches the value produced the first time. If multiple attempts to create a value the rest
     * blocks
     *
     * @return a new factory that caches the value produced the first time
     */
    Factory<T> singleton() {
        // Naahh taenker folk bare bliver forvirret.
        throw new UnsupportedOperationException();
    }

    /**
     * If this factory has been created from a Constructor or Method. This method returns a new factory that uses the
     * specified lookup object to access the underlying constructor or method whenever the factory needs to generate a new
     * value.
     * <p>
     * Normally, the accessors method this is not needed if configuring a module. This can be useful, for example, to share
     * a factory instance that has a package private constructor.
     *
     * @param lookup
     *            the lookup object
     * @return a new factory with uses the specified lookup object when invoke the underlying method or constructor
     * @throws IllegalArgumentException
     *             if the specified lookup object does not give access to the underlying constructor or method
     * @throws UnsupportedOperationException
     *             if this factory was not created from either a constructor or method.
     */
    public Factory<T> withLookup(MethodHandles.Lookup lookup) {
        return new Factory<>(factory.withMethodLookup(lookup));
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
     * annotated with Inject->fail) //if one constructor has more parameters than any other constructor return that. // Else
     * fail with Illegal Argument Exception
     *
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> find(Class<T> implementation) {
        return (Factory<T>) CACHE_FROM_CLASS.get(implementation);
    }

    /**
     * This method is equivalent to {@link #from(Class)} except that it takes a type literal or key.
     *
     * @param implementation
     *            the implementation type
     * @return a factory for the specified implementation type
     */
    public static <T> Factory<T> find(TypeLiteralOrKey<T> implementation) {
        // Does not really work, because getType() is not valid...
        // return (Factory<T>) MirrorOfClass.fromImplementation(implementation.getRawType()).getDefaultFactory();
        throw new UnsupportedOperationException();
    }

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
     * @param implementation
     *            the type that contains the constructor
     * @param parameterTypes
     *            the parameter types of the constructor
     * @return the new factory
     * @throws IllegalArgumentException
     *             if a constructor with the specified parameter types does not exist on the specified type
     */
    public static <T> Factory<T> ofConstructor(ConstructorDescriptor<T> constructor, Key<T> key) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a factory that returns the specified instance on every invocation.
     *
     * @param <T>
     *            the type of instances the factory will create
     * @param instance
     *            the instance to return on every invocation
     * @return the factory
     * @see #ofInstance(Object, Class)
     * @see #ofInstance(Object, TypeLiteralOrKey)
     */
    public static <T> Factory<T> ofInstance(T instance) {
        return new Factory<>(InternalFactoryInstance.of(instance));
    }

    /**
     * Returns a factory that returns the specified instance on every invocation.
     *
     * @param <T>
     *            the type of instances the factory will create
     * @param instance
     *            the instance to return on every invocation
     * @param type
     *            if using this factory to bind
     * @return the factory
     * @see #ofInstance(Object)
     * @see #ofInstance(Object, TypeLiteralOrKey)
     */
    public static <T> Factory<T> ofInstance(T instance, Class<T> type) {
        return new Factory<>(InternalFactoryInstance.of(instance, type));
    }

    /**
     * Returns a factory that returns the specified instance on every invocation.
     *
     * @param <T>
     *            the type of instances the factory will create
     * @param instance
     *            the instance to return on every invocation
     * @return the factory
     * @see #ofInstance(Object, Class)
     * @see #ofInstance(Object, TypeLiteralOrKey)
     */
    public static <T> Factory<T> ofInstance(T instance, TypeLiteralOrKey<T> typeLiteralOrKey) {
        return new Factory<>(InternalFactoryInstance.of(instance, typeLiteralOrKey));
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

// If we add withDependency, which I think we should. It should be reflected in the available keys
// Dependencies added here are only for construction. Not for injecting
// withSuppliedDependency(K key, Supplier<K> dependencySupplier)
// If the class is instantiated multiple times. the specified supplier will be called each time.
// withIndexedDependency()

// public int[] getMissingIndexes() {
// // returns the missing indexes
// throw new UnsupportedOperationException();
// }
//
// /**
// * Returns the number of unresolved dependencies this factory have.
// * <p>
// * Dependencies that have been added via {@link #withBinding(int, Object)} are not counted as a missing dependency.
// For
// * example:
// *
// * <pre>
// * Factory<String> f = fromConstructor(String.class, StringBuffer.class);
// * f.getNumberOfUnresolvedDependencies(); // Will return 1
// * f.withDependency(0, new StringBuffer()).getNumberOfUnresolvedDependencies(); // Will return 0
// * </pre>
// *
// * @return the number of unresolved dependencies this factory have.
// */
// public int getNumberOfUnresolvedDependencies() {
// return factory.getNumberOfUnresolvedDependencies();
// }
//
// /**
// * If there are no unresolved indexes. This method returns an empty array.
// *
// * @return
// */
// // Why would anyone want this???. Would be nice if we could have a Listener that could add a dependency to a factory
// // Hmm, I don't know if that would be a good idea.
// // void resolveMissingDependencies(Function<Factory<?>, Factory<?>);, hmmm cannot come up with any usecases.
// // Other than we can add dependencies at runtime!!! But only instantiation
//
// // Instantiate, Component not exposed as Service, Component Exposed as Service, Service no Component, None of the
// above
// // (newInstance()). I think take both as a
// // parameter then
// public int[] getUnresolvedIndexes() {
// throw new UnsupportedOperationException();
// }

// /**
// * @param instance
// * sd
// * @return a new factory
// * @throws IllegalArgumentException
// * if no parameters or more than 1 parameter found which fit the index
// */
// public Factory<T> withBinding(Object instance) {
// // Problem if it is named identical. But function in a differen way than component.addDependency
// // Here I'm thinking about the retainment
//
// // Add some variations <T> addDependency(Class<T> key, T instance)
// // someMethod(Number i, Object value)
// // addDependency(1) will fail because 1 match both parameters
// // addDependency(Number.class, 1) <- will success because it only matches Number
//
// // someMethod(List<String> l1, List<Number> numbers);
// // addDependency(List.class, someList) <- will fail because it matches both parameters
// // addDependency(new TypeLiteral<List<String>>, someList) <- will success because it only matches one parameter.
//
// // Finally
// // someMethod(@Name("x") String s1, @Name("y") String s2)
// // addDependency(Key.of(String.class, Name("x")) will success
// // Idk if the two last are needed, when we can specify an index?
// throw new UnsupportedOperationException();
