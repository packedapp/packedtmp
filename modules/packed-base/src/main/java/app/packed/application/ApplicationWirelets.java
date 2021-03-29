package app.packed.application;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import app.packed.application.ApplicationRuntime.StopOption;
import app.packed.component.Wirelet;
import app.packed.state.RunState;
import app.packed.state.StateWirelets.ShutdownHookWirelet;

// Er maaske lidt mere runtime wirelets... Eller Lifetime wirelets...
public final class ApplicationWirelets {
    private ApplicationWirelets() {}

    // after which the guest will be shutdown normally
    static Wirelet deadline(Instant deadline, StopOption... options) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    /**
     * Returns
     * 
     * @return
     */
    public static Wirelet enterToStop() {
        // https://github.com/patriknw/akka-typed-blog/blob/master/src/main/java/blog/typed/javadsl/ImmutableRoundRobinApp.java
//        ActorSystem<Void> system = ActorSystem.create(root, "RoundRobin");
//        try {
//          System.out.println("Press ENTER to exit [the application/job/program/...]");
//          System.in.read();
//        } finally {
//          system.terminate();
//        }
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new wirelet that will control the launch mode of the application overriding any default values.
     * 
     * <p>
     * If more than one launch mode wirelet is applied the last applied wirelet is chosen.
     * 
     * @param launchMode
     *            the launchMode of the application
     * @return the new wirelet
     */
    public static Wirelet launchMode(RunState launchMode) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will install a shutdown hook.
     *
     * <p>
     * <p>
     * As shutting down the root will automatically shutdown all of its child applications. Attempting to specify a shutdown
     * hook wirelet when launching a non-root application will fail with {@link IllegalArgumentException}.
     * 
     * @return a shutdown hook wirelet
     * @see #shutdownHook(Function, app.packed.application.ApplicationRuntime.StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     */
    public static Wirelet shutdownHook(ApplicationRuntime.StopOption... options) {
        // https://www.baeldung.com/spring-boot-shutdown
        return shutdownHook(r -> new Thread(r), options);
    }

    /**
     * @param threadFactory
     * @param options
     * @return a shutdown hook wirelet
     * @see Runtime#addShutdownHook(Thread)
     */
    public static Wirelet shutdownHook(Function<Runnable, Thread> threadFactory, ApplicationRuntime.StopOption... options) {
        return new ShutdownHookWirelet();
    }

    // excludes start?? IDK
    public static Wirelet timeToRun(Duration duration, ApplicationRuntime.StopOption... options) {
        // can make a timeToLive() <-- which includes start
        throw new UnsupportedOperationException();
    }

    /**
     * As measured from once the application reaches the {@link RunState#RUNNING} phase.
     * 
     * @param timeout
     * @param unit
     * @param options
     *            stop options that will applied when stopping the runtime
     * @return the new wirelet
     */
    public static Wirelet timeToRun(long timeout, TimeUnit unit, ApplicationRuntime.StopOption... options) {
        return timeToRun(Duration.of(timeout, unit.toChronoUnit()), options);
    }
}
