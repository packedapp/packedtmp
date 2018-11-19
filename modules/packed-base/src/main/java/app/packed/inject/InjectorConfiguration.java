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

import app.packed.bundle.Bundles;
import app.packed.bundle.InjectorBundle;
import app.packed.util.Nullable;
import app.packed.util.Taggable;

/**
 * A configuration object for an {@link Injector}. This interface is typically used when configuring a new injector via
 * {@link Injector#of(Consumer)}.
 */
public interface InjectorConfiguration extends Taggable {

    /**
     * Binds the specified implementation as a new service. The runtime will use {@link Factory#findInjectable(Class)} to
     * find a valid constructor or method to instantiate the service instance once the injector is created.
     * <p>
     * The default key for the service will be the specified {@code implementation}. If the implementation is annotated with
     * a {@link Qualifier qualifier annotation}, the default key will have the qualifier annotation added.
     *
     * @param <T>
     *            the type of service to bind
     * @param implementation
     *            the implementation to bind
     * @return a service configuration for the service
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

    default void deployInjector(Class<? extends InjectorBundle> bundleType, ServiceFilter... stages) {
        deployInjector(Bundles.instantiate(bundleType), stages);
    }

    void deployInjector(InjectorBundle bundle, ServiceFilter... stages);

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
     * Imports the services that are available in the specified injector.
     *
     * @param injector
     *            the injector to import services from
     * @param stages
     *            any number of stages that restricts or transforms the services that are imported
     */
    void importServices(Injector injector, ServiceImportStage... stages);

    /**
     * Sets the specified {@link Lookup lookup object} that will be used to instantiate new objects using constructors,
     * invoke methods, and read and write fields. The lookup object will be used for all service binding and component
     * installations that happens after the invocation of this method.
     * <p>
     * This method can be invoked multiple times. In all cases the object being bound or installed will use the latest
     * registered lookup object.
     * <p>
     * Lookup objects that have been explicitly set using {@link Factory#withLookup(java.lang.invoke.MethodHandles.Lookup)}
     * are never overridden by any lookup object set using the method.
     * <p>
     * If no lookup is specified using this method, the runtime will use the public lookup
     * ({@link MethodHandles#publicLookup()}) for member access.
     *
     * @param lookup
     *            the lookup object to use
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

// Disse giver ingen mening.
// Man kan enten, lav en ny injector via Injector.of() og saa importere den via importInjector
// Eller Saa lave en bundle
// Det eneste skulle være hvis de skal bruge nogle dependencies, og saa selv giver nogen
// default Map<Key<?>, ServiceConfiguration<?>> importFrom(Bundle bundle) {
// throw new UnsupportedOperationException();
// }
//
// default Map<Key<?>, ServiceConfiguration<?>> importFrom(Bundle bundle, Predicate<? super ServiceDescriptor> filter) {
// throw new UnsupportedOperationException();
// }
//
// default Map<Key<?>, ServiceConfiguration<?>> importFrom(Class<? extends Bundle> bundle) {
// throw new UnsupportedOperationException();
// }
//
// default Map<Key<?>, ServiceConfiguration<?>> importFrom(Class<? extends Bundle> bundle, Predicate<? super
// ServiceDescriptor> filter) {
// throw new UnsupportedOperationException();
// }
// class D {
// public static void main(InjectorConfiguration c) {
// c.bind(new Factory0<>(System::currentTimeMillis) {});
// c.bind(Long.class, System::currentTimeMillies);
// Denne sidste giver ingen mening, saa det kan kun betale sig at have den med en class
// c.bind(new TypeLiteral<Long>() {}, System::currentTimeMillies));
// }
// }
// Name?, Nahhh syntes ikke vi behoever det, Context Yes

// void optimizeFor(Optimizer.Speed);
// Hvis man koerer den som det foerste....
// enum Optimizer {MEMORY, SPEED, ECT...)
// Paa en eller anden maade skal det foere tilbage til base environment..
// Altsaa den skal kunne overskrive, f.eks. i development, er det altid fejlmeddelelser foerst
