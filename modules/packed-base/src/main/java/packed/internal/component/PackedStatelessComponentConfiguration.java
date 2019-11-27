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

import app.packed.component.StatelessConfiguration;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import packed.internal.artifact.PackedArtifactInstantiationContext;

/**
 *
 */
public final class PackedStatelessComponentConfiguration extends AbstractComponentConfiguration implements StatelessConfiguration {

    private final ComponentModel componentModel;

    public PackedStatelessComponentConfiguration(ConfigSite configSite, AbstractComponentConfiguration parent, ComponentModel componentModel) {
        super(configSite, parent);
        this.componentModel = requireNonNull(componentModel);
    }

    @Override
    protected String initializeNameDefaultName() {
        return componentModel.defaultPrefix();
    }

    /** {@inheritDoc} */
    @Override
    public AbstractComponent instantiate(AbstractComponent parent, PackedArtifactInstantiationContext paic) {
        return new PackedStatelessComponent(parent, this, paic);
    }

    public PackedStatelessComponentConfiguration runHooks(ContainerSource source) {
        componentModel.invokeOnHookOnInstall(source, this);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfiguration setName(String name) {
        super.setName(name);
        return this;
    }
}
