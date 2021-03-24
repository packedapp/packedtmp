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
package app.packed.application;

import app.packed.component.Wirelet;

/**
 * A collection of wirelets that can be specified when building an {@link ApplicationImage image}.
 */
// Hmm Vi vil gerne kunne specificere en ArtifactDriver med ImageWirelets
// Og hvor use() stadig kan bruges...
// Saa paa en eller anden maade skal de ignoreres hvis de bliver brugt normalt via ArtifactDriver.use()...

// De virker dog kun paa en artifact rod...
// Og kun paa build time
class ImageWirelets {
    // Kun aktuelt for system images, ikke for sub-system images
    static Wirelet retainImage() {
        throw new UnsupportedOperationException();
    }

    /**
     * Can be used when creating an image
     * 
     * @return the wirelet
     */
    // MainImage kunne faktisk godt vaere et der kun bliver brugt en gang...
    // Og saa clearet...
    static Wirelet single() {
        throw new UnsupportedOperationException();
    }
}
