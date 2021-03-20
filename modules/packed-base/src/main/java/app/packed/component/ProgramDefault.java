package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.inject.ServiceLocator;
import app.packed.state.Host;

/** The default implementation of {@link Program}. */
record ProgramDefault(Component component, ServiceLocator services, Host host) implements Program {

    /** An driver for creating App instances. */
    static final ApplicationDriver<Program> DRIVER = ApplicationDriver.of(MethodHandles.lookup(), ProgramDefault.class);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[state = " + host.state() + "] " + component.path();
    }
}
