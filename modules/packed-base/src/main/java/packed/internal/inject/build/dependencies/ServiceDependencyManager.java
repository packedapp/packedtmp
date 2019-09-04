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
package packed.internal.inject.build.dependencies;

import java.util.ArrayList;

import app.packed.config.ConfigSite;
import app.packed.inject.InjectionExtension;
import app.packed.inject.InjectorContract.Builder;
import app.packed.util.Key;
import packed.internal.inject.build.InjectorBuilder;

/**
 * This class takes care of dependencies.
 * 
 * @see InjectionExtension#manualRequirementsManagement()
 * @see InjectionExtension#require(Key)
 * @see InjectionExtension#requireOptionally(Key)
 */
public final class ServiceDependencyManager {

    /**
     * Explicit requirements, typically added via {@link InjectionExtension#require(Key)} or
     * {@link InjectionExtension#requireOptionally(Key)}.
     */
    final ArrayList<ExplicitRequirement> explicitRequirements = new ArrayList<>();

    /**
     * Whether or not the user must explicitly specify all required services. Via {@link InjectionExtension#require(Key)},
     * {@link InjectionExtension#requireOptionally(Key)} and add contract.
     * <p>
     * In previous versions we kept this information on a per node basis. However, it does not work properly with "foreign"
     * hook methods that make use of injection. Because they may not be processed until the bitter end, so it was only
     * really services registered via the provide methods that could make use of them.
     */
    public boolean manualRequirementsManagement;

    DependencyGraph graph;

    /** Enables manual requirements management. */
    public void manualRequirementsManagement() {
        manualRequirementsManagement = true;
    }

    public void require(Key<?> key, boolean isOptional, ConfigSite configSite) {
        explicitRequirements.add(new ExplicitRequirement(key, configSite, isOptional));
    }

    public void analyze(InjectorBuilder builder) {
        // It does not make sense to try and resolve
        graph = new DependencyGraph(builder);
        graph.analyze(builder.exporter);
    }

    /**
     * @param services
     */
    public void buildDescriptor(Builder services) {
        graph.buildContract(services);
    }
}
