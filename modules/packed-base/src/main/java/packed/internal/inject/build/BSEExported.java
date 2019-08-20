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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.config.ConfigSite;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.run.RSN;
import packed.internal.inject.run.RSNDelegate;

/**
 * A build node representing an exported service.
 */
public final class BSEExported<T> extends BSE<T> {

    /** The node that is exposed. */
    public ServiceEntry<T> exportOf;

    /**
     * @param configuration
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     */
    public BSEExported(InjectorBuilder configuration, InternalConfigSite configSite, Key<T> key) {
        super(configuration, configSite, List.of());
        as(key);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BSEExported(InjectorBuilder configuration, InternalConfigSite configSite, PackedProvidedComponentConfiguration<?> existingNode) {
        super(configuration, configSite, List.of());
        this.exportOf = (ServiceEntry<T>) existingNode.buildNode;
        as((Key) existingNode.getKey());
    }

    @Override
    @Nullable
    public BSE<?> declaringNode() {
        // Skal vi ikke returnere exposureOf?? istedet for .declaringNode
        return (exportOf instanceof BSE) ? ((BSE<?>) exportOf).declaringNode() : null;
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
    RSN<T> newRuntimeNode() {
        return new RSNDelegate<>(this, exportOf);
    }

    ServiceConfiguration<T> toServiceConfiguration() {
        return new ExportedServiceConfiguration<>(injectorBuilder.containerConfiguration, this);
    }

    private static final class ExportedServiceConfiguration<T> implements ServiceConfiguration<T> {

        private final PackedContainerConfiguration containerConfiguration;

        final BSE<T> node;

        /**
         * @param node
         */
        private ExportedServiceConfiguration(PackedContainerConfiguration containerConfiguration, BSEExported<T> node) {
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

}
