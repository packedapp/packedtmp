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

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.component.BaseBeanConfiguration;
import app.packed.container.BaseAssembly;
import app.packed.inject.sandbox.ExportedServiceConfiguration;

/**
 * A bean which provide an instance(s) of the bean type as a service.
 * <p>
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 */
//ProvidableComponentConfiguration
// Serviceable
public class ServiceBeanConfiguration<T> extends BaseBeanConfiguration {

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    public ServiceBeanConfiguration<T> as(Class<? super T> key) {
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
    public ServiceBeanConfiguration<T> as(Key<? super T> key) {
        super.provideAsService(key);
        return this;
    }
    public ServiceBeanConfiguration<T> asNone() {
        // Ideen er vi f.eks. kan
        // asNone().exportAs(Doo.class);
        super.provideAsService(null);
        return this;
    }

    /** {@inheritDoc} */
    public ExportedServiceConfiguration<T> export() {
        return super.exportAsService();
    }

    // Overvejer at smide... istedet for optional
    public Optional<Key<?>> key() {
        return super.sourceProvideAsKey();
    }

    // The key unless asNone()

    /** {@inheritDoc} */
    @Override
    public ServiceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return super.path();
    }

    public ServiceBeanConfiguration<T> provide() {
        super.provideAsService();
        return this;
    }
}
