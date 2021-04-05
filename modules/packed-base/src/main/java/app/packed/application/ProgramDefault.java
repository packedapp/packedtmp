package app.packed.application;

import java.lang.invoke.MethodHandles;

import app.packed.component.Component;
import app.packed.inject.ServiceLocator;
import app.packed.state.RunState;

/** The default implementation of {@link Program}. */
record ProgramDefault(Component component, ServiceLocator services, ApplicationRuntime runtime) implements Program {

    /** An driver for creating App instances. */
    static final ApplicationDriver<Program> DRIVER = ApplicationDriver.builder().launchMode(RunState.RUNNING).build(MethodHandles.lookup(), ProgramDefault.class);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[state = " + runtime.state() + "] " + component.path();
    }
}
