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
import app.packed.inject.ExportedServiceConfiguration;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;

/**
 *
 */
public class DefaultExportedServiceConfiguration<T> implements ExportedServiceConfiguration<T> {

    final BuildtimeServiceNode<T> node;

    private final DefaultContainerConfiguration dcc;
    /** The configuration site of this object. */
    private final InternalConfigurationSite configurationSite;

    /**
     * @param node
     */
    public DefaultExportedServiceConfiguration(DefaultContainerConfiguration dcc, BuildtimeServiceNode<T> node) {
        this.configurationSite = node.configurationSite;
        this.dcc = requireNonNull(dcc);
        this.node = requireNonNull(node);
    }

    /** {@inheritDoc} */
    @Override
    public ExportedServiceConfiguration<T> as(Key<? super T> key) {
        dcc.checkConfigurable();
        node.as(key);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public String getDescription() {
        dcc.checkConfigurable();
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
    public ExportedServiceConfiguration<T> setDescription(@Nullable String description) {
        dcc.checkConfigurable();
        node.description = description;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configurationSite() {
        return configurationSite;
    }

}
