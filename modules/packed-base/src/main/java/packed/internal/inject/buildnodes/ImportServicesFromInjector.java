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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionSite;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.inject.ServiceImportStage;
import packed.internal.inject.InternalInjector;
import packed.internal.inject.Node;
import packed.internal.inject.runtimenodes.RuntimeNode;
import packed.internal.inject.runtimenodes.RuntimeNodeAlias;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * Represents an imported injector via {@link InjectorConfiguration#importServices(Injector, ServiceImportStage...)}.
 */
public final class ImportServicesFromInjector extends ImportServices {

    /** A map of all services that have been imported. */
    final Map<Key<?>, ImportingNode<?>> importedServices = new HashMap<>();

    /** The injector to import services from. */
    final Injector injector;

    final List<ServiceImportStage> stages;

    /**
     * @param configurationSite
     */
    public ImportServicesFromInjector(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, Injector injector,
            ServiceImportStage[] stages) {
        super(injectorConfiguration, configurationSite);
        this.injector = requireNonNull(injector, "injector is null");
        this.stages = List.of(stages);
    }

    /**
     * 
     */
    public void doStuff() {
        InternalInjector ii = (InternalInjector) injector;

        // All the nodes we potentially want to import
        List<Node<?>> allNodes = List.copyOf(ii.nodes.toAll());// immutable

        ImportingNode<?>[] bns = new ImportingNode[allNodes.size()];
        for (int i = 0; i < bns.length; i++) {
            Node<?> n = allNodes.get(i);
            if (!n.isPrivate()) {
                InternalConfigurationSite cs = configurationSite.replaceParent(n.getConfigurationSite());
                bns[i] = new ImportingNode<>(injectorConfiguration, cs, n);
            }
        }

        for (ServiceImportStage stage : stages) {
            // Find @Provides, lookup class
            for (int i = 0; i < bns.length; i++) {
                ImportingNode<?> bn = bns[i];
                if (bn != null) {
                    Key<?> existing = bn.getKey();
                    stage.process(bn);
                    if (bn.getKey() == null) {
                        bns[i] = null;
                    } else {
                        if (!bn.getKey().equals(existing)) {
                            // Should make new, with new configuration site
                        }
                    }
                }
            }
        }
        for (int i = 0; i < bns.length; i++) {
            if (bns[i] != null) {
                importedServices.put(bns[i].getKey(), bns[i]);
            }
        }
    }

    @Override
    void importInto(InternalInjectorConfiguration configuration) {
        for (ImportingNode<?> n : importedServices.values()) {
            configuration.privateBuildNodeList.add(n);
        }
    }

    public Injector injector() {
        return injector;
    }

    class ImportingNode<T> extends BuildNode<T> {

        final Node<T> other;

        /**
         * @param injectorConfiguration
         * @param configurationSite
         * @param dependencies
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        ImportingNode(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, Node<T> node) {
            super(injectorConfiguration, configurationSite, List.of());
            this.other = requireNonNull(node);
            this.as((Key) node.getKey());
            this.setDescription(node.getDescription());
            this.tags().addAll(node.tags());
        }

        /** {@inheritDoc} */
        @Override
        public BindingMode getBindingMode() {
            return other.getBindingMode();
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
        RuntimeNode<T> newRuntimeNode() {
            return new RuntimeNodeAlias<T>(this, other);
            // return other.toRuntimeNode();
        }

    }
}
