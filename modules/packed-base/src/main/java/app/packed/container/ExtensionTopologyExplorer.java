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
package app.packed.container;

import java.util.List;
import java.util.Optional;

/**
 *
 */

// Pipelines... Hosts...
public interface ExtensionTopologyExplorer<E extends Extension> {

    Optional<E> findAncestor(ExtensionContext ec);

    Optional<E> parent(ExtensionContext ec);

    List<E> children(ExtensionContext ec);

    // desendents...
    // checkEitherRootArtifactOr Parent has extension (Check is viral... Maaske I ExtensionSidecar...
}

// Extensions

// Inter-artifact extension Communication

// parent/ findAncestor / findLastAncestor

// children / ancestors (all?)  (vil maaske hellere have noget forEach())....

// Wirelets -> Altid knyttet til en extension... (hvis linked -> final)

// Cross-artifact extension communication

/// Altsaa hvis man er i samme pod kan man jo finde ud af det paa samme maade....

//Hvis man ikke er...
// Saa saa snart vi tillader kommunikation mellem dem saa kan man...
// Og vi skal tillade kommunikation....

// Saa har en WeakReference 