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
package sandbox.lifetime.old;

import app.packed.build.BuildGoal;
import internal.app.packed.application.ApplicationSetup;

/**
 *
 */
//assembly.application().

// Can we override this???
public final class ApplicationConfiguration {

    ApplicationSetup application;

    public BuildGoal buildGoal() {
        return application.deployment.goal;
    }

    // Den er faktisk lidt "farlig" hvis man beslutter at bruge den som en reusable application...
    // Fordi saa er man ikke root applikationen laengere...
    // Og saa vil man fejle...
    // Saa maaske signalere den bare shutdown "normally" og saa kan application hosten sige ok.
    // Jeg lukker ogsaa ned
    // is Ignored if not launchable
    // Maaske skal vi ogsaa exponere applicationConfiguration#installShutdownHook(ContainerConfiguration?)
    void installShutdownHook() {
        // Taenker man fejler hvis man ikke er root
    }

    // ?
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
