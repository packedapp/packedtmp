package app.packed.application;

import app.packed.application.ApplicationRuntime.StopOption;
import app.packed.bundle.Bundle;
import app.packed.bundle.Wirelet;

// Ideen er lige at vi laver en deamon..

// 3 parametere vi skruer på

// Skal vi runne den (no deamon) eller starte den
// Skal vi tage String args...
// sync eller async

// Restartable som default??? Det vil jeg mere.. Will restart on failure.
// Can be configured via RestartWirelets.restartAlways() (sgu ikke paa shutdown hook)

// StateWirelets.restartUnlessStopped() -> Kan overskrives af brugere..
// Hader ogsaa Daemon som en annotering engang... Var stadig en okay ide syntes jeg
public interface Daemon extends AutoCloseable {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code host().stop()}, but this method
     * is called close in order to support try-with resources via {@link AutoCloseable}.
     * 
     * @see ApplicationRuntime#stop(ApplicationRuntime.StopOption...)
     **/
    @Override
    default void close() {
        runtime().stop();
    }

    /**
     * Returns the applications's host.
     * 
     * @return this application's host.
     */
    ApplicationRuntime runtime(); // giver ikke mening

    default void stop(StopOption... options) {
        runtime().stop(options);
    }

    default void stopAsync(StopOption... options) {
        runtime().stop(options);
    }

    /** { @return the default application driver for creating daemons} */
    static ApplicationDriver<Daemon> driver() {
        throw new UnsupportedOperationException();
    }

    static Launcher launcher() {
        throw new UnsupportedOperationException();
    }

//    /**
//     * Creates a application mirror by building an application of the specified assembly and introspecting it.
//     * 
//     * @param assembly
//     *            the assembly containing the daemon definition
//     * @param wirelets
//     *            optional wirelets
//     * @return an application mirror for the daemon
//     */
//    static ApplicationMirror introspect(Assembly<?> assembly, Wirelet... wirelets) {
//        return ApplicationMirror.of(driver(), assembly, wirelets);
//    }

    /**
     * Creates a application mirror by building an application of the specified assembly and introspecting it.
     * 
     * @param assembly
     *            the assembly containing the daemon definition
     * @param wirelets
     *            optional wirelets
     * @return an application mirror for the daemon
     */
    static ApplicationMirror mirrorOf(Bundle<?> assembly, Wirelet... wirelets) {
        return driver().mirrorOf(assembly, wirelets);
    }

    // When do want to run a daemon???
    // Isn't it main...
    static Daemon run(Bundle<?> assembly, String[] args, Wirelet... wirelets) {
        return run(assembly, wirelets);
    }

    static Daemon run(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Starts async... Builds/initializes sync
    static Daemon runAsync(Bundle<?> assembly, String[] args, Wirelet... wirelets) {
        return run(assembly, wirelets);
    }

    static Daemon runAsync(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Daemon start(Bundle<?> assembly, String[] args, Wirelet... wirelets) {
        return start(assembly, wirelets);
    }

    static Daemon start(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Daemon startAsync(Bundle<?> assembly, String[] args, Wirelet... wirelets) {
        return startAsync(assembly, wirelets);
    }

    static Daemon startAsync(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    interface Launcher {
        default Launcher args(String... args) {
            return this;
        }

        Launcher neverRestart();

        DaemonImage newImage(Bundle<?> assembly);

        DaemonImage newImage(Bundle<?> assembly, Wirelet... wirelets);

        Launcher restartPolicy(Object somePolicy);

        Daemon start(Bundle<?> assembly);

        Daemon start(Bundle<?> assembly, Wirelet... wirelets);
    }
}
