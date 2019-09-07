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
package packed.internal.inject.build.service;

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.config.ConfigSite;
import app.packed.container.extension.feature.FeatureMap;
import app.packed.inject.ComponentServiceConfiguration;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.build.BuildEntry;

/**
 *
 */
public final class PackedProvidedComponentConfiguration<T> implements ComponentServiceConfiguration<T> {

    /** The service we are exposing. */
    public final BuildEntry<T> buildEntry;

    /** The component we are exposing. */
    private final ComponentConfiguration component;

    /**
     * Creates a new configuration object
     * 
     * @param buildEntry
     *            the build entry to wrap
     */
    public PackedProvidedComponentConfiguration(ComponentConfiguration component, BuildEntry<T> buildEntry) {
        this.buildEntry = requireNonNull(buildEntry);
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> as(Key<? super T> key) {
        component.checkConfigurable();
        buildEntry.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void checkConfigurable() {
        component.checkConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return buildEntry.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public FeatureMap features() {
        return component.features();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return buildEntry.description;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public Key<?> getKey() {
        return buildEntry.getKey();
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
        return buildEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> lazy() {
        component.checkConfigurable();
        ((ComponentBuildEntry<T>) buildEntry).lazy();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return component.path();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> prototype() {
        component.checkConfigurable();
        ((ComponentBuildEntry<T>) buildEntry).prototype();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> setDescription(@Nullable String description) {
        // TODO, vi har kun en description...hvis man er lavet fra en component configuration...
        // Skriver direkte igennem til the underlying component configuration.. Hvis man er lavet via provide...
        component.setDescription(description);
        buildEntry.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentServiceConfiguration<T> setName(String name) {
        component.setName(name);
        return this;
    }
}
