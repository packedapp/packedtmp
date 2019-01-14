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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.InjectionSite;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNodeAlias;

/** A build node that imports a service from another injector. */
public class ServiceBuildNodeExport<T> extends ServiceBuildNode<T> {

    /** The node to import. */
    final ServiceNode<T> other;

    /** The bind injector source. */
    final AbstractWiring source;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    ServiceBuildNodeExport(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, AbstractWiring source, ServiceNode<T> node) {
        super(injectorConfiguration, configurationSite, List.of());
        this.other = requireNonNull(node);
        this.source = requireNonNull(source);
        this.as((Key) node.getKey());
        this.setDescription(node.getDescription());
        this.tags().addAll(node.tags());
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode getInstantiationMode() {
        return other.getInstantiationMode();
    }

    @Override
    @Nullable
    ServiceBuildNode<?> declaringNode() {
        return (other instanceof ServiceBuildNode) ? ((ServiceBuildNode<?>) other).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return other.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return other.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return other.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    RuntimeServiceNode<T> newRuntimeNode() {
        return new RuntimeServiceNodeAlias<T>(this, other);
    }
}
