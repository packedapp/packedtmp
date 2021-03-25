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
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BindableComponentDriver;
import app.packed.component.Component;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.container.BaseAssembly;
import packed.internal.component.SourcedComponentDriver;

/**
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 * <p>
 * It it also possible to install components at runtime via {@link Component}.
 */
//ProvidableComponentConfiguration
public class ServiceComponentConfiguration<T> extends BaseComponentConfiguration implements ServiceConfiguration<T> {

    @SuppressWarnings("rawtypes")
    private static final BindableComponentDriver DRIVER = SourcedComponentDriver.ofInstance(MethodHandles.lookup(), ServiceComponentConfiguration.class,
            true);

    @SuppressWarnings("rawtypes")
    private static final ComponentDriver PROTOTYPE_DRIVER = SourcedComponentDriver.ofFactory(MethodHandles.lookup(),
            ServiceComponentConfiguration.class, false);

    public ServiceComponentConfiguration(ComponentConfigurationContext context) {
        super(context);
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
    public ServiceComponentConfiguration<T> as(Class<? super T> key) {
        return as(Key.of(key));
    }

    // addQualififer();
    // Nahh

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Class)
     */
    public ServiceComponentConfiguration<T> as(Key<? super T> key) {
        context.sourceProvideAs(key);
        return this;
    }

    public ServiceComponentConfiguration<T> asNone() {
        // Ideen er vi f.eks. kan
        // asNone().exportAs(Doo.class);
        context.sourceProvideAs(null);
        return this;
    }

    // Once a bean has been exported, its key cannot be changed...
    public ServiceComponentConfiguration<T> export() {
        context.sourceExport();
        return this;
    }

    public ServiceComponentConfiguration<T> exportAs(Class<? super T> key) {
        export().as(key);
        return this;
    }

    public ServiceComponentConfiguration<T> exportAs(Key<? super T> key) {
        export().as(key);
        return this;
    }

    // The key unless asNone()

    // Overvejer at smide... istedet for optional
    public Optional<Key<?>> key() {
        return context.sourceProvideAsKey();
    }

    public ServiceComponentConfiguration<T> provide() {
        context.sourceProvide();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceComponentConfiguration<T> setName(String name) {
        context.setName(name);
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <T> ComponentDriver<ServiceComponentConfiguration<T>> provide(Class<T> implementation) {
        return DRIVER.bind(implementation);
    }

    @SuppressWarnings("unchecked")
    public static <T> ComponentDriver<ServiceComponentConfiguration<T>> provide(Factory<T> factory) {
        return DRIVER.bind(factory);
    }

    @SuppressWarnings("unchecked")
    public static <T> ComponentDriver<ServiceComponentConfiguration<T>> provideInstance(T instance) {
        return DRIVER.applyInstance(instance);
    }

    @SuppressWarnings("unchecked")
    public static <T> ComponentDriver<ServiceComponentConfiguration<T>> providePrototype(Class<T> implementation) {
        return PROTOTYPE_DRIVER.bind(implementation);
    }

    @SuppressWarnings("unchecked")
    public static <T> ComponentDriver<ServiceComponentConfiguration<T>> providePrototype(Factory<T> factory) {
        return PROTOTYPE_DRIVER.bind(factory);
    }
}

class Zandb0x<T> {

    public Zandb0x<T> aliasAs(Class<? super T> key) {
        return this;
    }
}