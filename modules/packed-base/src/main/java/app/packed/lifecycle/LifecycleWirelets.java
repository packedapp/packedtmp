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
package app.packed.lifecycle;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactSource;
import app.packed.component.Wirelet;

/**
 * Wirelets that can be used when wiring containers. For example, via {@link App#start(ArtifactSource, Wirelet...)} or
 * {@link App#execute(ArtifactSource, Wirelet...)}.
 */

// InvalidWireletScopeExtension extends IAE
// InvalidWireletApplicationException -> Thrown when trying to apply a wirelet in a situation where it cannot be used

// Do we allow them in Image????
// I don't see why not... So
//// Taenker ikke det er App. maaske Lifecycle wirelets....
// Isaer hvis App er en wrapper over en masse ting...

// LifecycleExtension refererer til en container!

//Runables -> InVirtualThread

// Take a ThreadBuilder???
public interface LifecycleWirelets {

    /**
     * @param state
     *            the state
     * @param task
     *            the task to run
     * @return the new wirelet
     */
    // How are exceptions handled???
    // I think the container should be shutdown...
    // Alternative is to print the exception

    // Users should execute

    // Maybe its not a task... More like a callback
    static Wirelet entering(LifecycleState state, Runnable task) {
        throw new UnsupportedOperationException();
    }

    // foo with x = 45;
    // foo with { x = 45; y = 34 }
    // Vil gerne have noget der kalder et callback on

    // Container reaches Lifecycle State
    // Container is leaving Lifecycle State
    // Container has been in Lifecycle State XX amount of time
    // Container came to Lifecycle State XX amount of time ago

    // Failing???? Is that lifecycle?? I assume so

    // Taenker ikke vi kan sige.. Koer den her foerend initialization metoder paa FooExtension koerer...

    // TimeToLive -> Container has been in lifecycle state 34 seconds, container.shutdown();

//    static Wirelet computeWithInstant(LifecycleState state, Function<Consumer<Instant>> f) {
//
//        computeWithInstant(LifecycleState.START, ff -> ff.accept(i -> {
//
//        }));
//        // com
//        throw new UnsupportedOperationException();
//    }

    static Wirelet deadline(Instant deadline) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    // Den har et andet navn en CTRL_C...
    // shutdownHook() -> Adds a shutdown directly I think...

    // Vi skal lige sikre os at det ikke bliver installeret
    // Foerend vi er fuldt initialiseret. Da vi ikke supportere
    // Fremmede traade der kalder ind paa os naar vi bygger.
    static Wirelet shutdownHook() {
        throw new UnsupportedOperationException();
    }

    static Wirelet shutdownHook(Supplier<? extends Throwable> withException) {
        throw new UnsupportedOperationException();
    }

    static Wirelet timeToLive(Duration duration) {
        // Duration is from Running transitioning...
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    /**
     * Sets a maximum time for the container to run. When the deadline podpodf the app is shutdown.
     * 
     * @param timeout
     *            the timeout
     * @param unit
     *            the time unit
     * @return this option object
     */
    // These can only be used with a TopContainer with lifecycle...
    // Why???? Kan bruges med alle pods...
    // Container will be shutdown normally after the specified timeout

    // Vi vil gerne have en version, vi kan refreshe ogsaa???
    // Maaske vi bare ikke skal supportered det direkte.

    // Teknisk set er det vel en artifact wirelet
    // Det er vel en der kan bruges hver gang vi har en dynamisk wiring. Og ikke kun apps...
    // Men, f.eks. ogsaa actors...

    // allowForLink() returns false // check(WireletPosition) <- if (wp == link -> throw new X)

    static Wirelet timeToLive(long timeout, TimeUnit unit) {
        return timeToLive(Duration.of(timeout, unit.toChronoUnit()));
    }

    static Wirelet timeToLive(long timeout, TimeUnit unit, Supplier<Throwable> supplier) {
        timeToLive(10, TimeUnit.SECONDS, () -> new CancellationException());
        // Alternativ, kan man wrappe dem i f.eks. WiringOperation.requireExecutionMode();
        throw new UnsupportedOperationException();
    }
}
