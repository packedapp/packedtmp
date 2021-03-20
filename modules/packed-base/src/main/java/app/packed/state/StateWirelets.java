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
package app.packed.state;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import app.packed.component.Program;
import app.packed.component.Assembly;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.state.Host.StopOption;

/**
 * Wirelets that can be used when wiring containers. For example, via {@link Program#start(Assembly, Wirelet...)}.
 * <p>
 * All wirelets on this class requires the {@link ComponentModifier#CONTAINEROLD} modifier on the component being wired.
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
public interface StateWirelets {

    // after which the guest will be shutdown normally
    static Wirelet deadline(Instant deadline, StopOption... options) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    // Teankt som en lille hjaelper metode
    static Wirelet enterToStop() {
        // https://github.com/patriknw/akka-typed-blog/blob/master/src/main/java/blog/typed/javadsl/ImmutableRoundRobinApp.java
//        ActorSystem<Void> system = ActorSystem.create(root, "RoundRobin");
//        try {
//          System.out.println("Press ENTER to exit");
//          System.in.read();
//        } finally {
//          system.terminate();
//        }
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

    /**
     * Returns a wirelet that will install a shutdown hook.
     * 
     * @return a shutdown hook wirelet
     *
     * @see Runtime#addShutdownHook(Thread)
     */
    // StopOption.panic(()->throw new DooException());
    static Wirelet shutdownHook(Host.StopOption... options) {
        // https://www.baeldung.com/spring-boot-shutdown
        return shutdownHook(r -> new Thread(r), options);
    }

    /**
     * @param threadFactory
     * @param options
     * @return a shutdown hook wirelet
     * @see Runtime#addShutdownHook(Thread)
     */
    static Wirelet shutdownHook(Function<Runnable, Thread> threadFactory, Host.StopOption... options) {
        return new ShutdownHookWirelet();
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

    static Wirelet timeToRun(long timeout, TimeUnit unit, Host.StopOption... options) {
        return timeToRun(Duration.of(timeout, unit.toChronoUnit()), options);
    }

    // excludes start?? IDK
    static Wirelet timeToRun(Duration duration, Host.StopOption... options) {
        // can make a timeToLive() <-- which includes start
        throw new UnsupportedOperationException();
    }

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
