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

import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.feature.FeatureMap;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.DefaultComponentConfiguration;

/**
 *
 */
public final class PackedProvidedComponentConfiguration<T> implements ProvidedComponentConfiguration<T> {

    /** The component we are exposing. */
    private final DefaultComponentConfiguration component;

    /** The service we are exposing. */
    private final BuildServiceNode<T> buildNode;

    /**
     * @param buildNode
     */
    public PackedProvidedComponentConfiguration(DefaultComponentConfiguration component, BuildServiceNode<T> buildNode) {
        this.buildNode = requireNonNull(buildNode);
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> as(Key<? super T> key) {
        component.checkConfigurable();
        buildNode.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return buildNode.configSite();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return buildNode.description;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> getKey() {
        return buildNode.getKey();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getName() {
        return component.getName();
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return buildNode.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> lazy() {
        component.checkConfigurable();
        ((BuildServiceNodeDefault<T>) buildNode).lazy();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> prototype() {
        component.checkConfigurable();
        ((BuildServiceNodeDefault<T>) buildNode).prototype();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> setDescription(@Nullable String description) {
        component.setDescription(description);
        buildNode.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> setName(String name) {
        component.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return component.path();
    }

    /** {@inheritDoc} */
    @Override
    public FeatureMap features() {
        return component.features();
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        component.checkConfigurable();
    }
}
