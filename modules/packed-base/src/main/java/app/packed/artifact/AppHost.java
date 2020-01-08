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
package app.packed.artifact;

import java.util.stream.Stream;

import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 *
 */
public interface AppHost {

    // Will initialize default, but can be disabled via a wirelet...

    // Problemet med ikke at f.eks. builde bundlen... er jo vi ikke kender navnet...
    // Saa maaske goer man det altid
    Guest<App> deploy(ContainerSource bundle, Wirelet... wirelets);

    Stream<Guest<App>> guests();
    // undeply

    // Will initialize
    App initialize(ContainerSource bundle, Wirelet... wirelets);

    // Will initialize
    App start(ContainerSource bundle, Wirelet... wirelets);

    void run(ContainerSource source, Wirelet... wirelets);

    /// guest.suspend();
    /// guest.moveToXServer()...
}
// add, remove, iterate, Listen to changes...
//// Argh det er her vi gerne vil have redeploy...
//// Lyt til dette direktorie...
// Redeploy? Eller bare erstat?

// Basel set et map jo...

/// Guest.java has a name() method -> We need to compose the artifact

// CLose reason, failing, restarting, Replacing