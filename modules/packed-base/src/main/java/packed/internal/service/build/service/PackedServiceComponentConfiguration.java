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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.component.SingletonConfiguration;
import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentPath;
import app.packed.component.FeatureMap;
import app.packed.config.ConfigSite;
import app.packed.container.Extension;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceComponentConfiguration;
import packed.internal.service.build.BuildEntry;

/**
 *
 */
public final class PackedServiceComponentConfiguration<T> implements ServiceComponentConfiguration<T> {

    /** The service we are exposing. */
    public final BuildEntry<T> buildEntry;

    /** The component we are exposing. */
    private final SingletonConfiguration<T> component;

    /**
     * Creates a new configuration object
     * 
     * @param buildEntry
     *            the build entry to wrap
     */
    public PackedServiceComponentConfiguration(SingletonConfiguration<T> component, BuildEntry<T> buildEntry) {
        this.buildEntry = requireNonNull(buildEntry);
        this.component = component;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceComponentConfiguration<T> as(Key<? super T> key) {
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
        return buildEntry.key();
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
    public ComponentPath path() {
        return component.path();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceComponentConfiguration<T> prototype() {
        component.checkConfigurable();
        ((ComponentFactoryBuildEntry<T>) buildEntry).prototype();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceComponentConfiguration<T> setDescription(@Nullable String description) {
        // TODO, vi har kun en description...hvis man er lavet fra en component configuration...
        // Skriver direkte igennem til the underlying component configuration.. Hvis man er lavet via provide...
        component.setDescription(description);
        buildEntry.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceComponentConfiguration<T> setName(String name) {
        component.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Class<? extends Extension>> extension() {
        return component.extension();
    }
}
