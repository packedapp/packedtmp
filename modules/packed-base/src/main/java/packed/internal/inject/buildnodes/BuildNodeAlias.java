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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.inject.Node;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeAlias;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** An alias build node. */
public final class BuildNodeAlias<T> extends BuildNode<T> {

    /** The node that this node is an alias of. */
    private final Node<T> aliasOf;

    /**
     * Creates a new build alias node.
     *
     * @param builder
     *            the injector builder
     * @param aliasOf
     *            the node that his is an alias for. Can be a build node, or a runtime node if the node was found in a
     *            parent injector
     * @param original
     *            the class or instance used for aliasing the method.
     */
    @Deprecated // Logikken omkring resolving bliver lidt lettere uden den.
    public BuildNodeAlias(InternalInjectorConfiguration bundle, InternalConfigurationSite configurationSite, Node<T> aliasOf) {
        super(bundle, configurationSite, List.of());
        this.aliasOf = requireNonNull(aliasOf);
        setDescription(aliasOf.getDescription());// we copy the descriptor
    }

    /** {@inheritDoc} */
    @Override
    public BindingMode getBindingMode() {
        return aliasOf.getBindingMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(InjectionSite site) {
        return aliasOf.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return aliasOf.needsInjectionSite();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsResolving() {
        return aliasOf.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeNode<T> newRuntimeNode() {
        return new RuntimeNodeAlias<>(this, aliasOf);
    }

    @Override
    BuildNode<?> declaringNode() {
        return aliasOf instanceof BuildNode ? (BuildNode<?>) aliasOf : null;
    }
}