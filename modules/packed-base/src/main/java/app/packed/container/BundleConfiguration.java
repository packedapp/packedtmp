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
package app.packed.container;

import java.lang.invoke.MethodHandles;
import java.util.Set;

import app.packed.component.BaseComponentConfiguration;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentDriver.Option;
import app.packed.component.StatelessConfiguration;
import app.packed.inject.Factory;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured by
 * extending {@link BundleAssembly} or {@link BaseAssembly}.
 */
public final class BundleConfiguration extends BaseComponentConfiguration {

    /** A driver that create container components. */
    private static final ComponentDriver<BundleConfiguration> DRIVER = ComponentDriver.of(MethodHandles.lookup(), BundleConfiguration.class, Option.bundle());

    /**
     * Creates a new PackedContainerConfiguration, only used by {@link #DRIVER}.
     *
     * @param context
     *            the component configuration context
     */
    private BundleConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    /**
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see BundleAssembly#extensions()
     */
    public Set<Class<? extends Extension>> extensions() {
        // getAttribute(EXTENSIONS);
        return context.bundleExtensions();
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param <T>
     *            the type of the component
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public <T> BeanConfiguration<T> install(Class<T> implementation) {
        return wire(BeanConfiguration.<T> driver().bind(implementation));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see BundleAssembly#install(Factory)
     */
    public <T> BeanConfiguration<T> install(Factory<T> factory) {
        return wire(BeanConfiguration.<T>driver().bind(factory));
    }

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see BundleAssembly#installInstance(Object)
     */
    public <T> BeanConfiguration<T> installInstance(T instance) {
        return wire(BeanConfiguration.<T>driver().bindInstance(instance));
    }

    /**
     * Installs a stateless component.
     * <p>
     * Extensions might still contain state. So Stateless is better under the assumption that extensions are better tested
     * the user code.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public StatelessConfiguration installStateless(Class<?> implementation) {
        return wire(StatelessConfiguration.driver().bind(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public BundleConfiguration setName(String name) {
        super.setName(name);
        return this;
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type is
     * requested. This method will create a new instance of the extension and return it for all subsequent calls to this
     * method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionType
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()
     */
    // extension() // extendWith(ServiceExtension.class).
    // Mest taenkt hvis vi faa hurtig metoder for attributes.
    // a.la. cc.with(
    public <T extends Extension> T use(Class<T> extensionType) {
        return context.bundleUse(extensionType);
    }

    /**
     * Returns the default driver for containers.
     * 
     * @return the default driver for containers
     */
    public static ComponentDriver<BundleConfiguration> driver() {
        return DRIVER;
    }
}
