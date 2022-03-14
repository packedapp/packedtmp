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

import app.packed.application.App;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;

/**
 * Wirelets that can be used when wiring containers. For example, via {@link App#run(Assembly, Wirelet...)}.
 * <p>
 */

//Runables -> InVirtualThread

// Take a ThreadBuilder???

// Methods for adding timestamps/durations on states
// Det er jo en slags state machine historik...
// Maaske noget generisks???

// GuestWirelets????  
// StateWirelets??
// RunStateWirelets???
// HostWirelet
public interface LifetimeWirelets {


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

    static Wirelet executeInThread() {
        throw new UnsupportedOperationException();
    }

    // Den har et andet navn en CTRL_C...
    // shutdownHook() -> Adds a shutdown directly I think...

    // Vi skal lige sikre os at det ikke bliver installeret
    // Foerend vi er fuldt initialiseret. Da vi ikke supportere
    // Fremmede traade der kalder ind paa os naar vi bygger.

    // * The returned application will lazily start itself when needed. For example, on first invocation of
    // * {@link #use(Class)}.
    static Wirelet lazyStart() {
        throw new UnsupportedOperationException();
    }

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

    // Runnig on the initializaiob/starting/stopping thread

    static Wirelet runOn(RunState state, Runnable task) {
        throw new UnsupportedOperationException();
    }

    static class ShutdownHookWirelet extends Wirelet {}

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


//    private static Wirelet timeToLive(long timeout, TimeUnit unit, Supplier<Throwable> supplier) {
//        timeToLive(10, TimeUnit.SECONDS, GuestStopOption.erroneous(() -> new CancellationException()));
//        // Alternativ, kan man wrappe dem i f.eks. WiringOperation.requireExecutionMode();
//        throw new UnsupportedOperationException();
//    }
}
//Friday, 28 August 2020
//18:36
//
//The default is to only construct the first guest of a system...
//Same with start. Only the 1st guest...
//
//
//Unless 
//GuestWirelet.initializeWithParent();
//GuestWirelet.initializeWithParentAsync();
//GuestWirelet.startWithParent()
//GuestWirelet.startWithParentAsync();  <--- Async start these 25 guests...
