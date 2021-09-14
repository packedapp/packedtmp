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
package packed.internal.container;

import app.packed.bundle.Wirelet;
import app.packed.bundle.WireletSelection;

/** A holder of wirelets. */
// Vi har faktisk ogsaa for den her paa runtime...
// Saa vi kan ikke kode den ind i ComponentSetup
public final class WireletWrapper {

    /** An empty wirelet wrapper. */
    static final WireletWrapper EMPTY = new WireletWrapper(CompositeWirelet.EMPTY);

    /** The number of unconsumed wirelets. */
    // Maaske tracker vi kun ikke interne wirelets
    int unconsumed;

    /** The wirelets we are wrapping. This array is safe for internal use. But must be cloned if exposed to users. */
    public final Wirelet[] wirelets;

    /** Creates a new wrapper. */
    public WireletWrapper(Wirelet[] wirelets) {
        this.wirelets = wirelets;
        this.unconsumed = wirelets.length;
    }

    public <T extends Wirelet> WireletSelection<T> sourceOf(Module module, Class<? extends T> wireletClass) {
        // Maaske skal vi have en caller med ala "Must be in the same module as"
        if (module != wireletClass.getModule()) {
            throw new IllegalArgumentException("The specified wirelet must be in module " + module + ", was " + module.getName());
        }
        return new PackedWireletSelection<>(this, wireletClass);
    }
    
    public int unconsumed() {
        return unconsumed;
    }
}
