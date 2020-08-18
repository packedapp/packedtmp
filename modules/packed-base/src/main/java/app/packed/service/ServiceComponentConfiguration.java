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

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Component;
import app.packed.component.SingletonConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.BaseBundle;
import app.packed.inject.Factory;

/**
 * This configuration represents an entity that is both a {@link ServiceConfiguration configuration of as service} and a
 * {@link SingletonConfiguration configuration of as component}.
 * <p>
 * An instance of this interface is usually obtained by calling one of the provide methods on {@link ServiceExtension}
 * or {@link BaseBundle}.
 * 
 * @see ServiceExtension#provide(Class)
 * @see ServiceExtension#provide(Factory)
 * @see ServiceExtension#provideConstant(Object)
 *
 * @see BaseBundle#provide(Class)
 * @see BaseBundle#provide(Factory)
 * @see BaseBundle#provideConstant(Object)
 */
public interface ServiceComponentConfiguration<T> extends ServiceConfiguration<T>, SingletonConfiguration<T> {

    //// Can be used to set separately tags, descriptions, ect...
    // SingletonConfiguration<T> componentConfiguration();
    // ServiceConfiguration<T> serviceConfiguration();

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    @Override
    default ServiceComponentConfiguration<T> as(Class<? super T> key) {
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
    @Override
    ServiceComponentConfiguration<T> as(Key<? super T> key);

    /**
     * Returns the configuration site where this configuration was created.
     * 
     * @return the configuration site where this configuration was created
     */
    @Override
    ConfigSite configSite();

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
    @Override
    Key<?> getKey();

    /**
     * Returns the instantiation mode of the service.
     *
     * @return the instantiation mode of the service
     */
    @Override
    ServiceMode instantiationMode();

    /**
     * Sets the description of this service.
     *
     * @param description
     *            the description of the service
     * @return this configuration
     * @see #getDescription()
     */
    @Override
    ServiceComponentConfiguration<T> setDescription(@Nullable String description);

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
    ServiceComponentConfiguration<T> setName(String name);
}

// /**
// * @param implementation
// * the mixin implementation to add
// * @return this configuration
// * @throws IllegalArgumentException
// * if the class is not a proper mixin class ({@code super != Object.class } or implements one or more
// * interfaces)
// * @see #addMixin(Factory)
// * @see #addMixin(Object)
// */
// @Override
// default ProvidedComponentConfiguration<T> addMixin(Class<?> implementation) {
// throw new UnsupportedOperationException();
// }
//
// /**
// * Adds the specified mixin to the list of mixins for the component.
// *
// * @param factory
// * the mixin (factory) to add
// * @return this configuration
// * @throws IllegalArgumentException
// * if the factory does not produce a proper mixin class ({@code super != Object.class } or implements one or
// * more interfaces)
// * @see #addMixin(Class)
// * @see #addMixin(Object)
// */
// @Override
// default ProvidedComponentConfiguration<T> addMixin(Factory<?> factory) {
// throw new UnsupportedOperationException();
// }
//
// /**
// * Adds the specified mixin instance to this component. The mixin can either be a class in which case it will be
// * instantiated and injected according to same rules as the component instance. Or an instance in which case it will
// * only be injected.
// *
// * @param instance
// * the mixin instance to add
// * @return this configuration
// * @throws IllegalArgumentException
// * if the instance is not a proper mixin class ({@code super != Object.class } or implements one or more
// * interfaces)
// * @see #addMixin(Class)
// * @see #addMixin(Factory)
// */
// @Override
// default ProvidedComponentConfiguration<T> addMixin(Object instance) {
// throw new UnsupportedOperationException();
// }

// default ContainerActionable on(LifecycleState... states) {
// throw new UnsupportedOperationException();
// }
/// **
// * Prohibits the component for being available as a dependency to other services/components.
// *
// * @return this component configuration
// */
// @Override
// ComponentServiceConfiguration<?> asNone();