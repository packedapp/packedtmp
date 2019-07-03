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

import app.packed.component.ComponentConfiguration;
import app.packed.container.InstantiationContext;
import packed.internal.componentcache.ComponentClassDescriptor;
import packed.internal.config.site.InternalConfigSite;

/**
 *
 */
public abstract class DefaultComponentConfiguration extends AbstractComponentConfiguration implements ComponentConfiguration {

    final ComponentClassDescriptor ccd;

    public DefaultComponentConfiguration(InternalConfigSite site, PackedContainerConfiguration containerConfiguration, ComponentClassDescriptor ccd) {
        super(site, containerConfiguration);
        this.ccd = ccd;
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
    public AbstractComponent instantiate(AbstractComponent parent, InstantiationContext ic) {
        return new DefaultComponent(parent, this, ic);
    }
}
