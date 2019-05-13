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

import app.packed.container.ComponentConfiguration;
import app.packed.inject.Factory;
import app.packed.inject.InstantiationMode;
import app.packed.util.Nullable;
import packed.internal.config.site.ConfigurationSiteType;

/**
 *
 */
public class DefaultComponentConfiguration extends AbstractFreezableNode implements ComponentConfiguration {

    public final ComponentBuildNode node;

    public DefaultComponentConfiguration(ComponentBuildNode node) {
        this.node = requireNonNull(node);
    }

    public DefaultComponentConfiguration(DefaultContainerConfiguration dcc, Object instance) {
        super(dcc.configurationSite().spawnStack(ConfigurationSiteType.COMPONENT_INSTALL));
        requireNonNull(instance, "instance is null");
        this.node = new ComponentBuildNode(dcc);
    }

    public DefaultComponentConfiguration(DefaultContainerConfiguration dcc, Factory<?> factory, InstantiationMode instantiationMode) {
        super(dcc.configurationSite().spawnStack(ConfigurationSiteType.COMPONENT_INSTALL));
        this.node = new ComponentBuildNode(dcc);
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

    @Override
    protected void onFreeze() {
        node.onFreeze();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setDescription(@Nullable String description) {
        checkConfigurable();
        node.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration setName(@Nullable String name) {
        checkConfigurable();
        node.name = name;
        return this;
    }
}
