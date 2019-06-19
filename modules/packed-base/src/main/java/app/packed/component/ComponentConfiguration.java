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

import app.packed.config.ConfigSite;
import app.packed.container.Bundle;
import app.packed.inject.Factory;
import app.packed.util.Nullable;

// isConfigurable();
// checkConfigurable();
// add getChildren()?
// add getComponentType() <- The type
// Syntes stadig vi skal overskrive component annotations med mixins //non-repeat overwrite, repeat add...
// Mixins er jo lidt limited nu. Kan jo ikke f.eks. lave

/**
 * This class represents the configuration of a component. An actual instance is usually obtained by calling one of the
 * install methods on, for example, {@link Bundle} or on another component configuration. It it also possible to install
 * components at runtime via {@link Component}.
 */
public interface ComponentConfiguration {

    // Tror tit man godt selv vil instantiere den...
    default <T extends FeatureHolder<?, ?>> T addFeature(Class<T> featureType) {
        throw new UnsupportedOperationException();
    }

    default <T extends FeatureHolder<?, ?>> T addFeature(T feature) {
        return feature;
    }

    /**
     * Returns the configuration site where this configuration was created.
     * 
     * @return the configuration site where this configuration was created
     */
    ConfigSite configurationSite();

    // TypeAnnotations are ignored for now...
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
    default ComponentConfiguration addMixin(Class<?> implementation) {
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
    default ComponentConfiguration addMixin(Factory<?> factory) {
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
    default ComponentConfiguration addMixin(Object instance) {
        throw new UnsupportedOperationException();
    }

    default ComponentConfiguration addMixinClass(Class<?> mixin) {
        // Hvordan opfoere de sig med de forskellige typer... f.eks. prototype services...
        // Prototypeservice er en type!

        // Denne metode instantiere aldrig
        throw new UnsupportedOperationException();
    }

    // Ville vaere rigtig rart at vi havde noget support for at fryse navnet, isaer mht til actors...
    // ComponentPath path() <- once invoked, the name cannot be changed....

    /**
     * Returns the description of this component. Or null if the description has not been set.
     *
     * @return the description of this component. Or null if the description has not been set.
     * @see #setDescription(String)
     * @see Component#description()
     */
    @Nullable
    String getDescription();

    /**
     * Returns the name of the component or null if the name has not been set.
     *
     * @return the name of the component or null if the name has not been set
     * @see #setName(String)
     * @see Component#name()
     */
    @Nullable
    String getName();

    default Class<?> type() {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the description of this component.
     *
     * @param description
     *            the description to set
     * @return this configuration
     * @see #getDescription()
     * @see Component#description()
     */
    ComponentConfiguration setDescription(@Nullable String description);

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
    ComponentConfiguration setName(@Nullable String name);
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