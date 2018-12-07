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

import app.packed.bundle.InjectorImportStage;
import app.packed.inject.Injector;
import app.packed.inject.InjectorConfiguration;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * Represents an imported injector via {@link InjectorConfiguration#injectorBind(Injector, InjectorImportStage...)}.
 */
final class BindInjectorFromInjector extends BindInjector {

    /** The injector to bind. */
    @Nullable
    Injector injector;

    /**
     * Creates a new
     * 
     * @param injectorConfiguration
     * @param configurationSite
     * @param injector
     * @param stages
     */
    BindInjectorFromInjector(InjectorBuilder injectorConfiguration, InternalConfigurationSite configurationSite, Injector injector,
            InjectorImportStage[] stages) {
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
