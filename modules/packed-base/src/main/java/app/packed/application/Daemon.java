package app.packed.application;

import app.packed.cli.CliWirelets;
import app.packed.component.Assembly;
import app.packed.component.Wirelet;
import app.packed.state.Host;

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
     * @see Host#stop(Host.StopOption...)
     **/
    @Override
    default void close() {
        host().stop();
    }

    /**
     * Returns the applications's host.
     * 
     * @return this application's host.
     */
    Host host(); // giver ikke mening 

    void stop();

    void stopAsync();

    static Daemon run(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        return run(assembly, CliWirelets.args(args).andThen(wirelets));
    }

    static Daemon run(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Starts async... Builds/initializes sync
    static Daemon runAsync(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
       return run(assembly, CliWirelets.args(args).andThen(wirelets));
    }

    static Daemon runAsync(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Daemon start(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        return start(assembly, CliWirelets.args(args).andThen(wirelets));
    }

    static Daemon start(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    static Daemon startAsync(Assembly<?> assembly, String[] args, Wirelet... wirelets) {
        return startAsync(assembly, CliWirelets.args(args).andThen(wirelets));
    }

    static Daemon startAsync(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
