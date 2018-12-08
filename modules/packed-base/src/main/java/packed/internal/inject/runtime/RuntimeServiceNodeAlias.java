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
package packed.internal.inject.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.inject.InstantiationMode;
import app.packed.inject.InjectionSite;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.builder.ServiceBuildNode;

/**
 * The runtime representation of an aliased service which delegates the getInstance() to the aliased node. This type is
 * used for exposed nodes as well as nodes that are imported from other injectors.
 */
public final class RuntimeServiceNodeAlias<T> extends RuntimeServiceNode<T> {

    /** The runtime node to delegate to. */
    private final RuntimeServiceNode<T> aliasOf;

    /**
     * Creates a new runtime alias node.
     *
     * @param aliasOf
     *            the build time alias node to create a runtime node from
     */
    public RuntimeServiceNodeAlias(ServiceBuildNode<T> buildNode, ServiceNode<T> aliasOf) {
        super(buildNode);
        this.aliasOf = requireNonNull(aliasOf.toRuntimeNode());
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode getInstantiationMode() {
        return aliasOf.getInstantiationMode();
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
}
