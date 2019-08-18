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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import app.packed.container.Wirelet;
import app.packed.container.WireletList;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.util.Key;
import packed.internal.config.site.ConfigSiteType;
import packed.internal.config.site.InternalConfigSite;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.AbstractInjector;
import packed.internal.inject.ServiceNode;

/** Provides services from an existing Injector. */
final class ImportedInjector {

    /** The injector to import services from. */
    private final AbstractInjector injector;

    /** Any wirelets used when importing the injector. */
    final WireletList wirelets;

    /** The configuration site of the import statement. */
    private final InternalConfigSite configSite;

    /** The injector builder into which the injector is imported. */
    private final InjectorBuilder builder;

    ImportedInjector(PackedContainerConfiguration containerConfiguration, InjectorBuilder ib, Injector injector, Wirelet... wirelets) {
        this.builder = requireNonNull(ib);
        if (!(requireNonNull(injector, "injector is null") instanceof AbstractInjector)) {
            throw new IllegalArgumentException("Custom implementations of Injector are currently not supported, injector type = " + injector.getClass());
        }
        this.injector = (AbstractInjector) injector;
        this.wirelets = WireletList.of(wirelets);
        this.configSite = containerConfiguration.configSite().thenStack(ConfigSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
    }

    void importAll() {
        processImport(injector.copyNodes());
    }

    /**
     * @param externalNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    private void processImport(List<? extends ServiceNode<?>> externalNodes) {

        // First create service build nodes for every existing node
        HashMap<Key<?>, BSN<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : externalNodes) {
            if (!node.isPrivate()) {
                BSNImported<?> n = new BSNImported<>(builder, configSite.replaceParent(node.configSite()), this, node);
                nodes.put(node.key(), n);
            }
        }

        // Process each wirelet
        for (Wirelet operation : wirelets.toList()) {
            if (operation instanceof Wirelet) {
                nodes = processWirelet(operation, nodes);
                throw new Error();
            }
        }

        // Add all to the private node map
        for (BSN<?> node : nodes.values()) {
            if (!builder.resolver.nodes.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, BSN<?>> processWirelet(Wirelet wirelet, HashMap<Key<?>, BSN<?>> nodes) {
        // if (true) {
        // throw new Error();
        // }
        // ImportExportDescriptor ied = ImportExportDescriptor.from(null /*
        // AppPackedBundleSupport.invoke().lookupFromWireOperation(stage) */, stage.getClass());
        //
        // for (AtProvides m : ied.provides.members.values()) {
        // for (InternalDependencyDescriptor s : m.dependencies) {
        // if (!nodes.containsKey(s.key())) {
        // throw new InjectionException("not good man, " + s.key() + " is not in the set of incoming services");
        // }
        // }
        // }

        // Make runtime nodes....

        HashMap<Key<?>, BSN<?>> newNodes = new HashMap<>();

        for (Iterator<BSN<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            BSN<?> node = iterator.next();
            Key<?> existing = node.key();

            // invoke the import function on the stage
            // if (stage instanceof InternalServiceWirelets) {
            // ((InternalServiceWirelets) stage).onEachService(node);
            // }

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
