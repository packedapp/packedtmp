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
package app.packed.lifetime.managed;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/** Various options that can be used when stopping a component. */
// Panic vs non-panic, a panic signals a non-normal shutdown

// interrupt vs non-interrupt
// forced - non-forced
//// Er kun aktuelt naar shutdown ikke foreloeber korrect...
// Den burde ikke have semantics indvirkning paa hvad der sker efter stop

// Move to top level c
public interface StopOption {

    static StopOption fail(Supplier<Throwable> cause) {
        throw new UnsupportedOperationException();
    }

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