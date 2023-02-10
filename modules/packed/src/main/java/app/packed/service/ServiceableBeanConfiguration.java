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
import app.packed.util.Key;

/**
 * A configuration of a container bean.
 */
// ServiceableBeanConfiguration?
public class ServiceableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /**
     * Create a new bean configuration.
     *
     * @param handle
     *            the bean handle this configuration wraps
     */
    public ServiceableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
    }

    /** {@return the default key that services will be provided as.} */
    public Key<T> defaultKey() {
        return instanceHandle().defaultKey();
    }

    public ServiceableBeanConfiguration<T> export() {
        return exportAs(defaultKey());
    }

    public ServiceableBeanConfiguration<T> exportAs(Class<? super T> key) {
        return exportAs(Key.of(key));
    }

    public ServiceableBeanConfiguration<T> exportAs(Key<? super T> key) {
        instanceHandle().exportAs(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceableBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ServiceableBeanConfiguration<T> provide() {
        return provideAs(defaultKey());
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
    public ServiceableBeanConfiguration<T> provideAs(Class<? super T> key) {
        return provideAs(Key.of(key));
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
    public ServiceableBeanConfiguration<T> provideAs(Key<? super T> key) {
        instanceHandle().provideAs(key);
        return this;
    }
}
