package app.packed.application;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import app.packed.application.ApplicationRuntime.StopOption;
import app.packed.component.Wirelet;
import app.packed.state.RunState;
import app.packed.state.StateWirelets.ShutdownHookWirelet;

/**
 * Wirelets that can be specified when building or launching an application.
 */
//Er maaske lidt mere runtime wirelets... Eller Lifetime wirelets...
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
     * @return the wirelet
     */
    // Must have an execution phase
    // Hmm.... Hvad hvis man er et job... Saa er det jo mere cancel end det er shutdown...
    // Er ikke sikker paa vi vil have den her..
    static Wirelet enterToStop() {
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
     * Returns a wirelet that will override the default launch mode of an application.
     * <p>
     * If multiple launch mode wirelets are specified, the last applied wirelet is chosen.
     * 
     * @param launchMode
     *            the launchMode of the application
     * @return the launch mode wirelet
     */
    // Hvad med ServiceLocator, vi skal vel skrive noget om at nogle launch modes ikke er supporteret
    public static Wirelet launchMode(RunState launchMode) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will install a shutdown hook.
     * <p>
     * As shutting down the root will automatically shutdown all of its child applications. Attempting to specify a shutdown
     * hook wirelet when launching a non-root application will fail with an exception.
     * <p>
     * 
     * @return a shutdown hook wirelet
     * @see #shutdownHook(Function, app.packed.application.ApplicationRuntime.StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     */
    // cannot specify it on ServiceLocator
    // Ogsaa skrive noget om hvad der sker hvis vi stopper
    // Skriv noget om der bliver lavet en traad, og man kan bruge den anden metode hvis man selv skal lave en
    public static Wirelet shutdownHook(ApplicationRuntime.StopOption... options) {
        // https://www.baeldung.com/spring-boot-shutdown
        return shutdownHook(r -> new Thread(r), options);
    }

    /**
     * Installs a shutdown hook similar to {@link #shutdownHook(StopOption...)} but also
     * 
     * @param threadFactory
     *            a factory that is used to create
     * @param options
     *            stop options
     * @return a shutdown hook wirelet
     * @see Runtime#addShutdownHook(Thread)
     */
    public static Wirelet shutdownHook(Function<Runnable, Thread> threadFactory, ApplicationRuntime.StopOption... options) {
        return new ShutdownHookWirelet();
    }

    // excludes start?? IDK
    /**
     * <p>
     * This wirelet can only be used with runnable applications. Attempts to use it with a non-runnable application will
     * fail with {@link ApplicationNotRunnableException}.
     * 
     * @param duration
     *            the duration
     * @param options
     * @return
     * 
     */
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
