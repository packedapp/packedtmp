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
import static packed.internal.util.StringFormatter.format;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.lifecycle.OnStart;
import app.packed.reflect.ConstructorDescriptor;
import app.packed.reflect.ExecutableDescriptor;
import app.packed.reflect.MethodDescriptor;
import app.packed.reflect.UncheckedIllegalAccessException;
import app.packed.util.BaseSupport;
import app.packed.util.InvalidDeclarationException;
import app.packed.util.Key;
import app.packed.util.TypeLiteral;
import packed.internal.access.AppPackedInjectAccess;
import packed.internal.access.SharedSecrets;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.factoryhandle.ExecutableFactoryHandle;
import packed.internal.inject.factoryhandle.FactoryHandle;
import packed.internal.inject.factoryhandle.InstanceFactoryHandle;
import packed.internal.inject.factoryhandle.MappingFactoryHandle;
import packed.internal.util.TypeUtil;
import packed.internal.util.TypeVariableExtractorUtil;

/**
 * Factories are used for creating new instances of a particular type.
 * <p>
 * This class does not currently expose any methods that actually creates new instances. This is all hidden in the
 * internals of Packed. This might change in the future, but for now factories are created by the user and only the
 * internals of Packed can use them to create new object instances.
 */
// TODO Qualifiers on Methods, Types together with findInjectable????
// Yes need to pick those up!!!!
// probably rename defaultKey to key.

// Split-module class hierachies, must

// Factories only
//
// Is it the responsibility of the factory or the injector to inject fields and methods???
// + Factory
//
// + Injector
// Then we can disable it on a case to case basis
// You can actually use factories without injection
//
//
//
//
// -------------------------
// ServiceDescriptor
// Refereres fra InjectorDescriptor....
// Skal bruges til Filtrering... Men hvis noeglerne er skjult kan vi vel bruge service....
public class Factory<T> {

    /** A cache of factories used by {@link #findInjectable(Class)}. */
    private static final ClassValue<Factory<?>> FIND_INJECTABLE_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            return new Factory(FactoryFindInjectableExecutable.find(implementation));
        }
    };

    /**
     * A cache of factories used by {@link #findInjectable(TypeLiteral)}. This cache is only used by subclasses of
     * TypeLiteral, never literals that are manually constructed.
     */
    private static final ClassValue<Factory<?>> FIND_INJECTABLE_FROM_TYPE_LITERAL_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        protected Factory<?> computeValue(Class<?> implementation) {
            Type t = TypeVariableExtractorUtil.findTypeParameterFromSuperClass((Class) implementation, TypeLiteral.class, 0);
            return new Factory(FactoryFindInjectableExecutable.find(SharedSecrets.util().toTypeLiteral(t)));
        }
    };

    static {
        SharedSecrets.zet(new AppPackedInjectAccess() {

            @Override
            public <T> FactoryHandle<T> toInternalFunction(Factory<T> factory) {
                return factory.factory.function;
            }

            @Override
            public InjectorBuilder getBuilder(InjectionExtension ie) {
                return ie.builder;
            }
        });
    }

    /** The internal factory that all calls delegate to. */
    final FactorySupport<T> factory;

    public final FactoryHandle<T> handle() {
        return factory.function;
    }

    /**
     * Used by {@link Factory2#Factory2(BiFunction)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(BiFunction<?, ?, ? extends T> function) {
        this.factory = (FactorySupport<T>) Factory2.create(getClass(), function);
    }

    /**
     * Creates a new factory by wrapping an internal factory.
     *
     * @param factory
     *            the internal factory to wrap.
     */
    Factory(FactorySupport<T> factory) {
        this.factory = requireNonNull(factory, "factory is null");
    }

    /**
     * Used by {@link Factory1#Factory1(Function)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param function
     *            the function used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(Function<?, ? extends T> function) {
        this.factory = (FactorySupport<T>) Factory1.create(getClass(), function);
    }

    /**
     * Used by {@link Factory0#Factory0(Supplier)} because we cannot call {@link Object#getClass()} before calling a
     * constructor in this class (super).
     *
     * @param supplier
     *            the supplier used to create new instances
     */
    @SuppressWarnings("unchecked")
    Factory(Supplier<? extends T> supplier) {
        this.factory = (FactorySupport<T>) Factory0.create(getClass(), supplier);
    }

    /**
     * The key under which If this factory is registered as a service with an {@link Injector}. This method returns the
     * (default) key that will be used, for example, when regist Returns the (default) key to which this factory will bound
     * to if using as If this factory is used to register a service, for example, via
     * {@link InjectorConfigurator#provide(Factory)}. This method returns the key for which the factory
     * 
     * Returns the key for which this factory will be registered, this can be overridden, for example, by calling
     * {@link ComponentServiceConfiguration#as(Key)}.
     *
     * @return the key under which this factory will be registered unless
     * @see #withKey(Key)
     */
    public final Key<T> key() {
        return factory.defaultKey;
    }

    /**
     * Returns a list of all of the dependencies that needs to be fulfilled in order for this factory to successfully create
     * an instance. Returns an empty list if this factory does not have any dependencies.
     * <p>
     * 
     * @apiNote The list does not include dependencies that may be needed to do field or instance method injection. As these
     *          are the responsibility of the injector in which they are registered.
     * 
     * @return a list of all of the dependencies of this factory
     */
    public final List<ServiceDependency> dependencies() {
        return factory.dependencies;
    }

    /**
     * Returns a new factory that maps every object this factory create using the specified mapper.
     * 
     * @param <R>
     *            the type of result to map to
     * @param mapper
     *            the mapper used to map the result
     * @param type
     *            the type of the mapped value
     * @return a new mapped factory
     */
    public final <R> Factory<R> mapTo(Function<? super T, R> mapper, Class<R> type) {
        return mapTo(mapper, TypeLiteral.of(type));
    }

    /**
     * Returns a new factory that maps every object this factory create using the specified mapper.
     * 
     * @param <R>
     *            the type of result to map to
     * @param mapper
     *            the mapper used to map the result
     * @param type
     *            the type of the mapped value
     * @return a new mapped factory
     */
    public final <R> Factory<R> mapTo(Function<? super T, R> mapper, TypeLiteral<R> type) {
        MappingFactoryHandle<T, R> f = new MappingFactoryHandle<>(type, factory.function, mapper);
        return new Factory<>(new FactorySupport<>(f, factory.dependencies));
    }

    /**
     * Returns the raw type of those objects that this factory creates.
     *
     * @return the raw type of those objects that this factory creates
     */
    public final Class<? super T> rawType() {
        return typeLiteral().rawType();
    }

    /**
     * Returns the injectable type of this factory. This is the type that will be used for scanning for scanning for
     * annotations. This might differ from the.
     *
     * @return stuff
     */
    // We should make this public...
    // InjectableType
    Class<? super T> scannableType() {
        return rawType();
    }

    /**
     * Returns the type of those objects that this factory creates.
     *
     * @return the type of those objects that this factory creates
     */
    public final TypeLiteral<T> typeLiteral() {
        return factory.function.returnType();
    }

    /**
     * Returns a new bindable factory.
     * 
     * @return a new bindable factory
     */
    // What if already bindable?
    // Create new or return same????
    public final BindableFactory<T> bindable() {
        return new BindableFactory<>(this);
    }

    /**
     * Returns a new factory retaining all of the existing properties of this factory. Except that the key returned by
     * {@link #key()} will be changed to the specified key.
     * 
     * @param key
     *            the key under which to bind the factory
     * @return the new factory
     * @throws ClassCastException
     *             if the type of the key does not match the type of instances this factory provides
     * @see #key()
     */
    public final Factory<T> withKey(Key<? super T> key) {
        throw new UnsupportedOperationException();
    }

    /**
     * If this factory was created from a member (field, constructor or method), this method returns a new factory that uses
     * the specified lookup object to access any mem underlying member whenever this framework needs to access.
     * <p>
     * This method is useful, for example, to make a factory publically available for an class that does not have a public
     * constructor.
     * <p>
     * The specified lookup object will always be preferred, even when, for example, being registered with a bundle who has
     * its own lookup object.
     * <p>
     * If you have split-module class hierachies with am abstract class in one module a concrete class in another module.
     * You can use a {@link BaseSupport} class to register a method handle with the abstract class.
     * 
     * Remember to register the support class via the standard service loading mechanism as outlined in ....
     * 
     * @param lookup
     *            the lookup object
     * @return a new factory with uses the specified lookup object when accessing the underlying member
     * @throws UncheckedIllegalAccessException
     *             if the specified lookup object does not give access to the underlying member
     * @throws UnsupportedOperationException
     *             if this factory was not created from either a field, constructor or method.
     */
    // Goddamn, what about static create method on one object, and the actuak object in another module.
    // Her taenker jeg ogsaa paa at det lookup object bliver brugt til Hooks, o.s.v.
    // Maaske skal vi tillade stacked MethodHandles..
    // Maaske skal vi endda have en SelectiveMethodHandle
    //// Ideen er at man kan pakke en method handle ind...
    // Stacked lookups..
    // Vi skal have en hel section omkring method handlers.
    public final Factory<T> withLookup(MethodHandles.Lookup lookup) {
        requireNonNull(lookup, "lookup is null");
        return new Factory<>(new FactorySupport<T>(factory.function.withLookup(lookup), factory.dependencies));
    }

    public Factory<T> useExactType(Class<? extends T> type) {
        // scanAs() must be exact type. Show example with static method that returns a Foo, but should scan with FooImpl

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
     * Tries to find a single injectable constructor or static method on the specified class using the following rules:
     * 
     * Finds a Creates a new factory from the specified class using the following rules:
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
        return (Factory<T>) FIND_INJECTABLE_CACHE.get(implementation);
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
        if (!SharedSecrets.util().isCanonicalized(implementation)) {
            // We cache factories for all "new TypeLiteral<>(){}"
            return (Factory<T>) FIND_INJECTABLE_FROM_TYPE_LITERAL_CACHE.get(implementation.getClass());
        }
        Type t = implementation.type();
        if (t instanceof Class) {
            return (Factory<T>) FIND_INJECTABLE_CACHE.get((Class<?>) t);
        } else {
            return new Factory<>(FactoryFindInjectableExecutable.find(implementation));
        }
    }

    /**
     * Returns a factory that returns the specified instance every time a new instance is requested.
     * <p>
     * Instances passed to this method should not use field or method injection if the factory needs to be used multiple
     * times. As these fields and members will be injected every time, possible concurrently, an instance is requested from
     * the factory.
     * 
     * @param <T>
     *            the type of instances created by the factory
     * @param instance
     *            the instance to return every time a new instance is requested
     * @return the factory
     */
    public static <T> Factory<T> ofInstance(T instance) {
        return new Factory<>(new FactorySupport<>(InstanceFactoryHandle.of(instance), List.of()));
    }

    static <T> Factory<T> fromMethodHandle(MethodHandle mh) {
        // Be aware that annotations and generic information is stripped from MethodHandles.
        throw new UnsupportedOperationException();
    }
}

/** This class is responsible for finding an injectable executable. */
final class FactoryFindInjectableExecutable {

    static <T> FactorySupport<T> find(Class<T> implementation) {
        ExecutableDescriptor executable = findExecutable(implementation);
        return new FactorySupport<>(new ExecutableFactoryHandle<>(TypeLiteral.of(implementation), executable, null),
                ServiceDependency.fromExecutable(executable));
    }

    static <T> FactorySupport<T> find(TypeLiteral<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        ExecutableDescriptor executable = findExecutable(implementation.rawType());
        return new FactorySupport<>(new ExecutableFactoryHandle<>(implementation, executable, null), ServiceDependency.fromExecutable(executable));
    }

    private static ExecutableDescriptor findExecutable(Class<?> type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an array");
        } else if (type.isAnnotation()) {
            throw new IllegalArgumentException("The specified type (" + format(type) + ") is an annotation");
        }

        // Try to find a single static method annotated with @Inject
        Method method = null;
        for (Method m : type.getDeclaredMethods()) {
            if (Modifier.isStatic(m.getModifiers()) && m.isAnnotationPresent(Inject.class)) {
                if (method != null) {
                    throw new IllegalArgumentException("There are multiple static methods annotated with @Inject on " + format(type));
                }
                method = m;
            }
        }

        // If a single method has been found, use it
        if (method != null) {
            // Det er jo i virkeligheden en Key vi laver her, burde havde det samme checkout..
            if (method.getReturnType() == void.class /* || returnType == Void.class */) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have a void return type."
                        + " (@Inject on static methods are used to indicate that the method is a factory for a specific type, not for injecting values");
            } else if (TypeUtil.isOptionalType(method.getReturnType())) {
                throw new IllegalArgumentException("Static method " + method + " annotated with @Inject cannot have an optional return type ("
                        + method.getReturnType().getSimpleName() + "). A valid instance needs to be provided by the method");
            }
            return MethodDescriptor.of(method);
        }

        Constructor<?>[] constructors = type.getDeclaredConstructors();

        // If we only have 1 constructor, return it.
        if (constructors.length == 1) {
            return ConstructorDescriptor.of(constructors[0]);
        }

        // See if we have a single constructor annotated with @Inject
        Constructor<?> constructor = null;
        int maxParameters = 0;
        for (Constructor<?> c : constructors) {
            maxParameters = Math.max(maxParameters, c.getParameterCount());
            if (c.isAnnotationPresent(Inject.class)) {
                if (constructor != null) {
                    throw new InvalidDeclarationException(
                            "Multiple constructors annotated with @" + Inject.class.getSimpleName() + " on class " + format(type));
                }
                constructor = c;
            }
        }
        if (constructor != null) {
            return ConstructorDescriptor.of(constructor);
        }

        // Try and find one constructor with maximum number of parameters.
        for (Constructor<?> c : constructors) {
            if (c.getParameterCount() == maxParameters) {
                if (constructor != null) {
                    throw new IllegalArgumentException("No constructor annotated with @" + Inject.class.getSimpleName()
                            + ". And multiple constructors having the maximum number of parameters (" + maxParameters + ") on class " + format(type));
                }
                constructor = c;
            }
        }
        return ConstructorDescriptor.of(constructor);
    }
}

/** An factory support class. */
// Inline target
final class FactorySupport<T> {

    /** The key that this factory will be registered under by default with an injector. */
    final Key<T> defaultKey;

    /** A list of all of this factory's dependencies. */
    final List<ServiceDependency> dependencies;

    /** The function used to create a new instance. */
    final FactoryHandle<T> function;

    FactorySupport(FactoryHandle<T> function, List<ServiceDependency> dependencies) {
        this.dependencies = requireNonNull(dependencies, "dependencies is null");
        this.function = requireNonNull(function);
        this.defaultKey = function.typeLiteral.toKey();
    }

    /**
     * Returns the scannable type of this factory. This is the type that will be used for scanning for annotations such as
     * {@link OnStart} and {@link Provide}.
     *
     * @return the scannable type of this factory
     */
    Class<? super T> getScannableType() {
        return function.returnTypeRaw();
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
     * @param implementation
     *            the implementation
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
     * Factory<List<String>> f = Factory.fromConstructor(ArrayList.class.getConstructor(), new TypeLiteral<List<String>>() {
     * });
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
