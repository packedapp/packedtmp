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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorImportStage;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.ServiceNode;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An abstract class for the injector bind methods
 * {@link InjectorConfiguration#bindInjector(Class, ImportExportStage...)},
 * {@link InjectorConfiguration#bindInjector(InjectorBundle, ImportExportStage...)}, and
 * {@link InjectorConfiguration#bindInjector(Injector, InjectorImportStage...)}.
 */
abstract class BindInjector {

    @Nullable
    final InjectorBundle bundle;

    /** The configuration site of binding. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector that binding another bundle or injector. */
    final InjectorBuilder injectorConfiguration;

    final Set<Key<?>> requiredKeys = new HashSet<>();

    /** The import export stages arguments. */
    final List<ImportExportStage> stages;

    BindInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, InjectorBundle bundle, List<ImportExportStage> stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = requireNonNull(stages);
        this.bundle = requireNonNull(bundle, "bundle is null");
    }

    BindInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, List<ImportExportStage> stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = requireNonNull(stages);
        this.bundle = null;
    }

    /**
     * @param importableNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    void processImport(List<? extends ServiceNode<?>> importableNodes) {
        HashMap<Key<?>, ServiceBuildNodeImport<?>> nodes = new HashMap<>();
        for (ServiceNode<?> node : importableNodes) {
            if (!node.isPrivate()) {
                nodes.put(node.getKey(),
                        new ServiceBuildNodeImport<>(injectorConfiguration, configurationSite.replaceParent(node.getConfigurationSite()), this, node));
            }
        }
        // Process each stage
        for (ImportExportStage stage : stages) {
            if (stage instanceof InjectorImportStage) {
                nodes = processImportStage((InjectorImportStage) stage, nodes);
                BundleSupport.invoke().stageOnFinish(stage);
            }
        }

        // Add all to the private node map
        for (ServiceBuildNodeImport<?> node : nodes.values()) {
            if (!injectorConfiguration.privateNodeMap.putIfAbsent(node)) {
                throw new InjectionException("oops for " + node.getKey()); // Tried to import a service with a key that was already present
            }
        }
    }

    private HashMap<Key<?>, ServiceBuildNodeImport<?>> processImportStage(InjectorImportStage stage, HashMap<Key<?>, ServiceBuildNodeImport<?>> nodes) {
        // Find @Provides, lookup class

        HashMap<Key<?>, ServiceBuildNodeImport<?>> newNodes = new HashMap<>();

        for (Iterator<ServiceBuildNodeImport<?>> iterator = nodes.values().iterator(); iterator.hasNext();) {
            ServiceBuildNodeImport<?> node = iterator.next();
            Key<?> existing = node.getKey();

            // invoke the import function on the stage
            BundleSupport.invoke().stageOnService(stage, node);

            if (node.getKey() == null) {
                iterator.remove();
            } else if (!node.getKey().equals(existing)) {
                iterator.remove();
                // TODO check if a node is already present
                newNodes.put(node.getKey(), node); // Should make new, with new configuration site
            }
        }
        // Put all remaining nodes in newNodes;
        newNodes.putAll(nodes);
        return newNodes;
    }

}
