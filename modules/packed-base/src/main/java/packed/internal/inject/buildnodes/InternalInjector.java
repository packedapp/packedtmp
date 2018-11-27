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

import java.util.Set;
import java.util.stream.Stream;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.CommonKeys;
import packed.internal.inject.Node;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link Injector}. */
public final class InternalInjector extends AbstractInjector {

    /** The configuration site of this injector. */
    private final InternalConfigurationSite configurationSite;

    /** An optional description of the injector. */
    @Nullable
    private final String description;

    /** A map of all services. */
    public final NodeMap nodes;

    /** This injector's parent, or null if this is a top-level injector. */
    @Nullable
    final AbstractInjector parent;

    /** This injector's tags. */
    private final Set<String> tags;

    public InternalInjector(InternalInjectorConfiguration injectorConfiguration, NodeMap nodes) {
        this.parent = null;
        this.nodes = requireNonNull(nodes);
        this.tags = injectorConfiguration.immutableCopyOfTags();
        this.description = injectorConfiguration.getDescription();
        this.configurationSite = injectorConfiguration.getConfigurationSite();
    }

    @Override
    protected void failedGet(Key<?> key) {
        // Oehhh hvad med internal injector, skal vi have en reference til den.
        // Vi kan jo saadan set GC'en den??!?!?!?
        for (Node<?> n : nodes.nodes.values()) {
            System.out.println(n);
            // if (n instanceof RuntimeNode<T>)
        }
        super.failedGet(key);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    protected <T> Node<T> findNode(Key<T> key) {
        return nodes.getRecursive(key);
    }

    /** {@inheritDoc} */
    @Override
    public InternalConfigurationSite getConfigurationSite() {
        return configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return description;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Stream<ServiceDescriptor> services() {
        return (Stream) nodes.nodes.values().stream().filter(e -> !e.getKey().equals(CommonKeys.INJECTOR_KEY));
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return tags;
    }
}
