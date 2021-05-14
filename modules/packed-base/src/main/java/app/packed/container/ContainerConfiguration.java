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

import java.util.Set;

import app.packed.base.NamespacePath;
import app.packed.component.Assembly;
import app.packed.component.BeanConfigurationBinder;
import app.packed.component.BeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentMirror;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import packed.internal.component.PackedComponentDriver.ContainerComponentDriver;

/**
 * The configuration of a container. This class is rarely used directly. Instead containers are typically configured by
 * extending {@link ContainerAssembly} or {@link BaseAssembly}.
 */
public class ContainerConfiguration extends AbstractContainerConfiguration {

    /** A driver for configuring containers. */
    private static final ComponentDriver<ContainerConfiguration> DRIVER = new ContainerComponentDriver(null);

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return super.extensions();
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration install(Class<?> implementation) {
        return super.install(implementation);
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        return super.install(implementation, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration install(Factory<?> factory) {
        return super.install(factory);
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        return super.install(factory, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration installInstance(Object instance) {
        return super.installInstance(instance);
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        return super.installInstance(instance, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror link(Assembly<?> assembly, Wirelet... wirelets) {
        return super.link(assembly, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerConfiguration named(String name) {
        super.named(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public NamespacePath path() {
        return super.path();
    }

    /** {@inheritDoc} */
    @Override
    public BeanConfiguration stateless(Class<?> implementation) {
        return super.wire(BeanConfigurationBinder.driverStateless(implementation));
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionClass) {
        return super.use(extensionClass);
    }

    /** {@inheritDoc} */
    @Override
    public <C extends ComponentConfiguration> C wire(ComponentDriver<C> driver, Wirelet... wirelets) {
        return super.wire(driver, wirelets);
    }

    /**
     * Returns a driver for creating new containers.
     * 
     * @return a driver for creating new containers
     */
    public static ComponentDriver<ContainerConfiguration> driver() {
        return DRIVER;
    }
}
