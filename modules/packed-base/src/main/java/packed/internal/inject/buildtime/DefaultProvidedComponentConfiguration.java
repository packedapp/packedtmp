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
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvidedComponentConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.DefaultComponentConfiguration;
import packed.internal.container.DefaultContainerConfiguration;

/**
 *
 */
public final class DefaultProvidedComponentConfiguration<T> implements ProvidedComponentConfiguration<T> {

    /** The component we are exposing. */
    private final DefaultComponentConfiguration component;

    private final DefaultContainerConfiguration dcc;

    /** The service we are exposing. */
    private final BuildtimeServiceNode<T> service;

    /**
     * @param service
     */
    public DefaultProvidedComponentConfiguration(DefaultContainerConfiguration dcc, DefaultComponentConfiguration component, BuildtimeServiceNode<T> service) {
        this.dcc = requireNonNull(dcc);
        this.service = requireNonNull(service);
        this.component = component;
        // component.serviceNode = service;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> as(Key<? super T> key) {
        dcc.checkConfigurable();
        service.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configurationSite() {
        return service.configurationSite();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        dcc.checkConfigurable();
        return service.description;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable Key<?> getKey() {
        return service.getKey();
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
        return service.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> lazy() {
        dcc.checkConfigurable();
        ((BuildtimeServiceNodeDefault<T>) service).lazy();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> prototype() {
        dcc.checkConfigurable();
        ((BuildtimeServiceNodeDefault<T>) service).prototype();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> setDescription(@Nullable String description) {
        dcc.checkConfigurable();
        service.description = description;
        component.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvidedComponentConfiguration<T> setName(String name) {
        dcc.checkConfigurable();
        component.setName(name);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return component.path();
    }
}
