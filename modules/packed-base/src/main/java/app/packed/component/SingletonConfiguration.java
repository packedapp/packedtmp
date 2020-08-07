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
import app.packed.inject.Factory;

/**
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseBundle}.
 * <p>
 * It it also possible to install components at runtime via {@link Component}.
 */

// SingletonConfiguration -> Noget der laver en single instance der kan bruges af andre.
// Maaske endda registereres som service
public interface SingletonConfiguration<T> extends SourcedComponentConfiguration<T> {

    /** {@inheritDoc} */
    @Override
    SingletonConfiguration<T> setDescription(String description);

    /** {@inheritDoc} */
    @Override
    SingletonConfiguration<T> setName(String name);

    // The component can be removed at runtime, separately from its container.
    // But again its not supported now...
    default SingletonConfiguration<T> removable() {
        throw new UnsupportedOperationException();
    }
}

interface XCC2<T> {
    // isConfigurable();
    // add getChildren()?
    // add getComponentType() <- The type

    // Syntes stadig vi skal overskrive component annotations med mixins //non-repeat overwrite, repeat add...
    // Mixins er jo lidt limited nu. Kan jo ikke f.eks. lave
    //
    // default boolean isStateful() {
    // return false;// Alternative we have a Component.Mode with Stateful, Stateless, Other
    // }
    //
    // default boolean isStateless() {
    // return !isStateless();
    // }
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
    default SingletonConfiguration<T> addMixin(Class<?> implementation) {
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
    default SingletonConfiguration<T> addMixin(Factory<?> factory) {
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
    default SingletonConfiguration<T> addMixin(Object instance) {
        throw new UnsupportedOperationException();
    }

    default SingletonConfiguration<T> addMixinClass(Class<?> mixin) {
        // Hvordan opfoere de sig med de forskellige typer... f.eks. prototype services...
        // Prototypeservice er en type!

        // Denne metode instantiere aldrig
        throw new UnsupportedOperationException();
    }
}
