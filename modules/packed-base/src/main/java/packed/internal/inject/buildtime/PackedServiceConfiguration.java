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

import static java.util.Objects.requireNonNull;

import app.packed.config.ConfigSite;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;

/**
 *
 */
class PackedServiceConfiguration<T> implements ServiceConfiguration<T> {

    private final PackedContainerConfiguration containerConfiguration;

    final BuildServiceNode<T> node;

    /**
     * @param node
     */
    public PackedServiceConfiguration(PackedContainerConfiguration containerConfiguration, BuildServiceNode<T> node) {
        this.containerConfiguration = requireNonNull(containerConfiguration);
        this.node = requireNonNull(node);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> as(@Nullable Key<? super T> key) {
        containerConfiguration.checkConfigurable();
        node.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return node.configSite();
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        return node.description;
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable Key<?> getKey() {
        return node.getKey();
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return node.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceConfiguration<T> setDescription(String description) {
        requireNonNull(description, "description is null");
        containerConfiguration.checkConfigurable();
        node.description = description;
        return this;
    }
}
