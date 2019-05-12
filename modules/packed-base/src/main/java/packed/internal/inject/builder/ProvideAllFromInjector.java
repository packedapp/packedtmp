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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import app.packed.bundle.WiringOption;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.util.Key;
import packed.internal.annotations.AtProvides;
import packed.internal.bundle.AppPackedBundleSupport;
import packed.internal.classscan.ImportExportDescriptor;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceWiringImportOperation;
import packed.internal.inject.runtime.InternalInjector;

/**
 *
 */
public class ProvideAllFromInjector extends ProvideAll {

    final Injector injector;

    /** The configuration of the injector that binding another bundle or injector. */
    final ContainerBuilder injectorConfiguration;

    public ProvideAllFromInjector(InternalConfigurationSite configurationSite, Injector injector, ContainerBuilder injectorConfiguration,
            WiringOption... operations) {
        super(configurationSite, operations);
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.injector = requireNonNull(injector, "injector is null");
    }

    void importServices() {
        InternalInjector ii = (InternalInjector) injector;

        // All the nodes we potentially want to import
        List<ServiceNode<?>> allNodes = ii.copyNodes();// immutable
        processImport(allNodes);
    }

    /**
     * @param importableNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    void processImport(List<? extends ServiceNode<?>> importableNodes) {
        HashMap<Key<?>, ServiceBuildNode_FromInjector<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : importableNodes) {
            if (!node.isPrivate()) {
                ServiceBuildNode_FromInjector<?> n = new ServiceBuildNode_FromInjector<>(injectorConfiguration,
                        configurationSite.replaceParent(node.configurationSite()), this, node);
                nodes.put(node.key(), n);
            }
        }
        // Process each stage
        for (WiringOption operation : options) {
            if (operation instanceof WiringOption) {
                AppPackedBundleSupport.invoke().startWireOperation(operation);
                nodes = processImportStage(operation, nodes);
                AppPackedBundleSupport.invoke().finishWireOperation(operation);
            }
        }

        // Add all to the private node map
        for (ServiceBuildNode_FromInjector<?> node : nodes.values()) {
            if (!injectorConfiguration.box.services().nodes.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, ServiceBuildNode_FromInjector<?>> processImportStage(WiringOption stage, HashMap<Key<?>, ServiceBuildNode_FromInjector<?>> nodes) {
        ImportExportDescriptor ied = ImportExportDescriptor.from(AppPackedBundleSupport.invoke().lookupFromWireOperation(stage), stage.getClass());

        for (AtProvides m : ied.provides.members.values()) {
            for (InternalDependencyDescriptor s : m.dependencies) {
                if (!nodes.containsKey(s.key())) {
                    throw new InjectionException("not good man, " + s.key() + " is not in the set of incoming services");
                }
            }
        }

        // Make runtime nodes....

        HashMap<Key<?>, ServiceBuildNode_FromInjector<?>> newNodes = new HashMap<>();

        for (Iterator<ServiceBuildNode_FromInjector<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            ServiceBuildNode_FromInjector<?> node = iterator.next();
            Key<?> existing = node.key();

            // invoke the import function on the stage
            if (stage instanceof ServiceWiringImportOperation) {
                ((ServiceWiringImportOperation) stage).onEachService(node);
            }

            if (node.key() == null) {
                iterator.remove();
            } else if (!node.key().equals(existing)) {
                iterator.remove();
                // TODO check if a node is already present
                newNodes.put(node.key(), node); // Should make new, with new configuration site
            }
        }
        // Put all remaining nodes in newNodes;
        newNodes.putAll(nodes);
        return newNodes;
    }
}
