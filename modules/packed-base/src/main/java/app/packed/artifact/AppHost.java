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
import app.packed.lifecycle.RunState;

/**
 * A host w
 */
public interface AppHost {

    // Will initialize default, but can be disabled via a wirelet...

    /**
     * Returns a stream containing all active apps. Use {@link #guests()} to include apps that have not in the running
     * phase.
     * 
     * @return a stream containing all active apps
     * @see #guests()
     */
    Stream<App> apps();// Skal vi have en 3 mulighed... running()

    /// guest.suspend();
    /// guest.moveToXServer()...

    // Problemet med ikke at f.eks. builde bundlen... er jo vi ikke kender navnet...
    // Saa maaske goer man det altid
    // Vi skal jo have unikke navne.. Saa med mindre man hedder noget a.la. DDddd$1
    Guest<App> deploy(ContainerSource bundle, Wirelet... wirelets);

    Stream<Guest<App>> guests();
    // undeply

    App initialize(ContainerSource bundle, Wirelet... wirelets);

    void run(ContainerSource source, Wirelet... wirelets);

    /**
     * Create and start an application from the specified source with this host as its parent. The state of the returned
     * application is {@link RunState#RUNNING}.
     *
     * @param source
     *            the source of the application
     * @param wirelets
     *            any wirelets to use in the construction of the application
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be constructed or started properly
     * @see App#start(ContainerSource, Wirelet...)
     */
    App start(ContainerSource source, Wirelet... wirelets);

    Guest<App> startGuest(ContainerSource source, Wirelet... wirelets); // Man kan ikke gaa Artifact->Guest???
}
// add, remove, iterate, Listen to changes...
//// Argh det er her vi gerne vil have redeploy...
//// Lyt til dette direktorie...
// Redeploy? Eller bare erstat?

// Basel set et map jo...

/// Guest.java has a name() method -> We need to compose the artifact

// CLose reason, failing, restarting, Replacing