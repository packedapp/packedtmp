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
public final class InjectorImporter {

    /** The injector we are providing services from. */
    private final Injector injector;

    /** The wiring options used when creating this configuration. */
    final WireletList wirelets;

    /** The configuration site of this object. */
    private final InternalConfigSite configSite;

    private final InjectorBuilder ib;

    public InjectorImporter(PackedContainerConfiguration containerConfiguration, InjectorBuilder ib, Injector injector, Wirelet... wirelets) {
        this.ib = requireNonNull(ib);
        this.injector = requireNonNull(injector, "injector is null");
        this.wirelets = WireletList.of(wirelets);
        this.configSite = containerConfiguration.configSite().thenStack(ConfigSiteType.INJECTOR_CONFIGURATION_INJECTOR_BIND);
    }

    /**
     * Returns the configuration site of this configuration.
     * 
     * @return the configuration site of this configuration
     */
    public final InternalConfigSite configSite() {
        return configSite;
    }

    public void process() {
        List<ServiceNode<?>> nodes;
        if (injector instanceof AbstractInjector) {
            nodes = ((AbstractInjector) injector).copyNodes();
        } else {
            throw new IllegalArgumentException("Currently only Injectors created by Packed are supported");
        }
        processImport(nodes);
    }

    /**
     * @param externalNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    private void processImport(List<? extends ServiceNode<?>> externalNodes) {

        // First create service build nodes for every existing node
        HashMap<Key<?>, BuildServiceNode<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : externalNodes) {
            if (!node.isPrivate()) {
                BuildServiceNodeImported<?> n = new BuildServiceNodeImported<>(ib, configSite.replaceParent(node.configSite()), this, node);
                nodes.put(node.key(), n);
            }
        }

        // Process each wiring operation
        for (Wirelet operation : wirelets.toList()) {
            if (operation instanceof Wirelet) {
                // AppPackedBundleSupport.invoke().startWireOperation(operation);
                nodes = processImportStage(operation, nodes);
                // AppPackedBundleSupport.invoke().finishWireOperation(operation);
                throw new Error();
            }
        }

        // Add all to the private node map
        for (BuildServiceNode<?> node : nodes.values()) {
            if (!ib.nodes.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.key()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, BuildServiceNode<?>> processImportStage(Wirelet stage, HashMap<Key<?>, BuildServiceNode<?>> nodes) {
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

        HashMap<Key<?>, BuildServiceNode<?>> newNodes = new HashMap<>();

        for (Iterator<BuildServiceNode<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            BuildServiceNode<?> node = iterator.next();
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
