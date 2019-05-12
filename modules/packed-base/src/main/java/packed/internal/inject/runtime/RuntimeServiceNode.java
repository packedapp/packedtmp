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

import java.util.Optional;

import app.packed.config.ConfigSite;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.buildtime.BuildtimeServiceNode;

/** A node that represents a service at runtime. */
public abstract class RuntimeServiceNode<T> implements ServiceNode<T> {

    /** The point where this node was registered. */
    private final ConfigSite configurationSite;

    /** An (optionally) description of the service. */
    @Nullable
    private final String description;

    /** The key under which the service is available. */
    private final Key<T> key;

    // /** Any tags that might be present on the service. */
    // private final Set<String> tags;

    /**
     * Creates a new runtime node from a build node.
     *
     * @param node
     *            the build node to create the runtime node from
     */
    RuntimeServiceNode(BuildtimeServiceNode<T> node) {
        this.configurationSite = requireNonNull(node.configurationSite());
        this.description = node.getDescription();
        this.key = requireNonNull(node.key());
        // this.tags = node.immutableCopyOfTags();
    }

    @Override
    public abstract InstantiationMode instantiationMode();

    /** {@inheritDoc} */
    @Override
    public final ConfigSite configurationSite() {
        return configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    public final Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public final String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @Override
    public final Key<T> key() {
        return key;
    }

    // Ideen er at vi kan komme med forslag til andre noegler end den forespurgte
    // F.eks. man eftersporger Foo.class, men maaske er der en FooImpl et sted
    public boolean isAssignableTo(Class<?> type) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean needsResolving() {
        return false;
    }
    //
    // /** {@inheritDoc} */
    // @Override
    // public final Set<String> tags() {
    // return tags;
    // }

    /** {@inheritDoc} */
    @Override
    public final RuntimeServiceNode<T> toRuntimeNode() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(key());
        sb.append("[").append(instantiationMode()).append(']');
        if (description != null) {
            sb.append(":").append(description);

        }
        return sb.toString();
    }
}
