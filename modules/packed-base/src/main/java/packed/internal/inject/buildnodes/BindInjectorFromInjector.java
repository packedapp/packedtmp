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

import app.packed.bundle.InjectorBundle;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.inject.InjectorImportStage;
import app.packed.inject.Key;
import app.packed.util.Nullable;
import packed.internal.inject.InternalInjector;
import packed.internal.inject.Node;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * Represents an imported injector via {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
public final class BindInjectorFromInjector extends BindInjector {

    /** The configuration site where the injector or bundle was bound. */
    final InternalConfigurationSite configurationSite;

    /** The configuration of the injector that the injector or bundle was bound to. */
    final InternalInjectorConfiguration injectorConfiguration;

    /** A map of all services that have been imported. */
    final Map<Key<?>, BuildNodeImport<?>> importedServices = new HashMap<>();

    /** The injector to bind. */
    @Nullable
    Injector injector;

    /** The import stages. */
    final List<InjectorImportStage> importStages;

    @Nullable
    final InjectorBundle bundle;

    /**
     * Creates a new
     * 
     * @param injectorConfiguration
     * @param configurationSite
     * @param injector
     * @param stages
     */
    public BindInjectorFromInjector(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, Injector injector,
            InjectorImportStage[] stages) {
        super(injectorConfiguration, configurationSite);
        this.injectorConfiguration = requireNonNull(injectorConfiguration);
        this.configurationSite = requireNonNull(configurationSite);
        this.injector = requireNonNull(injector, "injector is null");
        this.importStages = List.of(stages);
        this.bundle = null;
    }

    /**
     * 
     */
    public void doStuff() {
        InternalInjector ii = (InternalInjector) injector;

        // All the nodes we potentially want to import
        List<Node<?>> allNodes = List.copyOf(ii.nodes.toAll());// immutable

        BuildNodeImport<?>[] bns = new BuildNodeImport[allNodes.size()];
        for (int i = 0; i < bns.length; i++) {
            Node<?> n = allNodes.get(i);
            if (!n.isPrivate()) {
                InternalConfigurationSite cs = configurationSite.replaceParent(n.getConfigurationSite());
                bns[i] = new BuildNodeImport<>(injectorConfiguration, cs, this, n);
            }
        }

        for (InjectorImportStage stage : importStages) {
            // Find @Provides, lookup class
            for (int i = 0; i < bns.length; i++) {
                BuildNodeImport<?> bn = bns[i];
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
        for (BuildNodeImport<?> n : importedServices.values()) {
            configuration.privateBuildNodeList.add(n);
        }
    }
}
