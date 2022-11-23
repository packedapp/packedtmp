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

import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;

/**
 * A configuration of a container bean.
 */
// ServiceableBeanConfiguration?
public class ProvideableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * @param handle
     *            the bean driver to use for creating the bean
     */
    public ProvideableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    public Key<T> defaultKey() {
        return handle().defaultKey();
    }

    ProvideableBeanConfiguration<T> describeAs(String description) {
        // describeExportAs
        // describeProvisionAs
        return this;
    }

    public ProvideableBeanConfiguration<T> export() {
        return exportAs(defaultKey());
    }

    public ProvideableBeanConfiguration<T> exportAs(Class<? super T> key) {
        return exportAs(Key.of(key));
    }

    public ProvideableBeanConfiguration<T> exportAs(Key<? super T> key) {
        handle().serviceExportAs(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWithInstance(Class<K> key, K instance) {
        super.initializeWithInstance(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWithInstance(Key<K> key, K instance) {
        super.initializeWithInstance(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvideableBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ProvideableBeanConfiguration<T> provide() {
        Key<T> defaultKey = handle().defaultKey();
        handle().serviceProvideAs(defaultKey);
        return this;
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Key)
     */
    public ProvideableBeanConfiguration<T> provideAs(Class<? super T> key) {
        handle().serviceProvideAs(Key.of(key));
        return this;
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Class)
     */
    public ProvideableBeanConfiguration<T> provideAs(Key<? super T> key) {
        handle().serviceProvideAs(key);
        return this;
    }
}
