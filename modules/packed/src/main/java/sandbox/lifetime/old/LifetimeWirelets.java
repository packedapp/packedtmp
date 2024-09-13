package sandbox.lifetime.old;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import app.packed.container.Wirelet;
import app.packed.runtime.StopOption;

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
public final class LifetimeWirelets {
    private LifetimeWirelets() {}

    // after which the lifetime will be shutdown using the specified options
    public static Wirelet deadline(Instant deadline, StopOption... options) {
        // Shuts down container normally
        throw new UnsupportedOperationException();
    }

    /**
     * Returns
     *
     * @return the wirelet
     */
    // Requires Managed Root Application, will spawn a new daemon? thread not controlled by Packed
    // Hmm.... Hvad hvis man er et job... Saa er det jo mere cancel end det er shutdown...
    // Er ikke sikker paa vi vil have den her..
    // Requires Managed...
    // Er det et slags entry point? Nej vi spawner en traad
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
