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

import java.util.ArrayList;
import java.util.List;

import app.packed.bundle.Bundle;
import app.packed.bundle.WiringOption;
import app.packed.util.Key;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;

/**
 *
 */
final class BindInjectorFromBundle extends AbstractWiring {

    final ContainerBuilder newConfiguration;

    BindInjectorFromBundle(ContainerBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Bundle bundle, List<WiringOption> stages) {
        super(injectorConfiguration, configurationSite, bundle, stages);
        this.newConfiguration = new ContainerBuilder(configurationSite, bundle);
    }

    /**
     * 
     */
    void processImport() {
        bundle.doConfigure(newConfiguration);
        processImport(newConfiguration.publicNodeList);
    }

    void processExport() {
        for (WiringOption s : operations) {
            if (s instanceof WiringOption) {
                throw new UnsupportedOperationException();
            }
        }
        List<ServiceBuildNodeImport2<?>> exports = new ArrayList<>();
        if (newConfiguration.box.services().required != null) {
            for (Key<?> k : newConfiguration.box.services().required) {
                if (newConfiguration.box.services().nodes.containsKey(k)) {
                    throw new RuntimeException("OOPS already there " + k);
                }
                ServiceNode<?> node = injectorConfiguration.box.services().nodes.getRecursive(k);
                if (node == null) {
                    throw new RuntimeException("OOPS " + k);
                }
                ServiceBuildNodeImport2<?> e = new ServiceBuildNodeImport2<>(newConfiguration, configurationSite.replaceParent(node.configurationSite()), this,
                        node);
                exports.add(e);
                newConfiguration.box.services().nodes.put(e);
            }
        }
    }
}
