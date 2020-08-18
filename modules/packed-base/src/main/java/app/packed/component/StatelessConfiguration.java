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

import app.packed.inject.Factory;

/**
 *
 */
public interface StatelessConfiguration extends ComponentConfiguration {

    /**
     * Yup
     * 
     * @return yup
     */
    Class<?> definition();

    /** {@inheritDoc} */
    @Override
    StatelessConfiguration setName(String name);

    static <T> ClassSourcedDriver<T, StatelessConfiguration> driver() {
        throw new UnsupportedOperationException();
    }
}

interface Mixins<T> {

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
}