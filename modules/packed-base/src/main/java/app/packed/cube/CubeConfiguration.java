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
package app.packed.cube;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentDriver.Option;
import app.packed.inject.Factory;
import app.packed.component.StatelessConfiguration;
import packed.internal.component.ComponentBuild;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured by
 * extending {@link CubeBundle} or {@link BaseBundle}.
 */
public final class CubeConfiguration extends ComponentConfiguration {

    /** A driver that create container components. */
    private static final ComponentDriver<CubeConfiguration> DRIVER = ComponentDriver.of(MethodHandles.lookup(), CubeConfiguration.class,
            Option.container());

    /**
     * Creates a new PackedContainerConfiguration, only used by {@link #DRIVER}.
     *
     * @param context
     *            the component configuration context
     */
    private CubeConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    /**
     * Returns an unmodifiable view of the extensions that are currently used.
     * 
     * @return an unmodifiable view of the extensions that are currently used
     * 
     * @see #use(Class)
     * @see CubeBundle#extensions()
     */
    public Set<Class<? extends Extension>> extensions() {
        return context.cubeExtensions();
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
        return wire(BeanConfiguration.driver(), Factory.of(implementation));
    }

    /**
     * Installs a component that will use the specified {@link Factory} to instantiate the component instance.
     * 
     * @param <T>
     *            the type of the component
     * @param factory
     *            the factory to install
     * @return the configuration of the component
     * @see CubeBundle#install(Factory)
     */
    public <T> BeanConfiguration<T> install(Factory<T> factory) {
        return wire(BeanConfiguration.driver(), factory);
    }

    /**
     * @param <T>
     *            the type of the component
     * @param instance
     *            the instance to install
     * @return the configuration of the component
     * @see CubeBundle#installInstance(Object)
     */
    public <T> BeanConfiguration<T> installInstance(T instance) {
        return wireInstance(BeanConfiguration.driver(), instance);
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
        return wire(StatelessConfiguration.driver(), implementation);
    }

    /**
     * Registers a {@link Lookup} object that will be used for accessing members on components that are registered with the
     * container.
     * <p>
     * Lookup objects passed to this method are never made directly available to extensions. Instead the lookup is used to
     * create {@link MethodHandle} and {@link VarHandle} that are passed along to extensions.
     * <p>
     * This method allows passing null, which clears any lookup object that has previously been set. This is useful if allow
     * 
     * 
     * @param lookup
     *            the lookup object
     */
    // If you are creating resulable stuff, you should remember to null the lookup object out.
    // So child modules do not have the power of the lookup object.

    public void lookup(@Nullable Lookup lookup) {
        ((ComponentBuild) super.context).realm.lookup(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public CubeConfiguration setName(String name) {
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
        return context.cubeUse(extensionType);
    }

    /**
     * Returns the default driver for containers.
     * 
     * @return the default driver for containers
     */
    public static ComponentDriver<CubeConfiguration> driver() {
        return DRIVER;
    }
}
