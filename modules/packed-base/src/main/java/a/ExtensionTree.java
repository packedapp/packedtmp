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
package a;

import app.packed.container.extension.Extension;
import app.packed.container.extension.ExtensionWirelet;
import app.packed.util.Nullable;

/**
 * Used for
 * 
 * Communicating among extensions of the same type when building a single artifact.
 * 
 * Communication when parent artifacts in a hosted situation..
 * 
 * 
 * Communicating with artifact sieblings in a artifact mesh.
 * 
 * 
 */
// Used for
// A non-arg public constructor
// context initialized by the runtime

// Extension trees takes care of communication between extensions of the same type in the same artifact.
// It also support communications with runtime other artifact

// Just a thin wrapper on ExtensionTreeContext...

// Taenker at hvis man skal bruge den fra en Extension, saa kan man faa en injected....

// Vi har sikkert brug for en 3-4 forskellige klasser....

// Man kan ikke kommunikere fra en wirelet uden et ExtensionTree.
// Og muligvis en klasse til (som vi endnu ikke har fundet paa)
//// InjectionExtension er nok ogsaa den svaereste
public abstract class ExtensionTree<E extends Extension> {

    ExtensionTreeContext<E> context;

    // Alternativ kan man..
    // Virker bare ikke for en host->guest eftersom host ikke er en extension, men en sidecar
    protected void onLinkage(@Nullable E parent, E child) {}

    // Fungere ikke searlig godt med images....og pipeline
    ////
    protected void onLinkage(@Nullable E parent, E child, ExtensionWirelet<E, ?> w) {}

    // Lifecycle is intertweened with Extension.
    // Extension has no support for communication with other extensions...

    // find commonroot

    // boolean hasParent

    // findSidecarInParent();

    enum LinkageType {
        DEPLOY, LINK, OF;
    }
}

// This is all done in extension tree. Avoid having public APIs that describes interaction points.
// This allow for easier development, that APIs are not public...

// ExtensionMediator, Communicator