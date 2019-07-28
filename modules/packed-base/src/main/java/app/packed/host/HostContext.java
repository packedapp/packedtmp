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
package app.packed.host;

/**
 *
 */
// Taenker den er equivalent til ArtifactRuntimeContext....
// Hvor AppHost saa wrapper en HostContext.....
public interface HostContext {

    // Contract

    // deploy/undeploy

    // Canary deploy, runtime updates

    // inter artifact communication
    //// Pakke services for en artifact ind

    // Vi vil gerne supportere
}
// Kan vi lave dem paa runtime???
/// Hvorfor ikke....
/// Jeg taenker ogsaa paa en eller anden form for opdeling.
/// Isaer mht til simuleringer....
/// Men det er jo kun aktuelt, hvis vi har kommunikation paa tvaers

/// Maaske skal vi kigge hen mod noget VPN???
/// Det er jo saadan set

// Giver det mening kun at bruge hosten som parent...
//// Forstaaet paa den maade at det er fire og forget...
//// F.eks. en injector...
//// Nej saa laver vi jo bare en injector.
//// Og bruger den som parent.
