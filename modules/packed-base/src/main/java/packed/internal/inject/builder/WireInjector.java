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

import java.util.List;

import app.packed.bundle.UpstreamWiringOperation;
import app.packed.bundle.WiringOperation;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfigurator;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.InternalInjector;

/**
 * Represents an imported injector via {@link InjectorConfigurator#wireInjector(Injector, UpstreamWiringOperation...)}.
 */
final class WireInjector extends AbstractWiring {

    /** The injector we are wiring. */
    final Injector injector;

    /**
     * Creates a new
     * 
     * @param injectorConfiguration
     * @param configurationSite
     * @param injector
     * @param stages
     */
    WireInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Injector injector, List<WiringOperation> stages) {
        super(injectorConfiguration, configurationSite, stages);
        this.injector = requireNonNull(injector, "injector is null");
    }

    void importServices() {
        InternalInjector ii = (InternalInjector) injector;

        // All the nodes we potentially want to import
        List<ServiceNode<?>> allNodes = ii.copyNodes();// immutable
        processImport(allNodes);
    }
}
