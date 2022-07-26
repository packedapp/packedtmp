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
package app.packed.lifetime.sandbox;

import app.packed.container.ContainerLifetimeCompanion;

/**
 * A mirror of {@link ContainerLifetimeCompanion}
 */ 

// !! Er udviklet videre i den anden ExtensionLifetimeFeature
// Bridge
// LifetimeHolder eksistere jo ogsaa maaske vi skal knytte dem taettere sammen
//

// Tror ikke vi har et mirror... Ejeren er som regel syntetisk.
// Med mindre vi hoster
public interface LifetimeBeanCompanionMirror {

    // All companions needs a (host) bean...
    // Det betyder ogsaa at hvis stateless skal kunne bruge companions.
    // Saa skal de jo bruge en bean...
    //LifetimeBeanMirror bean();
}
