package app.packed.application;

import app.packed.cli.CliWirelets;
import app.packed.container.Wirelet;

public interface ApplicationLauncher<A> {

    /**
     * Launches an application.
     * 
     * @return the application interface
     */
    default A launch() {
        return launch(new Wirelet[] {});
    }

    default A launch(String[] args, Wirelet... wirelets) {
        return launch(CliWirelets.args(args).andThen(wirelets));
    }

    /**
     * Launches an application.
     * 
     * @param wirelets
     *            optional wirelets
     * @return the application interface
     */
    A launch(Wirelet... wirelets);
}
