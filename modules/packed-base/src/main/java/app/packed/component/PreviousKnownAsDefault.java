package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.inject.ServiceLocator;
import app.packed.state.Host;

/** The default implementation of {@link PreviousKnownAsApp}. */
record PreviousKnownAsDefault(Component component, ServiceLocator services, Host host) implements PreviousKnownAsApp {

    /** An driver for creating App instances. */
    static final ApplicationDriver<PreviousKnownAsApp> DRIVER = ApplicationDriver.of(MethodHandles.lookup(), PreviousKnownAsDefault.class);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[state = " + host.state() + "] " + component.path();
    }
}
