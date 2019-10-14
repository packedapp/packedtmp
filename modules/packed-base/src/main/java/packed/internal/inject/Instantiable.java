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
package packed.internal.inject;

import java.lang.module.ModuleDescriptor.Provides;

import app.packed.util.Nullable;
import packed.internal.container.FactoryComponentConfiguration;

/**
 *
 */

// Either a Component
// Or a singleton service defined with @Provides
public interface Instantiable {

    /**
     * Non-static methods or fields annotated with {@link Provides} may have a component that needs instantiating
     * 
     * @return stuff
     */
    @Nullable
    // Hmm, en prototype @Provides kan jo ogsaa have det...
    // Saa maaske skal den ikke vaere paa dette interface
    FactoryComponentConfiguration<?> declaringComponent();

    // List<ServiceDependency> dependencies

    // Internt er service != di

    // injection of Extension + Instantiation Context != Dependency Injection
    // Saa vi har maaske altid Dependency Injection enabled...
    // Men ikke noedvendigvis services...

    // ServiceRequest -> Provides.Request -> Provides.PrototypeRequest
    // Maybe just PrototypeRequest...
}
