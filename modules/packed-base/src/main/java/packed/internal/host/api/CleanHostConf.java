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
package packed.internal.host.api;

import app.packed.artifact.ArtifactDriver;
import app.packed.base.Key;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;

/**
 *
 */
public interface CleanHostConf {

    // Will fail if no default driver is set...

    // deploy and start together with host
    // deploy and delay start when needed

    // deploy and start together with host + Hard Link
    // deploy and delay start when needed + Hard Link

    // Maaske er det GuestWirelets...
    // ArtifactWirelets.delayInitialization(), ArtifactWirelet.delayStart(), ArtifactWirelet.hardLink();

    void add(Bundle bundle, Wirelet... wirelets);

    void add(Bundle bundle, ArtifactDriver<?> driver, Wirelet... wirelets);

    // Altsaa hvor skal jeg gemme disse images...
    // Ikke i bundlen taenker jeg...
    // I Think we need to store dem in the Host...
    // a.la. newImage(Key<?> key)
    // Taenker du kun laver et image, hvis det skal bruges flere gange...
    // Det giver simpelthen ikke mening andet taenker.
    // Saa lad os kalde det

    // IDK
    void newImage(Key<?> key, Bundle bundle, Wirelet... wirelets);

    GuestImage newImage(Bundle bundle, Wirelet... wirelets);

    GuestImage newImage(Bundle bundle, ArtifactDriver<?> driver, Wirelet... wirelets);
}

// Will not Extend ArtifactImage.. Is it an artifact???
interface GuestImage {

}
// Host with fixed driver.. maybe allow

// Alternativ hosts supportere kun artifakter af den samme type...

// Tror sgu kun vi tillader en type...
// MultiHostDriver

// wirelet.prefixWith(Wirelet[] wireletsThatShouldBePrefixed, wirelet... prefix);

//wirelet.postfixWith(Wirelet[] wireletsThatShouldBePostfixed, wirelet... postfix);