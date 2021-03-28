package app.packed.application;

import app.packed.component.Wirelet;
import app.packed.state.RunState;

public final class ApplicationWirelets {
    private ApplicationWirelets() {}

    /**
     * Create a new wirelet that will set the launch mode of the application.
     * 
     * @param launchMode
     *            the launchMode of the application
     * @return the new wirelet
     */
    public static Wirelet launchMode(RunState launchMode) {
        throw new UnsupportedOperationException();
    }
}
