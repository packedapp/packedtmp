package app.packed.component;

import java.lang.invoke.MethodHandles;

import app.packed.component.drivers.ArtifactDriver;
import app.packed.inject.ServiceLocator;
import app.packed.state.Host;

/** The default implementation of {@link App}. */
record AppDefault(Component component, ServiceLocator services, Host host) implements App {

    /** An driver for creating App instances. */
    static final ArtifactDriver<App> DRIVER = ArtifactDriver.of(MethodHandles.lookup(), AppDefault.class);

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[state = " + host.state() + "] " + component.path();
    }
}
