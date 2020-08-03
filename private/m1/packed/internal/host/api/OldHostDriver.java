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
import packed.internal.host.HostContext;

/**
 *
 */
// Vi lejer med 2 designs

// Conf + HostDriver, eller bare HostDriver.... Og saa en generisk HostConfiguration...

// Brugeren skal bruge en

// Conf <- skal vi have den.... Eller vi laver den paa driveren...

// AppHost.
/**
 * @param <H>
 *            The type of host this driver produces.
 */

// Maaske laver den en configuration????
// addHost(AppHost.driver()).

// Paa den anden side... A er lidt ligegyldig for brugere...

// C -> Configurations typen
// H -> Host typen

public abstract class OldHostDriver<C, H, A> {

    protected OldHostDriver(Option... options) {}
    // protected abstract H newMultiHost(MultiHostContext context);

    protected abstract H newHost(HostContext<A> context);

    protected abstract C newConfiguration(HostConfigurationContext context);

    public static class Option {

        public static Option defaultGuest(ArtifactDriver<?> driver) {
            throw new UnsupportedOperationException();
        }
    }
}
//
//protected final C newHostConfiguration(HostConfigurationContext context) {
//  throw new UnsupportedOperationException();
//}

// Option -> OnlyAllowImagesAsGuests... <- makes sure we optimize stuff...

// WebSessionHost

interface MultiHostContext {

}
