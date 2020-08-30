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
package app.packed.lifecycle;

import app.packed.component.Wirelet;

/**
 * These wirelets can only be used in the assembly phase of a component system.
 */
// Wirelet assemblyTimeOnly(Wirelet w); Hmmm idk if useful
public final class AssemblyWirelets {

    // NO FAIL <--- maaske brugbart for analyse

    // fail on warnings.

    // Throw XX exception instead of

    // Additional to people overridding artifacts, bundles, ect.
    public static Wirelet checkRuleset(Object... ruleset) {
        throw new UnsupportedOperationException();
    }

    // Taenker vi printer dem...
    // Og er det kun roden der kan disable dem???
    public static Wirelet disableWarnings() {
        throw new UnsupportedOperationException();
    }

    // Disable Host <--- Nej, det er et ruleset....
}
/// Interface with only static methods are