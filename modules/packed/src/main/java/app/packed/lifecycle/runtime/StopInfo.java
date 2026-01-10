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
package app.packed.lifecycle.runtime;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.lifecycle.RunState;

/**
 * Information about why a lifetime (application, container or bean) was stopped.
 * <p>
 * Once a lifetime has been stopped this information will not change.
 * <p>
 * For example, if a lifetime fails while stopping with an exception this does not change what {@link #failure()}
 * returns
 */

// Success | Fejl | Exception
// En Reason
// PreviousState
// What happens Next?

// Den bliver lavet praecis en gang. Og aendre sig ikke
// Saa den kan ikke rigtig have informationer omkring forhold
// mellem root lifetimen og children (isDependant)

// Hvad hvis child bliver lukket ned normalt
// Og saa en parent lifetime lukker exceptionelt??? Bare aergelig sonnyboy

// Kan ikke se den er brugbar for en bean.
// Omvendt fungere den ikkke godt paa onStop saa
//ContainerStopInfo?
public interface StopInfo {

    Optional<Throwable> exception();

    /** {@return true if the lifetime completed normally with a result, otherwise false.} */
    boolean hasResult();

    boolean isCancelled();

    boolean isCompletedExceptionally();

    boolean isCompletedNormally();

    /** {@return the previous state of the lifetime before it was stopped.} */
    // Is always starting or running. What if initialized.stop?
    RunState fromState();

    //// If the lifetime completed normally with a result

    /** {@return the reason for why the lifetime was stopped. */
    Reason reason();

    // isNormal, isExceptional

    // Cause

    // Whats next

    /**
     *
     * The framework comes with a number of pre-defined reasons. But users and extensions are free to define their own.
     */
    // De her skal jo altsaa shared mellem beans og containers...
    public final class Reason {

        final String reason;

        private Reason(String reason) {
            this.reason = requireNonNull(reason);
        }

        /**
         * Indicates that the JVM has been shutdown. And the lifetime is closing as the result of a shutdown hook being called.
         *
         * @see app.packed.application.ApplicationWirelets#shutdownHook(StopOption...)
         * @see app.packed.application.ApplicationWirelets#shutdownHook(java.util.function.Function, StopOption...)
         * @see Runtime#addShutdownHook(Thread)
         */
        public static final Reason SHUTDOWN_HOOK = new Reason("ShutdownHook");

        /** Indicates that lifetime has timed out. */
        // Vi har baade TimeToStart timeout, or entrypoint timeout, tror kun vi har en?
        // Session Time Out (Maa vaere SESSION_TIMEOUT)
        public static final Reason TIMEOUT = new Reason("Timeout");

        /** An entry-point completed normally. */
        public static final Reason ENTRY_POINT_COMPLETED = new Reason("Normal");

        public static final Reason NORMAL = new Reason("Normal");

        public static final Reason FAILED_INTERNALLY = new Reason("Failed");

        public static final Reason RESTARTING = new Reason("Restarting");

        public static final Reason UNKNOWN = new Reason("Unknown");

//       WAS
        // NORMAL, EXECUTION_FAILED,
//       PERSISTING,
        // SPAWN, // We are spawning a new instance of Application somewhere else. Serialize what you need to
        // Was Cancelled
        // CANCELLED, // Should never be
        // The application is being upgraded
        // UPGRADING; // Redeploy??? Implies restarting
        // ApplicationRedeploy
    }

    public enum WhatsNextDog {

    }
}
//// Hmm, Don't think so. Let say it says container
//// And we have child application. Would lead to some confusion
// eller Scope [Application, Container, Bean]

//// Nope vil jeg mene, fordi saa kan vi lige pludselig ikke share den
// isDependendant, isRoot, int depth()?? hmm;
// Det kan komme paa initialize
