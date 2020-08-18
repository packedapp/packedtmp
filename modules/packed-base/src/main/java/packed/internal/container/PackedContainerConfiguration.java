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
package packed.internal.container;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Set;

import app.packed.base.Nullable;
import app.packed.component.AbstractComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import packed.internal.component.ComponentNodeConfiguration;

/** The default implementation of {@link ContainerConfiguration}. */
public final class PackedContainerConfiguration extends AbstractComponentConfiguration implements ContainerConfiguration {

    public PackedContainerConfiguration(ComponentConfigurationContext node) {
        super(node);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Class<? extends Extension>> extensions() {
        return context.containerExtensions();
    }

    /** {@inheritDoc} */
    @Override
    public void lookup(@Nullable Lookup lookup) {
        node().realm().lookup(lookup);
    }

    private ComponentNodeConfiguration node() {
        return (ComponentNodeConfiguration) super.context;
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
