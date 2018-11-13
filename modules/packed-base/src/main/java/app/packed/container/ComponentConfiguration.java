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
package app.packed.container;

import app.packed.inject.Factory;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Nullable;

/**
 * The configuration of a components. A component configuration instance is usually obtained by calling one of the
 * install methods on XX or {@link ComponentConfiguration} at build time. Or one of the install methods on
 * {@link Component} at runtime.
 */
// add getChildren()?
// add getComponentType() <- The type
// Syntes stadig vi skal overskrive component annotations med mixins //non-repeat overwrite, repeat add...
// Mixins er jo lidt limited nu. Kan jo ikke f.eks. lave
public interface ComponentConfiguration<T> extends ServiceConfiguration<T> {

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
    ComponentConfiguration<T> addMixin(Class<?> implementation);

    /**
     * Adds the specified mixin to the list of mixins for the component.
     *
     * @param mixin
     *            the mixin (factory) to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the factory does not produce a proper mixin class ({@code super != Object.class } or implements one or
     *             more interfaces)
     * @see #addMixin(Class)
     * @see #addMixin(Object)
     */
    ComponentConfiguration<T> addMixin(Factory<?> factory);

    /**
     * Adds the specified mixin instance to this component. The mixin can either be a class in which case it will be
     * instantiated and injected according to same rules as the component instance. Or an instance in which case it will
     * only be injected.
     *
     * @param mixin
     *            the mixin instance to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the instance is not a proper mixin class ({@code super != Object.class } or implements one or more
     *             interfaces)
     * @see #addMixin(Class)
     * @see #addMixin(Factory)
     */
    ComponentConfiguration<T> addMixin(Object instance);

    /**
     * Will not participate as <b>source</b> in dependency injection
     * 
     * @return
     */
    ComponentConfiguration<?> asNone();

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
    ComponentConfiguration<T> as(Class<? super T> key);

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
    ComponentConfiguration<T> as(Key<? super T> key);

    /**
     * Returns the description of this component. Or null if the description has not been set.
     *
     * @return the description of this component. Or null if the description has not been set.
     * @see #setDescription(String)
     * @see Component#getDescription()
     */
    @Override
    @Nullable
    String getDescription();

    /**
     * If the main component instance is registered as a service, returns the key under which it is registered. Otherwise
     * returns null.
     *
     * @return if the main component instance is registered as a service, returns the key under which it is registered.
     *         Otherwise returns null
     */
    //
    // /**
    // * Returns the key that this service is bound to. Services does not support bindings to null, however
    // * ComponentConfiguration does. In which the object is installed as a component with all of its lifecycle capabilities
    // * but not available as a service.
    // *
    // * @return the key that this service is bound to.
    // */
    // @Override
    // Key<T> getKey();

    @Override
    @Nullable
    Key<T> getKey();

    /**
     * Returns the name of the component or null if the name has not been set.
     *
     * @return the name of the component or null if the name has not been set
     * @see #setName(String)
     * @see Component#getName()
     */
    @Nullable
    String getName();

    /**
     * Install the specified component implementation as a child of this component.
     *
     * @param implementation
     *            the component implementation to install
     * @return the configuration of the child component
     * @see ComponentListener#componentConfiguration_new(ComponentConfiguration)
     */
    // @NeedsJavadoc
    <S> ComponentConfiguration<S> install(Class<S> implementation);

    /**
     * Installs a new child to this configuration, which uses the specified factory to instantiate the component instance.
     *
     * @param factory
     *            the factory used to instantiate the component instance
     * @return the configuration of the child component
     * @see ComponentListener#componentConfiguration_new(ComponentConfiguration)
     */
    // @NeedsJavadoc
    <S> ComponentConfiguration<S> install(Factory<S> factory);

    /**
     * Install the specified component instance as a child of this component.
     *
     * @param implementation
     *            the component instance to install
     * @return the configuration of the child component
     */
    // @NeedsJavadoc
    <S> ComponentConfiguration<S> install(S instance);//

    /**
     * Returns the configuration of the components injector. This injector is responsible for any dependency injection
     * needed for this component. For example, for instantiating the component instance or any of its mixins. The injector
     * can accessed via {@link Component#injector()} at runtime.
     * 
     * @see Component#injector()
     */
    // Or privateInjector
    // Do example, with listener, on instance annotation
    InjectorConfiguration privates();

    /**
     * Sets the description of this component.
     *
     * @param description
     *            the description of the component
     * @return this configuration
     * @see #getDescription()
     * @see Component#getDescription()
     */
    @Override
    ComponentConfiguration<T> setDescription(String description);

    /**
     * Sets the {@link Component#getName() name} of the component. The name must consists only of alphanumeric characters
     * and '_', '-' or '.'. The name is case sensitive.
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
     * @see Component#getName()
     */
    ComponentConfiguration<T> setName(String name);

    // default ContainerActionable on(LifecycleState... states) {
    // throw new UnsupportedOperationException();
    // }
}
