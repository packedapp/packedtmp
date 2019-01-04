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
package app.packed.inject;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.BundlingStage;
import app.packed.bundle.BundlingImportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.util.Nullable;
import app.packed.util.Qualifier;
import app.packed.util.Taggable;
import app.packed.util.TypeLiteral;
import packed.internal.bundle.Bundles;

/**
 * A configuration object for an {@link Injector}. This interface is typically used when configuring a new injector via
 * {@link Injector#of(Consumer)}.
 */
public interface InjectorConfiguration extends Taggable {

    /**
     * Binds the specified implementation as a new singleton service. An instance of the implementation will be created
     * together with the injector that this configurations create. The runtime will use
     * {@link Factory#findInjectable(Class)} to find the constructor or method used for instantiation.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the specified {@code Class} is
     * annotated with a {@link Qualifier qualifier annotation}, the default key will include the qualifier. For example,
     * given this implementation: <pre>
     * &#64;SomeQualifier
     * public class SomeService {}
     * </pre>
     * <p>
     * The following two example are equivalent
     * </p>
     * <pre> 
     * Injector i = Injector.of(c -&gt; { 
     *    c.bind(SomeService.class); 
     * });
     * </pre> <pre> 
     * Injector i = Injector.of(c -&gt; { 
     *   c.bind(SomeService.class).as(new Key&lt;&#64;SomeQualifier SomeService&gt;() {});
     * });
     * </pre>
     * 
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
     * @see Bundle#bind(Class)
     */
    <T> ServiceConfiguration<T> bind(Class<T> implementation);

    /**
     * Binds the specified factory to a new service. When the injector is created the factory will be invoked <b>once</b> to
     * instantiate the service instance.
     * <p>
     * The default key for the service is determined by {@link Factory#getKey()}.
     * 
     * @param <T>
     *            the type of service to bind
     * @param factory
     *            the factory to bind
     * @return a service configuration for the service
     */
    <T> ServiceConfiguration<T> bind(Factory<T> factory);

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
    <T> ServiceConfiguration<T> bind(T instance);

    <T> ServiceConfiguration<T> bind(TypeLiteral<T> implementation);

    /**
     * @param bundleType
     *            the type of bundle to instantiate
     * @param stages
     *            optional stages
     */
    default void bindInjector(Class<? extends InjectorBundle> bundleType, BundlingStage... stages) {
        bindInjector(Bundles.instantiate(bundleType), stages);
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
     *   c.injectorBind(importFrom);
     * );
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
     *   c.injectorBind(importTo, InjectorImportStage.reject(Integer.class));
     * });
     * </pre> @param injector the injector to bind services from
     * 
     * @param injector
     *            the injector to import services from
     * @param stages
     *            any number of stages that restricts or transforms the services that are imported
     * @throws IllegalArgumentException
     *             if the specified stages are not instance all instance of {@link BundlingImportStage} or combinations (via
     *             {@link BundlingStage#andThen(BundlingStage)} thereof
     */
    void bindInjector(Injector injector, BundlingStage... stages);

    /**
     * @param bundle
     *            the bundle to bind
     * @param stages
     *            optional import/export stages
     */
    void bindInjector(InjectorBundle bundle, BundlingStage... stages);

    /**
     * Binds the specified implementation lazily. This is equivalent to {@link #bind(Class)} except that the instance will
     * not be instantiatied until it is requested, possible never.
     * 
     * @param <T>
     *            the type of service
     * @param implementation
     *            the implementation to bind
     * @return a service configuration object
     */
    <T> ServiceConfiguration<T> bindLazy(Class<T> implementation);

    /**
     * Binds the specified factory to a new service. The first time the service is requested, the factory will be invoked to
     * instantiate the service instance. The instance produced by the factory will be used for all subsequent requests. The
     * runtime guarantees that at most service instance is ever created, blocking concurrent requests to the instance at
     * creation time.
     *
     * @param <T>
     *            the type of service to bind
     * @param factory
     *            the factory to bind
     * @return a service configuration for the service
     */
    <T> ServiceConfiguration<T> bindLazy(Factory<T> factory);

    <T> ServiceConfiguration<T> bindLazy(TypeLiteral<T> implementation);

    <T> ServiceConfiguration<T> bindPrototype(Class<T> implementation);

    /**
     * Binds the specified factory to a new service. When the service is requested the factory is used to create a new
     * instance of the service. The runtime will never cache instances, once they are returned to the client requesting the
     * service, the runtime will keep no references to them.
     *
     * @param <T>
     *            the type of service to bind
     * @param factory
     *            the factory to bind
     * @return a service configuration for the service
     */
    <T> ServiceConfiguration<T> bindPrototype(Factory<T> factory);

    <T> ServiceConfiguration<T> bindPrototype(TypeLiteral<T> implementation);

    /**
     * Returns the description of this injector, or null if no description has been set via {@link #setDescription(String)}.
     *
     * @return the description of this injector, or null if no description has been set via {@link #setDescription(String)}.
     * @see Injector#getDescription()
     * @see #setDescription(String)
     */
    @Nullable
    String getDescription();

    /**
     * Sets a {@link Lookup lookup object} that will be used to access members (fields, constructors and methods) on
     * registered objects. The lookup object will be used for all service binding and component installations that happens
     * after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * Lookup objects that have been explicitly set using {@link Factory#withLookup(java.lang.invoke.MethodHandles.Lookup)}
     * are never overridden by any lookup object set using this method.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object
     */
    void lookup(MethodHandles.Lookup lookup);

    /**
     * Sets the (nullable) description of this injector, the description can later be obtained via
     * {@link Injector#getDescription()}.
     *
     * @param description
     *            a (nullable) description of this injector
     * @return this configuration
     * @see #getDescription()
     * @see Injector#getDescription()
     */
    InjectorConfiguration setDescription(@Nullable String description);
}
