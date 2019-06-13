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
import app.packed.config.ConfigSite;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.buildtime.BuildtimeServiceNode;

/**
 *
 */
public final class DefaultComponentConfiguration implements ComponentConfiguration {

    /** The configuration of the container that this component has been installed into. */
    final DefaultContainerConfiguration containerConfiguration;

    /** The description of the component */
    @Nullable
    public String description;

    public Object instance;

    /** The name of the component */
    @Nullable
    public String name;

    public BuildtimeServiceNode<?> serviceNode;

    /** The configuration site of the component. */
    public final InternalConfigurationSite site;

    public DefaultComponentConfiguration(InternalConfigurationSite site, DefaultContainerConfiguration containerConfiguration) {
        this.site = requireNonNull(site);
        this.containerConfiguration = requireNonNull(containerConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configurationSite() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return name;
    }

    public void onFreeze() {
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        containerConfiguration.components.put(name, this);
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
}
