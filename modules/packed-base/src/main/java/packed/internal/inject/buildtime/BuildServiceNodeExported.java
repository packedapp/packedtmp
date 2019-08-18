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

import java.util.List;

import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.AbstractRuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeDelegateServiceNode;

/**
 * A build node representing an exported service.
 */
final class BuildServiceNodeExported<T> extends BuildServiceNode<T> {

    /** The node that is exposed. */
    public ServiceNode<T> exportOf;

    /**
     * @param configuration
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     */
    public BuildServiceNodeExported(InjectorBuilder configuration, InternalConfigSite configSite) {
        super(configuration, configSite, List.of());
    }

    @Override
    @Nullable
    BuildServiceNode<?> declaringNode() {
        // Skal vi ikke returnere exposureOf?? istedet for .declaringNode
        return (exportOf instanceof BuildServiceNode) ? ((BuildServiceNode<?>) exportOf).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return exportOf.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    AbstractRuntimeServiceNode<T> newRuntimeNode() {
        return new RuntimeDelegateServiceNode<>(this, exportOf);
    }
}
