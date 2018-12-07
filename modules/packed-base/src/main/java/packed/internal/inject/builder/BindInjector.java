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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorImportStage;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNode;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An abstract class for the injector bind methods
 * {@link InjectorConfiguration#injectorBind(Class, ImportExportStage...)},
 * {@link InjectorConfiguration#injectorBind(InjectorBundle, ImportExportStage...)}, and
 * {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
abstract class BindInjector {

    /** The configuration site of binding. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector that binding another bundle or injector. */
    final InjectorBuilder injectorConfiguration;

    /** The import export stages arguments. */
    final List<ImportExportStage> stages;

    final Set<Key<?>> requiredKeys = new HashSet<>();

    @Nullable
    final InjectorBundle bundle;

    BindInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, InjectorImportStage[] stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = List.of(stages); // checks for null
        this.bundle = null;
    }

    BindInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, InjectorBundle bundle, ImportExportStage[] stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = List.of(stages); // checks for null
        this.bundle = requireNonNull(bundle, "bundle is null");
    }

    /**
     * @param importableNodes
     *            all nodes that are available for import from bound injector or bundle
     */
    void processImport(List<? extends ServiceNode<?>> importableNodes) {
        // A working array of nodes that we want to import
        ArrayList<ServiceBuildNodeImport<?>> nodes = new ArrayList<>(importableNodes.size());

        for (ServiceNode<?> n : importableNodes) {
            if (!n.isPrivate()) {
                nodes.add(new ServiceBuildNodeImport<>(injectorConfiguration, configurationSite.replaceParent(n.getConfigurationSite()), this, n));
            }
        }
        for (ImportExportStage s : stages) {
            if (s instanceof InjectorImportStage) {
                processImportStage((InjectorImportStage) s, nodes);
            }
        }
        for (ServiceBuildNodeImport<?> i : nodes) {
            if (i != null) {
                injectorConfiguration.privateNodeMap.put(i);
            }
        }
    }

    void processImportStage(InjectorImportStage stage, ArrayList<ServiceBuildNodeImport<?>> nodes) {
        // Find @Provides, lookup class
        boolean rebinds = false;

        for (int i = 0; i < nodes.size(); i++) {
            ServiceBuildNodeImport<?> importNode = nodes.get(i);
            if (importNode != null) {
                Key<?> existing = importNode.getKey();

                stage.importService(importNode);

                if (importNode.getKey() == null) {
                    nodes.set(i, null);
                } else {
                    if (!importNode.getKey().equals(existing)) {
                        rebinds = true;
                        // Should make new, with new configuration site
                    }
                }
            }
        }
        if (rebinds) {
            // TODO check that we do not have multiple nodes with the same key now....
            // put them in a map...
        }
    }

}
