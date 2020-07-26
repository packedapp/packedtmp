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
package sandbox.artifact.hostguest;

import sandbox.artifact.hosttest.ImageSet;

/** A context object that can be injected into components that functions as hosts. */

// Altsaa det er jo ikke reelt noget en actor skal have lov til at faa???

// HostContext vil man primaert bruge til at styre sine guests
// ComponentContext kan man bruge til forspoergsel om ens boern generelt..

// Add, Remove, Update guests

// The artifacts, the artifacts

// Need an ArtifactSet
// Eller ogsaa har vi kun den samme artifact...
// Og saa returnere vi F.eks. PackedActor
// som er initialiseret med den rigtige instance..
public interface HostContext {

    // Altsaa reelt set er det her jo antallet af boern...
    // Taenker det er noget vi kan faa fra component context???
    long guestCount(); // think int is ok... idk

    // embed this...
    ImageSet images();
}

// Vi man

// Saa HostContext

// HostLine

/// startIfAbsent()
// hc.images().start(Session.class);
