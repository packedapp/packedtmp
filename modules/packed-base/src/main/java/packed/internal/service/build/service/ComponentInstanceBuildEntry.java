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

import java.util.List;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import app.packed.service.PrototypeRequest;
import app.packed.util.Nullable;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.service.run.SingletonRuntimeEntry;

/**
 *
 */
public class ComponentInstanceBuildEntry<T> extends AbstractComponentBuildEntry<T> {

    /** The singleton instance. */
    private final T instance;

    /**
     * Creates a new node from an instance.
     * 
     * @param ib
     *            the injector builder
     * @param configSite
     *            the configuration site
     * @param instance
     *            the instance
     */
    public ComponentInstanceBuildEntry(ServiceExtensionNode ib, ConfigSite configSite, ComponentConfiguration<T> cc, T instance) {
        super(ib, configSite, List.of(), null, cc);
        this.instance = requireNonNull(instance, "instance is null");
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(@Nullable PrototypeRequest request) {
        return instance;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode() {
        return new SingletonRuntimeEntry<>(this, instance);
    }

}
