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
import app.packed.inject.InjectionExtension;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import app.packed.inject.ServiceConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.run.RSE;
import packed.internal.inject.run.RSEDelegate;

/**
 * A build node representing an exported service.
 */
public final class BSEExported<T> extends BSE<T> {

    /** The node that is exported. */
    @Nullable
    public ServiceEntry<T> exportedEntry;

    /**
     * Exports an entry via its key. Is typically used via {@link InjectionExtension#export(Class)} or
     * {@link InjectionExtension#export(Key)}.
     * 
     * @param builder
     *            the injector configuration this node is being added to
     * @param configSite
     *            the configuration site of the exposure
     */
    public BSEExported(InjectorBuilder builder, ConfigSite configSite, Key<T> key) {
        super(builder, configSite, List.of());
        as(key);
    }

    @SuppressWarnings("unchecked")
    public BSEExported(InjectorBuilder builder, ConfigSite configSite, ServiceEntry<?> existingNode) {
        super(builder, configSite, List.of());
        this.exportedEntry = (ServiceEntry<T>) existingNode;

        // Export of export, of export????
        // Hvad hvis en eller anden aendrer en key midt i chainen.
        // Slaar det igennem i hele vejen ned.
        this.key = (Key<T>) existingNode.key();
    }

    @Override
    @Nullable
    public BSE<?> declaringNode() {
        // Skal vi ikke returnere exposureOf?? istedet for .declaringNode
        return (exportedEntry instanceof BSE) ? ((BSE<?>) exportedEntry).declaringNode() : null;
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return exportedEntry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return null;// ???
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
    RSE<T> newRuntimeNode() {
        return new RSEDelegate<>(this, exportedEntry);
    }

    ServiceConfiguration<T> toServiceConfiguration() {
        return new ExportedServiceConfiguration<>(injectorBuilder.pcc, this);
    }

    /**
     * An instance of {@link ServiceConfiguration} that is returned to the user, for example, when invoking
     * {@link InjectionExtension#export(Class)}.
     */

    // We should use injectorExtension.checkConfigurable
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
        @Nullable
        public Key<?> getKey() {
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
