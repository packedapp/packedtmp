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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Key.Qualifier;
import app.packed.component.Bundle;
import app.packed.component.CustomConfigurator;
import app.packed.component.Wirelet;
import app.packed.container.BaseBundle;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Factory;

/**
 * A lightweight configuration object that can be used to create {@link Injector injectors} via
 * {@link Injector#configure(CustomConfigurator, Wirelet...)}. This is thought of a alternative to using a
 * {@link BaseBundle}. Unlike bundles all services are automatically exported once defined. For example useful in tests.
 * 
 * <p>
 * The main difference compared to bundles is that there is no concept of encapsulation. All services are exported by
 * default.
 */
public final class InjectorAssembler {

    /** The configuration we delegate all calls to. */
    private final ContainerConfiguration configuration;

    private boolean initialized;

    /**
     * Creates a new configurator
     * 
     * @param configuration
     *            the configuration to wrap
     */
    InjectorAssembler(ContainerConfiguration configuration) {
        this.configuration = requireNonNull(configuration, "configuration is null");
    }

    /**
     * Returns the container configuration that was used to create this configurator.
     * 
     * @return the container configuration that was used to create this configurator
     */
    private ContainerConfiguration configuration() {
        return configuration;
    }

    /**
     * Returns an instance of the injector extension.
     * 
     * @return an instance of the injector extension
     */
    private ServiceExtension extension() {
        ServiceExtension se = configuration().use(ServiceExtension.class);
        if (!initialized) {
            se.exportAll();
            initialized = true;
        }
        return se;
    }

    /**
     * @param bundle
     *            the bundle to bind
     * @param wirelets
     *            optional import/export wirelets
     */
    public void link(Bundle<?> bundle, Wirelet... wirelets) {
        configuration().link(bundle, wirelets);
    }

    /**
     * Sets a {@link Lookup lookup object} that will be used to access members (fields, constructors and methods) on
     * registered objects. The lookup object will be used for all service bindings and component installations that happens
     * after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * Lookup objects that have been explicitly set using {@link Factory#withLookup(java.lang.invoke.MethodHandles.Lookup)}
     * are never overridden by any lookup object set by this method.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup object
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object
     */
    public void lookup(Lookup lookup) {
        configuration().lookup(lookup);
    }

    /**
     * Provides the specified implementation as a new singleton service. An instance of the implementation will be created
     * together with the injector. The runtime will use {@link Factory#find(Class)} to find the constructor or method used
     * for instantiation.
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
     * @see ServiceExtension#provide(Class)
     */
    public <T> ServiceComponentConfiguration<T> provide(Class<T> implementation) {
        return extension().provide(implementation);
    }

    /**
     * Binds the specified factory to a new service. When the injector is created the factory will be invoked <b>once</b> to
     * instantiate the service instance.
     * <p>
     * The default key for the service is determined by {@link Factory#key()}.
     * 
     * @param <T>
     *            the type of service to bind
     * @param factory
     *            the factory to bind
     * @return a service configuration for the service
     */
    public <T> ServiceComponentConfiguration<T> provide(Factory<T> factory) {
        return extension().provide(factory);
    }

    public <T> ServiceComponentConfiguration<T> providePrototype(Class<T> factory) {
        return extension().providePrototype(factory);
    }

    public <T> ServiceComponentConfiguration<T> providePrototype(Factory<T> factory) {
        return extension().providePrototype(factory);
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
     * @param wirelets
     *            any number of stages that restricts or transforms the services that are imported
     * @throws IllegalArgumentException
     *             if the specified wirelet are not all wirelets from {@link ServiceWirelets} or combinations (via
     *             {@link Wirelet#combine(Wirelet, Wirelet) combinations} thereof
     */
    // maybe bindAll()... Syntes man burde hedde det samme som Bindable()
    // Er ikke sikker paa vi skal have wirelets her....
    // Hvis det er noedvendigt saa maa man lave en ny injector taenker jeg....
    public void provideAll(Injector injector, Wirelet... wirelets) {
        extension().provideAll(injector, wirelets);
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
    public <T> ServiceComponentConfiguration<T> provideInstance(T instance) {
        return extension().provideInstance(instance);
    }
}
// addStatics(); useStatics()
// @OnHook
// @Provides
// I think we should replace with

// provide(Stuff).asNone();
// providersOnly(Class<?>)<- dont register owning object, dont instantiate itif only static Provides methods...
// provideBy
// provideHolder
// Class + Instance + Factory???
// Sleezy method on ServiceConfiguration .lazy().asNone();
// Can also be used with hooks.... <- Well @OnHook, could be used with a service???
// .neverInstantiate(); static @OnHook...
// noInstantiation()
// provideStatics(Object instance)
// <T> ServiceConfiguration<T> provideLazy(Class<T> implementation);

// * @return a service configuration for the service
// */
// <T> ServiceConfiguration<T> provideLazy(Factory<T> factory);
//
// <T> ServiceConfiguration<T> provideLazy(TypeLiteral<T> implementation);

// <T> ServiceConfiguration<T> providePrototype(Class<T> implementation);
// */
// <T> ServiceConfiguration<T> providePrototype(Factory<T> factory);
//
// <T> ServiceConfiguration<T> providePrototype(TypeLiteral<T> implementation);
