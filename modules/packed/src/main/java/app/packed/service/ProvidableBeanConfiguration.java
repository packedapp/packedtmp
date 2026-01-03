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

import java.util.function.Function;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.binding.BindingHandle;
import app.packed.binding.Key;
import app.packed.service.mirror.ServiceBindingMirror;

/**
 * A configuration of a bean that can be provided as a service.
 */
// Was ServiceableBeanConfiguration?
public class ProvidableBeanConfiguration<T> extends BeanConfiguration<T> {

    /**
     * Create a new configuration.
     *
     * @param handle
     *            the bean's handle
     */
    public ProvidableBeanConfiguration(BeanHandle<?> handle) {
        super(handle);
    }

    /** {@inheritDoc} */
    @Override
    public ProvidableBeanConfiguration<T> allowMultiClass() {
        super.allowMultiClass();
        return this;
    }

    /** {@return the default key that services will be provided as.} */
    @SuppressWarnings("unchecked")
    public Key<T> defaultKey() {
        return (Key<T>) handle().defaultKey();
    }

    public ProvidableBeanConfiguration<T> export() {
        return exportAs(defaultKey());
    }

    public ProvidableBeanConfiguration<T> exportAs(Class<? super T> key) {
        return exportAs(Key.of(key));
    }

    public ProvidableBeanConfiguration<T> exportAs(Key<? super T> key) {
        handle().exportAs(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidableBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ProvidableBeanConfiguration<T> provide() {
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
    public ProvidableBeanConfiguration<T> provideAs(Class<? super T> key) {
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
    public ProvidableBeanConfiguration<T> provideAs(Key<? super T> key) {
        handle().provideAs(key);
        return this;
    }

    public ProvidableBeanConfiguration<T> specializeMirror(Function<? super BindingHandle, ? extends ServiceBindingMirror> factory) {
        throw new UnsupportedOperationException();
    }
}
