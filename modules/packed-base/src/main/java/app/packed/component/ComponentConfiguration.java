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
package app.packed.component;

import app.packed.container.BaseBundle;
import app.packed.service.Factory;

// isConfigurable();
// add getChildren()?
// add getComponentType() <- The type

// Syntes stadig vi skal overskrive component annotations med mixins //non-repeat overwrite, repeat add...
// Mixins er jo lidt limited nu. Kan jo ikke f.eks. lave

/**
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseBundle}.
 * <p>
 * It it also possible to install components at runtime via {@link Component}.
 */
public interface ComponentConfiguration<T> extends BaseComponentConfiguration {

    /**
     * Sets the description of this component.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @see #getDescription()
     * @see Component#description()
     */
    @Override
    ComponentConfiguration<T> setDescription(String description);

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
     * <p>
     * If no name is set using this method. A name will be assigned to the component when the component is initialized, in
     * such a way that it will have a unique name other sibling components.
     *
     * @param name
     *            the name of the component
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the specified name is the empty string, or if the name contains other characters then alphanumeric
     *             characters and '_', '-' or '.'
     * @see #getName()
     * @see Component#name()
     */
    @Override
    ComponentConfiguration<T> setName(String name);

    default Class<?> type() {
        // instanceType???
        throw new UnsupportedOperationException();
    }
    //
    // default boolean isStateful() {
    // return false;// Alternative we have a Component.Mode with Stateful, Stateless, Other
    // }
    //
    // default boolean isStateless() {
    // return !isStateless();
    // }
}

interface XCC2<T> {

    /**
     * 
     * @param implementation
     *            the mixin implementation to add
     * @return this configuration
     * @throws IllegalArgumentException
     *             if the class is not a proper mixin class ({@code super != Object.class } or implements one or more
     *             interfaces)
     * @see #addMixin(Factory)
     * @see #addMixin(Object)
     */
    default ComponentConfiguration<T> addMixin(Class<?> implementation) {
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
    default ComponentConfiguration<T> addMixin(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    /**
     * Adds a component mixin to this component. The mixin can either be a class in which case it will be instantiated and
     * injected according to same rules as the component instance. Or an instance in which case it will only be injected.
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
    default ComponentConfiguration<T> addMixin(Object instance) {
        throw new UnsupportedOperationException();
    }

    default ComponentConfiguration<T> addMixinClass(Class<?> mixin) {
        // Hvordan opfoere de sig med de forskellige typer... f.eks. prototype services...
        // Prototypeservice er en type!

        // Denne metode instantiere aldrig
        throw new UnsupportedOperationException();
    }
}
/**
 * Returns an injector configurator for this component. This configurator can be used to provide service specifically to
 * the underlying component instance or any of its mixins.
 * 
 * injector is responsible for any dependency injection needed for this component. For example, for instantiating the
 * component instance or any of its mixins. The injector can accessed via {@link Component#injector()} at runtime.
 * 
 * @return a the component's injector
 * @see Component#injector()
 */
// Or privateInjector
// Do example, with listener, on instance annotation
// injectorInheritable()... an injector that is inherited by all
// Nu er vi taet knyttet til services....
// Maasske Hellere end Service.private(ComponentConfiguration cc).bind(dddd);
// InjectorConfigurator injector();