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

import app.packed.bundle.Bundle;
import app.packed.config.ConfigSite;
import app.packed.container.Component;
import app.packed.container.ComponentConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 * The configuration representing an entity that is both being registered as a component and as a service.
 * 
 * 
 * <p>
 * All the methods in this interface is solely added to allow
 * 
 * A component configuration instance is usually obtained by calling one of the install methods on
 * {@link ServiceConfiguration} or {@link Bundle} at configuration time. Or one of the install methods on
 * {@link Component} at runtime.
 */
public interface ServiceConfiguration<T> extends ComponentConfiguration {

    // /**
    // * Prohibits the component for being available as a dependency to other services/components.
    // *
    // * @return this component configuration
    // */
    // @Override
    // ComponentServiceConfiguration<?> asNone();

    /**
     * @param implementation
     *            the mixin implementation to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the class is not a proper mixin class ({@code super != Object.class } or implements one or more
     *             interfaces)
     * @see #addMixin(Factory)
     * @see #addMixin(Object)
     */
    @Override
    default ServiceConfiguration<T> addMixin(Class<?> implementation) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the specified mixin to the list of mixins for the component.
     *
     * @param factory
     *            the mixin (factory) to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the factory does not produce a proper mixin class ({@code super != Object.class } or implements one or
     *             more interfaces)
     * @see #addMixin(Class)
     * @see #addMixin(Object)
     */
    @Override
    default ServiceConfiguration<T> addMixin(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds the specified mixin instance to this component. The mixin can either be a class in which case it will be
     * instantiated and injected according to same rules as the component instance. Or an instance in which case it will
     * only be injected.
     *
     * @param instance
     *            the mixin instance to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the instance is not a proper mixin class ({@code super != Object.class } or implements one or more
     *             interfaces)
     * @see #addMixin(Class)
     * @see #addMixin(Factory)
     */
    @Override
    default ServiceConfiguration<T> addMixin(Object instance) {
        throw new UnsupportedOperationException();
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    default ServiceConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Class)
     */
    ServiceConfiguration<T> as(Key<? super T> key);

    /**
     * Returns the configuration site where this configuration was created.
     * 
     * @return the configuration site where this configuration was created
     */
    @Override
    ConfigSite configurationSite();

    /**
     * Returns the description of this service. Or null if no description has been set.
     *
     * @return the description of this service
     * @see #setDescription(String)
     */
    @Override
    @Nullable
    String getDescription();

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see #as(Key)
     * @see #as(Class)
     */
    Key<?> getKey();

    /**
     * Returns the instantiation mode of the service.
     *
     * @return the instantiation mode of the service
     */
    InstantiationMode instantiationMode();

    /**
     * 
     * 
     * .provide(instance) cannot be lazy
     * 
     * @return this configuration
     * @throws UnsupportedOperationException
     *             if the service cannot be lazy
     */
    ServiceConfiguration<T> lazy();

    ServiceConfiguration<T> prototype();

    /**
     * Sets the description of this service.
     *
     * @param description
     *            the description of the service
     * @return this configuration
     * @see #getDescription()
     */
    @Override
    ServiceConfiguration<T> setDescription(@Nullable String description);

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique path among other components in the container.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    @Override
    ServiceConfiguration<T> setName(String name);

    // default ContainerActionable on(LifecycleState... states) {
    // throw new UnsupportedOperationException();
    // }
}
