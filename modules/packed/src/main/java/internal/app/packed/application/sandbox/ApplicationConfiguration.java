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
package internal.app.packed.application.sandbox;

import app.packed.application.BuildGoal;
import internal.app.packed.application.ApplicationSetup;

/**
 *
 */
//assembly.application().

// Can we override this???
public class ApplicationConfiguration {

    ApplicationSetup application;

    public BuildGoal buildGoal() {
        return application.goal;
    }

    // Den er faktisk lidt "farlig" hvis man beslutter at bruge den som en reusable application...
    // Fordi saa er man ikke root applikationen laengere...
    // Og saa vil man fejle...
    // Saa maaske signalere den bare shutdown "normally" og saa kan application hosten sige ok.
    // Jeg lukker ogsaa ned
    // is Ignored if not launchable
    void installShutdownHook() {
        // Taenker man fejler hvis man ikke er root
    }

    //?
    public boolean isRoot() {
        return true;
    }

    // assembly.realm
    // assembly.container
    // assemcly.application

    // Det eneste er

    // Restart
    // Lifecycle
}
