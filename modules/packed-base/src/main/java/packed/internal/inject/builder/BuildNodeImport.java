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

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.Node;
import packed.internal.inject.runtime.RuntimeServiceNode;
import packed.internal.inject.runtime.RuntimeServiceNodeAlias;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** A build node that imports a service from another injector. */
public class BuildNodeImport<T> extends BuildNode<T> {

    /** The node to import. */
    final Node<T> other;

    /** The bind injector source. */
    final BindInjector binding;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    BuildNodeImport(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, BindInjector binding, Node<T> node) {
        super(injectorConfiguration, configurationSite, List.of());
        this.other = requireNonNull(node);
        this.binding = requireNonNull(binding);
        this.as((Key) node.getKey());
        this.setDescription(node.getDescription());
        this.tags().addAll(node.tags());
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return other.getBindingMode();
    }

    @Override
    @Nullable
    BuildNode<?> declaringNode() {
        return (other instanceof BuildNode) ? ((BuildNode<?>) other).declaringNode() : null;
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
