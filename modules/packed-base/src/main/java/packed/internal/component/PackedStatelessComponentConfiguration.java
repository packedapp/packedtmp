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
package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import packed.internal.artifact.PackedArtifactInstantiationContext;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
public final class PackedStatelessComponentConfiguration<T> extends AbstractComponentConfiguration implements ComponentConfiguration<T> {

    final ComponentModel componentModel;
    /** The static implementation. */
    public final Class<T> implementation;

    public PackedStatelessComponentConfiguration(ConfigSite configSite, PackedContainerConfiguration pcc, ComponentModel componentModel,
            Class<T> implementation) {
        super(configSite, pcc);
        this.componentModel = requireNonNull(componentModel);
        this.implementation = requireNonNull(implementation);
    }

    @Override
    protected String initializeNameDefaultName() {
        return componentModel.defaultPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext paic) {
        return new PackedComponent(parent, this, paic);
    }

    public PackedStatelessComponentConfiguration<T> runHooks(ContainerSource source) {
        componentModel.invokeOnHookOnInstall(source, this);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfiguration<T> setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfiguration<T> setName(String name) {
        super.setName(name);
        return this;
    }
}
