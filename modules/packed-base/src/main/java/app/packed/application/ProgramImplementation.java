package app.packed.application;

import java.lang.invoke.MethodHandles;

import app.packed.inject.ServiceLocator;
import app.packed.state.RunState;

/** The default implementation of {@link Program}. */
record ProgramImplementation(String name, ServiceLocator services, ApplicationRuntime runtime) implements Program {

    /** An driver for creating App instances. */
    static final ApplicationDriver<Program> DRIVER = ApplicationDriver.builder().launchMode(RunState.RUNNING).build(MethodHandles.lookup(), ProgramImplementation.class);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[name = " + name() +", state = " + runtime.state() + "] ";
    }
}
