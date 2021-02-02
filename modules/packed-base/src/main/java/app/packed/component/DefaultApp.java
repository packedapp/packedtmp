package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.component.drivers.ArtifactDriver;
import app.packed.inject.ServiceLocator;
import app.packed.state.Host;

/** The default implementation of {@link App}. */
final class DefaultApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ArtifactDriver<App> DRIVER = ArtifactDriver.of(MethodHandles.lookup(), DefaultApp.class);

    /** The app's root component. */
    private final Component component;

    /** The app's host. */
    private final Host host;

    /** The app's service locator. */
    private final ServiceLocator services;

    /**
     * Creates a new app.
     * 
     * @param component
     *            the app's root component
     * @param services
     *            the app's service locator
     * @param host
     *            the app's host
     */
    private DefaultApp(Component component, ServiceLocator services, Host host) {
        this.component = requireNonNull(component);
        this.host = requireNonNull(host);
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public Component component() {
        return component;
    }

    /** {@inheritDoc} */
    @Override
    public Host host() {
        return host;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceLocator services() {
        return services;
    }

    @Override
    public String toString() {
        return "App[state = " + host.state() + "] " + component.path();
    }
}
