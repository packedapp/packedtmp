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
import java.util.Map;

import app.packed.component.ComponentConfiguration;
import app.packed.config.ConfigSite;
import app.packed.service.InstantiationMode;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.run.InjectorEntry;
import packed.internal.service.run.SingletonInjectorEntry;

/**
 *
 */
public final class ComponentInstanceBuildEntry<T> extends AbstractComponentBuildEntry<T> {

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
    public boolean hasUnresolvedDependencies() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return InstantiationMode.SINGLETON;
    }

    /** {@inheritDoc} */
    @Override
    protected InjectorEntry<T> newRuntimeNode(Map<BuildEntry<?>, InjectorEntry<?>> entries) {
        return new SingletonInjectorEntry<>(this, instance);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
