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
package app.packed.bean;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.bean.ProvidableBeanConfiguration.ServiceableBean;

/**
 * A configuration of a container bean.
 */
// Tror ikke den kan noget PBC ikke kan.
// Men vi kan kraeve den som parameter nogle steder.
public class ContainerBeanConfiguration<T> extends InstanceBeanConfiguration<T> {
    
    private final ServiceableBean sb;

    /**
     * @param handle
     *            the bean driver to use for creating the bean
     */
    public ContainerBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        this.sb = new ServiceableBean(beanHandle);
        handle.addWiringAction(() -> sb.onWired());
    }


    public ContainerBeanConfiguration<T> export() {
        sb.export();
        return this;
    }

    public ContainerBeanConfiguration<T> exportAs(Class<? super T> key) {
        sb.export();
        return this;
    }

    public ContainerBeanConfiguration<T> exportAs(Key<? super T> key) {
        sb.export();
        return this;
    }
    
    
    public ContainerBeanConfiguration<T> provide() {
        sb.provide();
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
    public ContainerBeanConfiguration<T> provideAs(Class<? super T> key) {
        sb.provideAs(key);
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
    public ContainerBeanConfiguration<T> provideAs(Key<? super T> key) {
        sb.provideAs(key);
        return this;
    }

    // Ser dum ud naar man laver completion
    public Optional<Key<?>> providedAs() {
        return sb.providedAs();
    }

    ContainerBeanConfiguration<T> describeAs(String description) {
        // describeExportAs
        // describeProvisionAs
        return this;
    }
}
//
//public <X extends Runnable & Callable<String>> X foo() {
//  return null;
//}