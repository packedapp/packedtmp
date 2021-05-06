package app.packed.application;

import java.util.function.Consumer;

import app.packed.state.sandbox.InstanceState;

// root application
// will automatically have a shutdown hook installed
// will have a unique name

// PlatformHost?
public interface PlatformApplication {

    /**
     * Returns the name of the application.
     * <p>
     * Every platform application has a unique name.
     * 
     * @return
     */
    String name();

    InstanceState state(); // the current state

    public static void forEach(Consumer<? super PlatformApplication> action) {

    }
}
// Det er jo saadan set en stor host der er gemt i et statisk field...
// Saa alle de der guest - linger settings kan vel blive anvendt.