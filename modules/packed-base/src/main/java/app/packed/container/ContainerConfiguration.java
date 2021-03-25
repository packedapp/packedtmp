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

import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentModifier;
import app.packed.component.ComponentModifierSet;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import packed.internal.component.OldPackedComponentDriver;
import packed.internal.component.PackedComponentDriver;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured by
 * extending {@link ContainerAssembly} or {@link BaseAssembly}.
 */
public class ContainerConfiguration extends BaseComponentConfiguration {

    /** A driver for configuring containers. */
    private static final ComponentDriver<ContainerConfiguration> DRIVER = OldPackedComponentDriver.of(MethodHandles.lookup(), ContainerConfiguration.class,
            OldPackedComponentDriver.Option.container());

    private static final ComponentDriver<ContainerConfiguration> DRIVER_NEW = new ContainerComponentDriver(null);

    
    /**
     * Creates a new ContainerConfiguration, only used by {@link #DRIVER}.
     *
     * @param context
     *            the component configuration context
     */
    public ContainerConfiguration(ComponentConfigurationContext context) {
        super(context);
    }
    
    /**
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see ContainerAssembly#extensions()
     */
    public Set<Class<? extends Extension>> extensions() {
        // getAttribute(EXTENSIONS);
        return context.containerExtensions();
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * <p>
     * Invoking this method is equivalent to invoking {@code install(Factory.findInjectable(implementation))}.
     * 
     * @param implementation
     *            the type of instantiate and use as the component instance
     * @return the configuration of the component
     */
    public BaseComponentConfiguration install(Class<?> implementation) {
        return context.wire(ComponentDriver.driverInstall(implementation));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see ContainerAssembly#install(Factory)
     */
    public BaseComponentConfiguration install(Factory<?> factory) {
        return context.wire(ComponentDriver.driverInstall(factory));
    }

    /**
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see ContainerAssembly#installInstance(Object)
     */
    public BaseComponentConfiguration installInstance(Object instance) {
        return context.wire(ComponentDriver.driverInstallInstance(instance));
    }

    /**
     * Links the specified assembly with this container as its parent.
     * 
     * @param assembly
     *            the assembly to link
     * @param wirelets
     *            optional wirelets
     * @return the component that was linked
     */
    public Component link(Assembly<?> assembly, Wirelet... wirelets) {
        return context.link(assembly, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration setName(String name) {
        context.setName(name);
        return this;
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
    public BaseComponentConfiguration stateless(Class<?> implementation) {
        return context.wire(ComponentDriver.driverStateless(implementation));
    }

    /**
     * Returns an extension of the specified type. If this is the first time an extension of the specified type has been
     * requested. This method will create a new instance of the extension. This instance will be returned for all subsequent
     * calls to this method with the same extension type.
     * 
     * @param <T>
     *            the type of extension to return
     * @param extensionClass
     *            the type of extension to return
     * @return an extension of the specified type
     * @throws IllegalStateException
     *             if this configuration is no longer configurable and an extension of the specified type has not already
     *             been installed
     * @see #extensions()
     */
    public <T extends Extension> T use(Class<T> extensionClass) {
        return context.containerUse(extensionClass);
    }

    /**
     * Wires a new child component using the specified component driver and optional wirelets.
     * 
     * @param <C>
     *            the type of configuration returned by the specified driver
     * @param driver
     *            the driver to use for creating the component
     * @param wirelets
     *            any wirelets that should be used when creating the component
     * @return a configuration for the new child component
     */
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return context.wire(driver, wirelets);
    }

    /**
     * Returns a driver for creating new containers.
     * 
     * @return a driver for creating new containers
     */
    public static ComponentDriver<ContainerConfiguration> driver() {
        return DRIVER;
    }

    /** A component driver that create containers. */
    private static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        private static final ComponentModifierSet CONTAINER_MODIFIERS = ComponentModifierSet.of(ComponentModifier.CONTAINER);

        private ContainerComponentDriver(Wirelet wirelet) {
            super(wirelet);
        }

        @Override
        public ComponentDriver<ContainerConfiguration> bind(Object object) {
            throw new UnsupportedOperationException("Cannot bind to a container component driver");
        }

        @Override
        public ComponentModifierSet modifiers() {
            return CONTAINER_MODIFIERS;
        }

        @Override
        public ContainerConfiguration toConfiguration(ComponentConfigurationContext context) {
            return new ContainerConfiguration(context);
        }

        @Override
        protected ComponentDriver<ContainerConfiguration> withWirelet(Wirelet w) {
            return new ContainerComponentDriver(w);
        }
    }
}
