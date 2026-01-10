/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.service.mirrorold;

import java.util.Set;

/**
 *
 */
// Hmm, represents a constant.
// Do we support runtime constants??? As in post-build constants
// I'm guessing if we are super lazy.
// A constant mirror is never a bean. Which might itself be a constant.`

// NonBeanConstant?


// Tror ikke det er en constant. Men maaske et ArgumentMirror istedet for???

public non-sealed class BuildArgumentMirror implements ServiceProviderIsThisUsefulMirror {

    //// Vi vil godt vide hvor den bliver brugt henne...

    // Er det altid operations bindinger?????
    // Nej det kan ogsaa vaere som en ServiceProvider i et namespace (maaske er den slet ikke i nogen bindinger)
    // En bean kan godt vaere constant. U

    public Set<Object> useSites() {
        return Set.of();
    }
}
/// Lad os sige den bliver brugt i forbindelse med Config...
// Vi laeser en fil en naar vi starter...Saa er det vel en constant???
// Jeg spoerger fordi ServiceProviderMirror kan baade vaere en bean, en operation og en constant...
// Lidt som Co