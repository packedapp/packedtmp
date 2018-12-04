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
import packed.internal.inject.Node;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An abstract class for the various injector bind methods such as
 * {@link InjectorConfiguration#injectorBind(Class, ImportExportStage...)} and
 * {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
abstract class BindInjector {

    /** The configuration site where the injector was imported. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector that is import/exporting services. */
    final InjectorBuilder injectorConfiguration;

    /** The import stages. */
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

    BindInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, InjectorBundle bundle,
            ImportExportStage[] stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = List.of(stages); // checks for null
        this.bundle = requireNonNull(bundle, "bundle is null");
    }

    /**
     * 
     */
    void processNodes(List<? extends Node<?>> existingNodes) {
        BuildNodeImport<?>[] importNodes = new BuildNodeImport[existingNodes.size()];

        for (int i = 0; i < importNodes.length; i++) {
            Node<?> n = existingNodes.get(i);
            if (!n.isPrivate()) {
                importNodes[i] = new BuildNodeImport<>(injectorConfiguration, configurationSite.replaceParent(n.getConfigurationSite()), this, n);
            }
        }

        for (ImportExportStage s : stages) {
            if (s instanceof InjectorImportStage) {
                InjectorImportStage stage = (InjectorImportStage) s;
                // Find @Provides, lookup class
                boolean rebinds = false;
                for (int i = 0; i < importNodes.length; i++) {
                    BuildNodeImport<?> importNode = importNodes[i];
                    if (importNode != null) {
                        Key<?> existing = importNode.getKey();

                        stage.importService(importNode);

                        if (importNode.getKey() == null) {
                            importNodes[i] = null;
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
        for (int i = 0; i < importNodes.length; i++) {
            if (importNodes[i] != null) {
                // TODO check non existing
                injectorConfiguration.privateNodeMap.put(importNodes[i]);
            }
        }
    }
}
