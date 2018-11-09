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
package packed.internal.inject;

import java.util.Set;
import java.util.stream.Stream;

import app.packed.inject.Injector;
import app.packed.inject.Key;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Nullable;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/** The default implementation of {@link Injector}. */
public final class InternalInjector extends AbstractInjector {

    /** The configuration site of the injector. */
    private final InternalConfigurationSite configurationSite;

    /** An optional description of the injector. */
    @Nullable
    private final String description;

    /** All the service configurations of this builder. */
    public final NodeMap nodes;

    /** This injector's parent, or null if this is a top-level injector. */
    private final AbstractInjector parent;

    /** The injector's tags. */
    private final Set<String> tags;

    public InternalInjector(InternalInjectorConfiguration injectorConfiguration) {
        this.parent = injectorConfiguration.parentInjector;
        this.nodes = parent == null ? new NodeMap() : new NodeMap(injectorConfiguration.parentInjector.nodes);
        this.tags = injectorConfiguration.immutableCopyOfTags();
        this.description = injectorConfiguration.getDescription();
        this.configurationSite = injectorConfiguration.configurationSite;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    protected <T> Node<T> findNode(Key<T> key) {
        return nodes.get(key);
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
        return (Stream) nodes.map.values().stream();
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> tags() {
        return tags;
    }
}
