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
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentDriver;
import app.packed.component.ComponentDriver.Option;
import packed.internal.component.ComponentNodeConfiguration;

/** Implementation of {@link ContainerConfiguration}. */
final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    /** A component driver that create instances of this configuration class. */
    static final ComponentDriver<ContainerConfiguration> DRIVER = ComponentDriver.of(MethodHandles.lookup(), PackedContainerConfiguration.class,
            Option.container());

    /**
     * Creates a new PackedContainerConfiguration, only used by {@link #DRIVER}.
     *
     * @param context
     *            the component configuration context
     */
    private PackedContainerConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return context.containerExtensions();
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        ((ComponentNodeConfiguration) super.context).realm.lookup(lookup);
    }

    /** {@inheritDoc} */
    @Override
    public PackedContainerConfiguration setName(String name) {
        super.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Extension> T use(Class<T> extensionType) {
        return context.containerUse(extensionType);
    }
}
