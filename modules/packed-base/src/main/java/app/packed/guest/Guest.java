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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.component.Wirelet;

/**
 * 
 */

// Restarting a guest actually means terminated an existing guest. And starting a new one.

// Component, Service = Describes something, Guest also controls something...
// I would imagine we want something to iterate over all state machines...

// StateMachine

// host facing
// Maybe GuestController.... And guest can be a view/readable thingy

// External facing...

// Taenker ikke en Guest har attributer direkte. Udover componenten..
// Taenker heller ikke ServiceRegistry
public interface Guest {

    /**
     * Returns a snapshot of the guests current state.
     * 
     * @return a snapshot of the guests current state
     */
    default GuestStateSnapshot snapshotState() {
        throw new UnsupportedOperationException();
    }

    // app.guest().start(); I think that is cool, no need to move this method to App for now
    Guest start();

    <T> CompletableFuture<T> startAsync(T result);

    Guest stop(GuestStopOption... options);

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     *
     * @param options
     *            optional guest stop options
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    <T> CompletableFuture<T> stopAsync(T result, GuestStopOption... options);

    // Altsaa vi skal ikke have interface StartOption of @interface WireletOption... Saa maa de hedde noget forskelligt

    // Altsaa skal vi have den her.. eller kan vi komme langt nok med wirelets???
    // Og stopoptions... Kan vi komme derhen med wirelets??? F.eks. lad os sige vi gerne vil restarte med nogle andre
    // settings???? StopOption.restart(Wirelet... wirelets)
    // Jeg vil ikke afvise at vi skal have den... men Wirelets er maaske lidt bedre...
    public interface GuestStartOption {

        // LifecycleTransition
        static GuestStartOption reason(String reason) {
            throw new UnsupportedOperationException();
        }
    }

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

        static GuestStopOption erroneous(Supplier<Throwable> cause) {
            throw new UnsupportedOperationException();
        }

        static GuestStopOption erroneous(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        static GuestStopOption forced() {
            throw new UnsupportedOperationException();
        }

        // Can be used as wirelet as well...
        static GuestStopOption forcedGraceTime(long timeout, TimeUnit unit) {
            // before forced???
            throw new UnsupportedOperationException();
        }

        static GuestStopOption now() {
            // Now == shutdownNow();
            throw new UnsupportedOperationException();
        }

        static GuestStopOption now(Throwable cause) {
            throw new UnsupportedOperationException();
        }

        static GuestStopOption restart(Wirelet... wirelets) {
            // restart(Wirelet.rename("Restart at ....");
            //// Men okay hvad hvis det ikke kan lade sige goere at omnavngive den...
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
    // normal
    // normal + restart(manual)
    // erroneous[cause]
    // erroneous[cause] + restart(manual)
    // forced() (either directly, or after
    // forced(cause?)
    // delayForce(10 min) <- try shutdown normally and then forced after X mins...

    // Scheduled (Altsaa er det ikke folks eget ansvar???)
    // Kun fordi vi supporter noget af det med wirelets
    // shutdown in 10 minutes and then restart... (altsaa kan man ikke s)
}
