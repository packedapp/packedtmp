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

import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorImportStage;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.Key;
import packed.internal.inject.Node;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * An abstract class for the various injector bind methods such as
 * {@link InjectorConfiguration#injectorBind(Class, ImportExportStage...)} and
 * {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
public abstract class BindInjector {

    /** A map of all services that have been imported. */
    final Map<Key<?>, BuildNodeImport<?>> importedServices = new HashMap<>();

    /** The configuration site where the injector was imported. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector. */
    final InternalInjectorConfiguration injectorConfiguration;

    /** The import stages. */
    final List<ImportExportStage> stages;

    public BindInjector(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, InjectorImportStage[] stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = List.of(stages);
    }

    public BindInjector(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, ImportExportStage[] stages) {
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.stages = List.of(stages);
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

                        stage.process(importNode);

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
                importedServices.put(importNodes[i].getKey(), importNodes[i]);
            }
        }
    }
}
