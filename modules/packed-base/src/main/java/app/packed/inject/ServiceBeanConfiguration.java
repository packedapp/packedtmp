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
package app.packed.inject;

import java.lang.invoke.MethodHandles;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.container.BaseAssembly;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.PackedBeanConfigurationBinder;

/**
 * A bean whose type is registered as a service.
 * <p>
 * 
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 */
//ProvidableComponentConfiguration
// Serviceable
public class ServiceBeanConfiguration<T> extends BeanConfiguration {

    @SuppressWarnings("rawtypes")
    private static final PackedBeanConfigurationBinder DRIVER = PackedBeanConfigurationBinder.ofInstance(MethodHandles.lookup(), ServiceBeanConfiguration.class,
            true);

    @SuppressWarnings("rawtypes")
    private static final PackedBeanConfigurationBinder PROTOTYPE_DRIVER = PackedBeanConfigurationBinder.ofFactory(MethodHandles.lookup(),
            ServiceBeanConfiguration.class, false);

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
        super.sourceProvideAs(key);
        return this;
    }

    public ServiceBeanConfiguration<T> asNone() {
        // Ideen er vi f.eks. kan
        // asNone().exportAs(Doo.class);
        super.sourceProvideAs(null);
        return this;
    }

    /** {@inheritDoc} */
    public ExportedServiceConfiguration<T> export() {
        return super.sourceExport();
    }

    // The key unless asNone()

    // Overvejer at smide... istedet for optional
    public Optional<Key<?>> key() {
        return super.sourceProvideAsKey();
    }

    public ServiceBeanConfiguration<T> provide() {
        super.sourceProvide();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    // Hvordan giver folk med foldere mulighed for at expose dem her...
    // Hvorfor ikke
    // Det tror jeg maaske ikke vi goer
    @SuppressWarnings("unchecked")
    static <T> ComponentDriver<ServiceBeanConfiguration<T>> provide(Class<T> implementation) {
        return DRIVER.bind(implementation);
    }

    @SuppressWarnings("unchecked")
    static <T> ComponentDriver<ServiceBeanConfiguration<T>> provide(Factory<T> factory) {
        return DRIVER.bind(factory);
    }

    @SuppressWarnings("unchecked")
    static <T> ComponentDriver<ServiceBeanConfiguration<T>> provideInstance(T instance) {
        return DRIVER.bindInstance(instance);
    }

    @SuppressWarnings("unchecked")
    static <T> ComponentDriver<ServiceBeanConfiguration<T>> providePrototype(Class<T> implementation) {
        return PROTOTYPE_DRIVER.bind(implementation);
    }

    @SuppressWarnings("unchecked")
    static <T> ComponentDriver<ServiceBeanConfiguration<T>> providePrototype(Factory<T> factory) {
        return PROTOTYPE_DRIVER.bind(factory);
    }
}
