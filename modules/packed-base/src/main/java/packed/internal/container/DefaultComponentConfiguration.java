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

import static java.util.Objects.requireNonNull;

import app.packed.artifact.ArtifactInstantiationContext;
import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import packed.internal.container.model.ComponentModel;

/**
 *
 */
public abstract class DefaultComponentConfiguration extends AbstractComponentConfiguration implements ComponentConfiguration {

    final ComponentModel ccd;

    public DefaultComponentConfiguration(ConfigSite site, PackedContainerConfiguration containerConfiguration, ComponentModel ccd) {
        super(site, containerConfiguration);
        this.ccd = requireNonNull(ccd);
    }

    /** {@inheritDoc} */
    @Override
    public DefaultComponentConfiguration setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DefaultComponentConfiguration setName(String name) {
        super.setName(name);
        return this;
    }

    @Override
    public AbstractComponent instantiate(AbstractComponent parent, ArtifactInstantiationContext ic) {
        return new DefaultComponent(parent, this, ic);
    }
}
