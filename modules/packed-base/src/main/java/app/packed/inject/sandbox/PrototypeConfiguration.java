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
package app.packed.inject.sandbox;

import app.packed.base.Key;
import app.packed.bundle.BaseAssembly;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.Component;
import app.packed.component.ComponentConfigurationContext;
import app.packed.inject.Factory;
import app.packed.inject.ServiceExtension;

/**
 * A prototype service configuration represents an entity that is both a {@link ExportedServiceConfiguration
 * configuration of as service} and a .
 * <p>
 * An instance of this interface is usually obtained by calling one of the provide methods on {@link ServiceExtension}
 * or {@link BaseAssembly}.
 * 
 * @see BaseAssembly#providePrototype(Class)
 * @see BaseAssembly#providePrototype(Factory)
 * @see ServiceExtension#providePrototype(Factory)
 */
//PrototypeServiceConfiguration??
// Extends Stateless???
public final class PrototypeConfiguration<T> extends AbstractComponentConfiguration {

    /**
     * Creates a new configuration object
     */
    public PrototypeConfiguration(ComponentConfigurationContext component) {
        super(component);
        context.sourceProvide();
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    public PrototypeConfiguration<T> as(Class<? super T> key) {
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
    public PrototypeConfiguration<T> as(Key<? super T> key) {
        context.sourceProvideAs(key);
        return this;
    }

    /**
     * Returns the key that the service is registered under.
     *
     * @return the key that the service is registered under
     * @see #as(Key)
     * @see #as(Class)
     */
    public Key<?> getKey() {
        return context.sourceProvideAsKey().get();
    }

    /**
     * Sets the {@link Component#name() name} of the component. The name must consists only of alphanumeric characters and
     * '_', '-' or '.'. The name is case sensitive.
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
     * @see Component#name()
     */
    @Override
    public PrototypeConfiguration<T> setName(String name) {
        context.setName(name);
        return this;
    }

    public ExportedServiceConfiguration<T> export() {
        return context.sourceExport();
    }
}
