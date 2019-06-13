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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.util.Nullable;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.DefaultContainerConfiguration;

/**
 *
 */
public class OldDefaultComponentConfiguration implements ComponentConfiguration {

    final DefaultComponentConfiguration node;

    private final DefaultContainerConfiguration dcc;

    /** The configuration site of this object. */
    private final InternalConfigurationSite configurationSite;

    OldDefaultComponentConfiguration(DefaultContainerConfiguration dcc, DefaultComponentConfiguration node) {
        this.node = requireNonNull(node);
        this.dcc = requireNonNull(dcc);
        this.configurationSite = node.site;
    }

    public OldDefaultComponentConfiguration(DefaultContainerConfiguration dcc, Object instance) {
        this.configurationSite = dcc.configurationSite().thenStack(ConfigurationSiteType.COMPONENT_INSTALL);
        this.dcc = requireNonNull(dcc);
        requireNonNull(instance, "instance is null");
        this.node = new DefaultComponentConfiguration(configurationSite, dcc);
    }

    public OldDefaultComponentConfiguration(DefaultContainerConfiguration dcc, Factory<?> factory, InstantiationMode instantiationMode) {
        this.configurationSite = dcc.configurationSite().thenStack(ConfigurationSiteType.COMPONENT_INSTALL);
        this.dcc = requireNonNull(dcc);
        this.node = new DefaultComponentConfiguration(configurationSite, dcc);
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getDescription() {
        return node.description;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable String getName() {
        return node.name;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setDescription(@Nullable String description) {
        dcc.checkConfigurable();
        node.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setName(@Nullable String name) {
        dcc.checkConfigurable();
        node.name = name;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configurationSite() {
        throw new UnsupportedOperationException();
    }
}
