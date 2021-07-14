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
import app.packed.bean.BaseBeanConfiguration;
import app.packed.component.Wirelet;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.container.PackedContainerDriver;

/**
 * The configuration of a container. This class is rarely referenced directly. Instead containers are typically
 * configured by extending {@link CommonContainerAssembly} or {@link BaseAssembly}.
 */
public class BaseContainerConfiguration extends ContainerConfiguration {

    /** A driver for configuring containers. */
    private static final ContainerDriver<BaseContainerConfiguration> DRIVER = new PackedContainerDriver<>(null);

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return super.extensions();
    }

    @Override
    public BaseBeanConfiguration install(Class<?> implementation) {
        return super.install(implementation);
    }

    @Override
    public BaseBeanConfiguration install(Class<?> implementation, Wirelet... wirelets) {
        return super.install(implementation, wirelets);
    }

    @Override
    public BaseBeanConfiguration install(Factory<?> factory) {
        return super.install(factory);
    }

    @Override
    public BaseBeanConfiguration install(Factory<?> factory, Wirelet... wirelets) {
        return super.install(factory, wirelets);
    }

    @Override
    public BaseBeanConfiguration installInstance(Object instance) {
        return super.installInstance(instance);
    }

    @Override
    public BaseBeanConfiguration installInstance(Object instance, Wirelet... wirelets) {
        return super.installInstance(instance, wirelets);
    }

    /** {@inheritDoc} */
    @Override
    public ContainerMirror link(ContainerAssembly<?> assembly, Wirelet... wirelets) {
        return super.link(assembly, wirelets);
    }

    @Override
    public ContainerMirror mirror() {
        return super.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public BaseContainerConfiguration named(String name) {
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
    public <T extends Extension> T use(Class<T> extensionType) {
        return super.use(extensionType);
    }

    /**
     * Returns a driver for creating new containers.
     * 
     * @return a driver for creating new containers
     */
    public static ContainerDriver<BaseContainerConfiguration> driver() {
        return DRIVER;
    }
}
