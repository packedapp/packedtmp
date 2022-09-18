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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.base.Key;
import app.packed.base.Qualifier;
import app.packed.bean.BeanExtension;
import app.packed.bean.Provider;
import app.packed.bean.BeanExtensionPoint.BindingHook;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.BuildAction;
import app.packed.operation.op.Op;
import app.packed.container.Assembly;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import internal.app.packed.application.ApplicationInitializationContext;
import internal.app.packed.inject.service.PackedServiceLocator;
import internal.app.packed.util.LookupUtil;

/**
 * An injector is an immutable holder of services that can be dependency injected or looked up by their type at runtime.
 * An injector is typically created by populating an injector builder with the various services that needs to be
 * available.
 *
 * These
 *
 * _ xxxxx Injector controls the services that are available from every container at runtime and is typically used for
 * and for injection.
 *
 * Typically a number of injectors exist. The container injector is.
 *
 *
 *
 * Container injector can be.
 *
 * For example, the injector for a component will also include a Component service. Because an instance of the component
 * interface can always be injected into any method.
 *
 *
 * <p>
 * An Injector instance is usually acquired in one of the following three ways:
 * <h3>Directly from a Container instance</h3> By calling container.getService(ServiceManager.class)}:
 *
 * <pre>
 * Container c = ...;
 * Injector injector = c.getService(Injector.class);
 * System.out.println(&quot;Available services: &quot; + Injector.services());
 * </pre>
 *
 * <h3>Annotated method, such as OnStart or OnStop</h3> When using annotations such as OnStart or OnStop. An injected
 * component manager can be used to determine which parameters are available for injection into the annotated method.
 *
 * <pre>
 * &#064;RunOnStart()
 * public void onStart(ServiceManager ServiceManager) {
 *     System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 * }
 * </pre>
 *
 * <h3>Injecting it into a Constructor</h3> Or, by declaring it as a parameter in the constructor of a service or agent
 * registered using container builder or container builder
 *
 * <pre>
 * public class MyService {
 *     public MyService(ServiceManager ServiceManager) {
 *         System.out.println(&quot;The following services can be injected: &quot; + ServiceManager.getAvailableServices());
 *     }
 * }
 * </pre>
 *
 * <p>
 * The map returned by this method may vary doing the life cycle of a container. For example, if this method is invoked
 * in the constructor of a service registered with container builder. An instance of container builder is present in the
 * map returned. However, after the container has been initialized, the container will no longer keep a reference to the
 * configuration instance. So instances of Injector will never be available from any service manager after the container
 * has fully started.
 * <p>
 * Injectors are always immutable, however, extensions of this interface might provide mutable operations for methods
 * unrelated to injection.
 */

/**
 * 
 * <p>
 * Unless otherwise specified the set of services provided by a service locator is always unchangeable.
 */
@BindingHook(extension = BeanExtension.class)
public interface ServiceLocator {

    /**
     * Returns {@code true} if this service locator provides a service with the specified key.
     *
     * @param key
     *            key whose presence in this service locator is to be tested
     * @return {@code true} if a service with the specified key is provided by this service locator. Otherwise {@code false}
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /**
     * Returns {@code true} if this service locator provides a service with the specified key.
     *
     * @param key
     *            key whose presence in this service locator is to be tested
     * @return {@code true} if a service with the specified key is provided by this service locator. Otherwise {@code false}
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return keys().contains(key);
    }

    /**
     * Returns a service instance with the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service is provided for the specified key, {@link #use(Class)} usually gives more
     * fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing an instance of the service if present, or an empty optional if not present
     * @see #use(Class)
     */
    default <T> Optional<T> findInstance(Class<T> key) {
        return findInstance(Key.of(key));
    }

    /**
     * Returns a service instance with the given key if available, otherwise an empty optional.
     * <p>
     * If you know for certain that a service is provided for the specified key, {@link #use(Key)} usually gives more
     * fluent code.
     *
     * @param <T>
     *            the type of service that this method returns
     * @param key
     *            the key of the service to find
     * @return an optional containing an instance of the service if present, or an empty optional if not present
     * @see #use(Key)
     */
    default <T> Optional<T> findInstance(Key<T> key) {
        return findProvider(key).map(p -> p.provide());
    }

    default <T> Optional<Provider<T>> findProvider(Class<T> key) {
        return findProvider(Key.of(key));
    }

    <T> Optional<Provider<T>> findProvider(Key<T> key);

    /**
     * If a service with the specified key is present, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is present
     */
    default <T> void ifPresent(Class<T> key, Consumer<? super T> action) {
        ifPresent(Key.of(key), action);
    }

    /**
     * If a service with the specified key is provided, performs the given action with a service instance, otherwise does
     * nothing.
     *
     * @param key
     *            the key to test
     * @param action
     *            the action to be performed, if a service with the specified key is provided
     */
    default <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(action, "action is null");
        Optional<T> o = findInstance(key);
        if (o.isPresent()) {
            T instance = o.get();
            action.accept(instance);
        }
    }

    /** {@return true if this locator provides any services, otherwise false} */
    default boolean isEmpty() {
        return keys().isEmpty();
    }

    /**
     * Returns a set view containing the keys of every provided service.
     * <p>
     * If this locator supports removals, the returned set will also support removal operations: {@link Set#clear()},
     * {@link Set#remove(Object)}, {@link Set#removeAll(java.util.Collection)},
     * {@link Set#removeIf(java.util.function.Predicate)} and {@link Set#retainAll(java.util.Collection)}. or via any set
     * iterators. The returned map will never support insertion or update operations.
     * <p>
     * The returned set will retain any thread-safety guarantees provided by the locator itself.
     * 
     * @return a set view containing the keys of every provided service.
     */
    Set<Key<?>> keys();

    /** {@return the number of services provided by this locator} */
    default int size() {
        return keys().size();
    }

    /**
     * Returns a service with the specified key. Or throws a {@link NoSuchElementException} if no such service is available.
     * <p>
     * The semantics of this method are identical to {@link #findInstance(Class)} except that an exception is thrown instead
     * of returning if the service does not exist.
     *
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     * @see #find(Class)
     */
    default <T> T use(Class<T> key) {
        return use(Key.of(key));
    }

    /**
     * Returns a service with the specified type, or throws a {@link NoSuchElementException} if no such service exists. This
     * method is typically used to create fluent APIs such as:
     *
     * <pre>{@code
     * Key<WebServer> key = Key.of(WebServer.class);
     * locator.use(key).printAllLiveConnections();}
     * </pre>
     *
     * The default implementation of this method does:
     *
     * <pre>{@code
     *  Optional<T> t = find(key);
     *  if (!t.isPresent()) {
     *      throw new NoSuchElementException();
     *  }
     *  return t.get();}
     * </pre>
     *
     * @param <T>
     *            the type of service instance this method returns
     * @param key
     *            the key of the service instance to return
     * @return a service instance for the specified key
     * @throws NoSuchElementException
     *             if no service with the specified key exist
     */
    default <T> T use(Key<T> key) {
        Optional<T> t = findInstance(key);
        if (!t.isPresent()) {
            throw new NoSuchElementException("A service with the specified key does not exist, key = " + key);
        }
        return t.get();
    }

    /**
     * Returns an application driver that can be used to create standalone service locator instances.
     * 
     * @return an application driver
     * @see #build(Assembly, Wirelet...)
     * @see #of(Consumer)
     * @see #of(Assembly, Wirelet...)
     */
    private static ApplicationDriver<ServiceLocator> driver() {
        throw new UnsupportedOperationException();
    }

    // maaske har vi launcher og Image...

    static <T> T lookup(Class<T> type, Assembly assembly, Wirelet... wirelets) {
        // Hvad med empty
        throw new UnsupportedOperationException();
    }

    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return driver().mirrorOf(assembly, wirelets);
    }

    /**
     * Creates a new service locator image from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     * @see #driver()
     */
    static ApplicationLauncher<ServiceLocator> newLauncher(Assembly assembly, Wirelet... wirelets) {
        return driver().imageOf(assembly, wirelets);
    }

    static ApplicationLauncher<ServiceLocator> newReusableLauncher(Assembly assembly, Wirelet... wirelets) {
        return driver().newReusableImage(assembly, wirelets);
    }

    /** {@return a service locator that provides no services.} */
    static ServiceLocator of() {
        return new PackedServiceLocator(Map.of());
    }

    /**
     * Creates a new service locator from the specified assembly and optional wirelets.
     * 
     * @param assembly
     *            the assembly that should be used to create the service locator
     * @param wirelets
     *            optional wirelets
     * @return a new service locator
     */
    static ServiceLocator of(Assembly assembly, Wirelet... wirelets) {
        return driver().launch(assembly, wirelets);
    }

    static ServiceLocator of(BuildAction<? super Composer> configurator, Wirelet... wirelets) {
        return Composer.of(configurator, wirelets);
    }

    /**
     * A lightweight configuration object that can be used to create injectors via. This is thought of a alternative to
     * using a {@link BaseAssembly}. Unlike assemblies all services are automatically exported once defined. For example
     * useful in tests.
     * 
     * <p>
     * The main difference compared assemblies is that there is no concept of encapsulation. All services are exported by
     * default.
     */
    public static final class Composer extends AbstractComposer {

        private static final MethodHandle CONV = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ApplicationInitializationContext.class,
                "serviceLocator", ServiceLocator.class);

        private static final ApplicationDriver<ServiceLocator> DRIVER = ApplicationDriver.builder().build(ServiceLocator.class, CONV);

        private boolean initialized;

        /** For internal use only. */
        Composer() {}

        /**
         * Returns an instance of the injector extension.
         * 
         * @return an instance of the injector extension
         */
        private ServiceExtension extension() {
            ServiceExtension se = container().use(ServiceExtension.class);
            if (!initialized) {
                se.exportAll();
                initialized = true;
            }
            return se;
        }

        /**
         * @param assembly
         *            the assembly to bind
         * @param wirelets
         *            optional import/export wirelets
         */
        public ContainerMirror link(Assembly assembly, Wirelet... wirelets) {
            return container().link(assembly, wirelets);
        }

        /**
         * Provides the specified implementation as a new singleton service. An instance of the implementation will be created
         * together with the injector. The runtime will use {@link Op#factoryOf(Class)} to find the constructor or method
         * used for instantiation.
         * <p>
         * The default key for the service will be the specified {@code implementation}. If the
         * {@code implementation.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will
         * include the qualifier. For example, given this implementation: <pre>
         * &#64;SomeQualifier
         * public class SomeService {}
         * </pre>
         * <p>
         * The following two example are equivalent
         * </p>
         * <pre> 
         * Injector i = Injector.of(c -&gt; { 
         *    c.provide(SomeService.class); 
         * });
         * </pre> <pre> 
         * Injector i = Injector.of(c -&gt; { 
         *   c.provide(SomeService.class).as(new Key&lt;&#64;SomeQualifier SomeService&gt;() {});
         * });
         * </pre>
         * 
         * @param <T>
         *            the type of service to provide
         * @param implementation
         *            the implementation to provide a singleton instance of
         * @return a service configuration for the service
         */
        public <T> ProvideableBeanConfiguration<T> provide(Class<T> implementation) {
            extension();
            return container().use(BeanExtension.class).install(implementation).provide();
        }

        /**
         * Binds the specified factory to a new service. When the injector is created the factory will be invoked <b>once</b> to
         * instantiate the service instance.
         * <p>
         * The default key for the service is determined by {@link Op#key()}.
         * 
         * @param <T>
         *            the type of service to bind
         * @param factory
         *            the factory to bind
         * @return a service configuration for the service
         */
        public <T> ProvideableBeanConfiguration<T> provide(Op<T> factory) {
            extension();
            return container().use(BeanExtension.class).install(factory).provide();
        }

        /**
         * Binds all services from the specified injector.
         * <p>
         * A simple example, importing a singleton {@code String} service from one injector into another:
         * 
         * <pre> {@code
         * Injector importFrom = Injector.of(c -&gt; c.bind("foostring"));
         * 
         * Injector importTo = Injector.of(c -&gt; {
         *   c.bind(12345); 
         *   c.provideAll(importFrom);
         * });
         * 
         * System.out.println(importTo.with(String.class));// prints "foostring"}}
         * </pre>
         * <p>
         * It is possible to specify one or import stages that can restrict or transform the imported services.
         * <p>
         * For example, the following example takes the injector we created in the previous example, and creates a new injector
         * that only imports the {@code String.class} service.
         * 
         * <pre>
         * Injector i = Injector.of(c -&gt; {
         *   c.injectorBind(importTo, InjectorImportStage.accept(String.class));
         * });
         * </pre> Another way of writing this would be to explicitly reject the {@code Integer.class} service. <pre>
         * Injector i = Injector.of(c -&gt; {
         *   c.provideAll(importTo, InjectorImportStage.reject(Integer.class));
         * });
         * </pre> @param injector the injector to bind services from
         * 
         * @param injector
         *            the injector to import services from
         */
        // maybe bindAll()... Syntes man burde hedde det samme som Bindable()
        // Er ikke sikker paa vi skal have wirelets her....
        // Hvis det er noedvendigt saa maa man lave en ny injector taenker jeg....
        public void provideAll(ServiceLocator injector) {
            extension();
            container().use(ServiceExtension.class).provideAll(injector);
        }

        /**
         * Binds the specified instance as a new service.
         * <p>
         * The default key for the service will be {@code instance.getClass()}. If the type returned by
         * {@code instance.getClass()} is annotated with a {@link Qualifier qualifier annotation}, the default key will have the
         * qualifier annotation added.
         *
         * @param <T>
         *            the type of service to bind
         * @param instance
         *            the instance to bind
         * @return a service configuration for the service
         */
        // Rename to instant
        // All annotations will be processed like provide() except that constructors will not be processed
        // Ohh we need to analyze them differently, because we should ignore all constructors.
        // Should not fail if we fx have two public constructors of equal lenght
        public <T> ProvideableBeanConfiguration<T> provideInstance(T instance) {
            extension();
            return container().use(BeanExtension.class).installInstance(instance).provide();
        }

        public <T> ProvideableBeanConfiguration<T> providePrototype(Class<T> implementation) {
            extension();
            return container().use(ServiceExtension.class).providePrototype(implementation);
        }

        public <T> ProvideableBeanConfiguration<T> providePrototype(Op<T> factory) {
            extension();
            return container().use(ServiceExtension.class).providePrototype(factory);
        }

        private static ServiceLocator of(BuildAction<? super Composer> configurator, Wirelet... wirelets) {
            return compose(DRIVER, new Composer(), configurator, wirelets);
        }
    }
}
