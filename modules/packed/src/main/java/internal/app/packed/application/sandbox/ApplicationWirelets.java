package internal.app.packed.application.sandbox;

import java.nio.channels.AcceptPendingException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import app.packed.container.Wirelet;
import app.packed.lifetime.RunState;
import app.packed.lifetime.sandbox.StopOption;

/**
 * Application runtime wirelet that can be specified when building or launching an application that includes the
 * {@link AcceptPendingException}. Attempt to use these wirelets
 * <p>
 * Attempting to use any of the wirelets on this class on an application that does not. Attempts to use it with a
 * non-runnable application will fail with
 * 
 */
///// ContainerWirelets??? RuntimeWirelets?? ApplicationWirelets, Was ExecutionWirelets

// Application og Build har jo meget med hinanden at goere. Eftersom Application er "the minimal buildable unit"
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
    // Requires Managed...
    // Er det et slags entry point?
    static Wirelet enterToStop() {

        // Den fungere kun med Terminate eller Stop mode...
        // Og kun in MainThreadOfControl

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

//    /**
//     * Returns a wirelet that will override the default launch mode of an application.
//     * <p>
//     * If multiple launch mode wirelets are specified, the last applied wirelet is chosen.
//     * 
//     * @param launchMode
//     *            the launchMode of the application
//     * @return the launch mode wirelet
//     */
//    // Hvad med ServiceLocator, vi skal vel skrive noget om at nogle launch modes ikke er supporteret
//    // Taenker vi kun kan bruge den ved build mode.
//    // Altsaa ved ikke om den skal vaere en wirelet
//    public static Wirelet launchMode(ApplicationLaunchMode launchMode) {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Returns a wirelet that will install a shutdown hook.
     * <p>
     * As shutting down the root will automatically shutdown all of its child applications. Attempting to specify a shutdown
     * hook wirelet when launching a non-root application will fail with an exception.
     * <p>
     * 
     * @return a shutdown hook wirelet
     * @see #shutdownHook(Function, app.packed.lifetime.sandbox.StopOption...)
     * @see Runtime#addShutdownHook(Thread)
     */
    // cannot specify it on ServiceLocator or anyone else that is not closeable
    // Ogsaa skrive noget om hvad der sker hvis vi stopper
    // Skriv noget om der bliver lavet en traad, og man kan bruge den anden metode hvis man selv skal lave en

    // Maaske skal vi ogsaa exponere en installShutdownHook(ContainerConfiguration?)
    // Maaske har vi en PlatformAssembly???
    public static Wirelet shutdownHook(StopOption... options) {
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
    // Fail if unmanaged???? Or just ignore?
    // I would think failed... Maybe Wirelets.ifManagedApplication(Wirelet w); only apply the wirelet if a managed application
    public static Wirelet shutdownHook(Function<Runnable, Thread> threadFactory, StopOption... options) {
        throw new UnsupportedOperationException();
        // return new ShutdownHookWirelet();
    }

    // excludes start?? IDK
    // Altsaa det giver jo mening at faa en total tid taenker jeg
    // Omvendt tror jeg ikke det giver mening at tage build time med...
    //

    // timeToStart() If the application does not start within the X time
    // Shut it down

    /**
     * 
     * @param duration
     *            the duration
     * @param options
     * @return
     */
    public static Wirelet timeToRun(Duration duration, StopOption... options) {
        // can make a timeToLive() <-- which includes start
        // timeTo(Initialize, Running) (from, to)
        // (Running, Running)
        // Or maybe we should have a boolean, includeTimeToStart, includeTimeToInitize
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
    public static Wirelet timeToRun(long timeout, TimeUnit unit, StopOption... options) {
        return timeToRun(Duration.of(timeout, unit.toChronoUnit()), options);
    }
}
