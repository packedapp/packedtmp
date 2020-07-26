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
package sandbox.artifact.hosttest;

import app.packed.container.Wirelet;

/**
 *
 */
// Som regel har vi en navne generator..
// Maaske den endda kan tage en Wirelet som parameter....

// Hvad med compute if absent???
// Skal vi bare sige at start() returnere en eksisterende

// Igen ideen er jo egentlig ikke at den noedvendigvis skal exporteres til end users... Faktisk ikke
// End Users skal wrappe den... Saa der er en staerk binding mellem configurationen og det interface
// man exportere...

// start or
public interface FixedBundleFixedDriverHostLine<T> {

    // Will get of the specified name
    // Men boer den ikke fejle hvis navnet ikke eksistere????
    T getOrStart(String name, Wirelet... wirelets);

    T getOrStartAsync(String name, Wirelet... wirelets);

    T start(SomeGuest guest, Wirelet... wirelets);

    T start(Wirelet... wirelets);

    T startAsync(SomeGuest guest, Wirelet... wirelets);

    // add
    // remove

    // Vi kan ikke erstatte den med noget...

    // Det her er f.eks. Session

    // Session ligger 100 %fast....
    // Man kan endda specificere en navne generator paa linjen/hosten

    // add, remove
    // suspend... <-- ligger den der naar man itereret.

    // Man skal kunne loade den fra disk/db (Altsaa reaktivere en Session)
    // Det er maaske udelukkende
}
// Altsaa en mulighed er ikke at have start()..

// Men bare f.eks. get/remote/ect

// get = Create with this name
// get = Create with this name, unless a specific name is specified in which case see if it exists...

// Men configurere hvordan det fungere...