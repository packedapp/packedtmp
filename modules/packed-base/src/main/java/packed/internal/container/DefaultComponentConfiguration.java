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

import java.util.UUID;

import app.packed.component.ComponentConfiguration;
import app.packed.util.Nullable;
import packed.internal.componentcache.ComponentClassDescriptor;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.buildtime.BuildtimeServiceNode;

/**
 *
 */
public final class DefaultComponentConfiguration extends AbstractComponentConfiguration implements ComponentConfiguration {

    /** The configuration of the container that this component has been installed into. */
    final DefaultContainerConfiguration containerConfiguration;

    public Object instance;

    public BuildtimeServiceNode<?> serviceNode;

    public DefaultComponentConfiguration(InternalConfigurationSite site, DefaultContainerConfiguration containerConfiguration) {
        super(site, containerConfiguration);
        this.containerConfiguration = requireNonNull(containerConfiguration);
        this.ccd = null;
    }

    private final ComponentClassDescriptor ccd;

    public DefaultComponentConfiguration(InternalConfigurationSite site, DefaultContainerConfiguration containerConfiguration, ComponentClassDescriptor ccd) {
        super(site, containerConfiguration);
        this.containerConfiguration = requireNonNull(containerConfiguration);
        this.ccd = ccd;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return name;
    }

    public void onFreeze() {
        if (name == null && ccd != null) {
            name = ccd.defaultPrefix();
        }
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        containerConfiguration.children.put(name, this);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setDescription(@Nullable String description) {
        containerConfiguration.checkConfigurable();
        this.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setName(@Nullable String name) {
        containerConfiguration.checkConfigurable();
        this.name = name;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected void checkConfigurable() {
        // TODO Auto-generated method stub

    }
}
