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

import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;

/**
 *
 */
class DefaultServiceConfiguration<T> extends AbstractFreezableNode implements ServiceConfiguration<T> {

    /** The component we are exposing. */
    private final ComponentBuildNode component;

    /** The service we are exposing. */
    private final BuildtimeServiceNode<T> service;

    /**
     * @param service
     */
    DefaultServiceConfiguration(ComponentBuildNode component, BuildtimeServiceNode<T> service) {
        this.service = requireNonNull(service);
        this.component = component;
        component.serviceNode = service;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> as(Key<? super T> key) {
        checkConfigurable();
        service.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        checkConfigurable();
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
        return component.name;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return service.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> lazy() {
        checkConfigurable();
        ((BuildtimeServiceNodeDefault<T>) service).lazy();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected void onFreeze() {
        component.onFreeze();
        service.onFreeze();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> prototype() {
        checkConfigurable();
        ((BuildtimeServiceNodeDefault<T>) service).prototype();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> setDescription(@Nullable String description) {
        checkConfigurable();
        service.description = description;
        component.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> setName(String name) {
        checkConfigurable();
        component.name = name;
        return this;
    }
}
