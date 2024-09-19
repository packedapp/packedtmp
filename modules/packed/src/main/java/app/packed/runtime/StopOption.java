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
package app.packed.runtime;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Various options that can be used when stopping a lifetime. */
// Panic vs non-panic, a panic signals a non-normal shutdown

// interrupt vs non-interrupt
// forced - non-forced
//// Er kun aktuelt naar shutdown ikke foreloeber korrect...
// Den burde ikke have semantics indvirkning paa hvad der sker efter stop

// Move to top level c

// Hvis vi fx for en Tag() saa kan man jo kun bruge det naar man lukker ned foerste gang.
// Ellers bliver de ignoreret. Kan jo ikke rigtig fejle, hvis lukker ned parallelt pga en fejl
// Men okay det er jo det samme fail, o.s.v.
public interface StopOption {

    static StopOption cancel() {
        throw new UnsupportedOperationException();
    }

    static StopOption fail(Supplier<Throwable> cause) {
        throw new UnsupportedOperationException();
    }

    // I think it can only be set for the original stop option
    static StopOption reason(String reason) {
        throw new UnsupportedOperationException();
    }

    // I actually, think it should be the first failure and not the last
    // if multiple failures are specified
    static StopOption fail(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    // Forced = try and interrupt
    static StopOption forced() {
        throw new UnsupportedOperationException();
    }

    // Can be used as wirelet as well...
    static StopOption forcedGraceTime(long timeout, TimeUnit unit) {
        // before forced???
        throw new UnsupportedOperationException();
    }

    // Er vel forced???
    static StopOption now() {
        // Now == shutdownNow();
        throw new UnsupportedOperationException();
    }


    static StopOption now(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    static StopOption restart() {
        // Now == shutdownNow();
        throw new UnsupportedOperationException();
    }

    // does not throw UOE
    static StopOption tryRestart() {
        // Now == shutdownNow();
        throw new UnsupportedOperationException();
    }


//        // add Runtime.IsRestartable??
//        static StopOption restart(Wirelet... wirelets) {
//            // restart(Wirelet.rename("Restart at ....");
//            //// Men okay hvad hvis det ikke kan lade sige goere at omnavngive den...
//            throw new UnsupportedOperationException();
//        }
//
//        // Will override default settings.
//        // linger would be nice
//        // Or maybe somewhere to replace the guest with a tombstone of some kind.
//        // Summarizing everything in the guest...
//
//        // Hmmmmmm IDK
//        static StopOption undeploy() {
//            throw new UnsupportedOperationException();
//        }
    // restart.. (Artifact must have been started with RestartWirelets.restartable();
}
// normal
// normal + restart(manual)
// erroneous[cause]
// erroneous[cause] + restart(manual)
// forced() (either directly, or after
// forced(cause?)
// delayForce(10 min) <- try shutdown normally and then forced after X mins...