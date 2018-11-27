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

import java.util.ArrayList;
import java.util.List;

import app.packed.bundle.ImportExportStage;
import app.packed.bundle.InjectorBundle;
import app.packed.bundle.InjectorExportStage;
import app.packed.inject.Key;
import packed.internal.bundle.BundleSupport;
import packed.internal.inject.Node;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 *
 */
final class BindInjectorFromBundle extends BindInjector {

    private final InjectorBundle bundle;

    final InternalInjectorConfiguration newConfiguration;

    BindInjectorFromBundle(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, InjectorBundle bundle,
            ImportExportStage[] filters) {
        super(injectorConfiguration, configurationSite, filters);
        this.bundle = requireNonNull(bundle, "bundle is null");
        this.newConfiguration = new InternalInjectorConfiguration(configurationSite, bundle);
    }

    /**
     * 
     */
    void process() {
        BundleSupport.configure(bundle, newConfiguration, true);
        processNodes(newConfiguration.publicNodeList);
    }

    void processExport() {
        for (ImportExportStage s : stages) {
            if (s instanceof InjectorExportStage) {
                throw new UnsupportedOperationException();
            }
        }

        List<BuildNodeExport<?>> exports = new ArrayList<>();
        for (Key<?> k : newConfiguration.requiredServicesMandatory) {
            if (newConfiguration.privateNodeMap.nodes.containsKey(k)) {
                throw new RuntimeException("OOPS already there " + k);
            }
            Node<?> node = injectorConfiguration.privateNodeMap.nodes.get(k);
            if (node == null) {
                throw new RuntimeException("OOPS " + k);
            }
            BuildNodeExport<?> e = new BuildNodeExport<>(newConfiguration, configurationSite.replaceParent(node.getConfigurationSite()), this, node);
            exports.add(e);
            newConfiguration.privateNodeMap.put(e);
        }
    }
}
