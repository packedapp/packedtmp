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
package app.packed.guest;

import java.util.concurrent.TimeUnit;

import app.packed.component.Wirelet;

/**
 *
 */

// ContainerStopOption????
// Eller er det generisk..? Kan den bruges paa en actor??? et Actor Trae...
// Hehe, hvis actor ogsaa er en artifact... Saa 

// Men taenker det her lifecycle er rimligt generisk...

// StopOptions is Wirelets at stop time...
// GuestStopOption????

// Flags.. Minder lidt om wirelets...
// Og list stop options
// https://twitter.github.io/util/docs/com/twitter/app/App.html

// ER HELT SIKKER IKKE EN DEL AF LIFECYCLE VIL JEG MENE
// Som udgangspunkt er det noget med Guest og goere..
// Med mindre instanser lige pludselig kan bruge det.
public interface GuestStopOption {

    static GuestStopOption erroneous(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    static GuestStopOption forced() {
        throw new UnsupportedOperationException();
    }

    // Can be used as wirelet as well...
    static GuestStopOption graceTime(long timeout, TimeUnit unit) {
        // before forced???
        throw new UnsupportedOperationException();
    }

    static GuestStopOption restart(Wirelet... wirelets) {
        // restart(Wirelet.rename("Restart at ....");
        //// Men okay hvad hvis det ikke kan lade sige goere at omnavngive den...
        throw new UnsupportedOperationException();
    }

    static GuestStopOption now() {
        // Now == shutdownNow();
        throw new UnsupportedOperationException();
    }

    static GuestStopOption now(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    // Will override default settings.
    // linger would be nice
    // Or maybe somewhere to replace the guest with a tombstone of some kind.
    // Summarizing everything in the guest...
    static GuestStopOption undeploy() {
        throw new UnsupportedOperationException();
    }

    // restart.. (Artifact must have been started with RestartWirelets.restartable();
}
//normal
//normal + restart(manual)
//erroneous[cause]
//erroneous[cause] + restart(manual)
// forced() (either directly, or after 
// forced(cause?)
// delayForce(10 min) <- try shutdown normally and then forced after X mins... 

// Scheduled (Altsaa er det ikke folks eget ansvar???)
// Kun fordi vi supporter noget af det med wirelets
// shutdown in 10 minutes and then restart... (altsaa kan man ikke s)