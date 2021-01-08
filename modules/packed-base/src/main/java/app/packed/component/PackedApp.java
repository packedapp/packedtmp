package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.inject.ServiceLocator;
import app.packed.state.Host;

/** The default implementation of {@link App}. */
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), PackedApp.class);

    /** The system component. */
    private final Component component;

    /** The app's container. */
    private final Host guest;

    /** The app's service locator. */
    private final ServiceLocator services;

    /**
     * Creates a new app.
     * 
     * @param component
     *            the service component
     * @param services
     *            the exported services
     * @param guest
     *            the container
     */
    private PackedApp(Component component, ServiceLocator services, Host guest) {
        this.component = requireNonNull(component);
        this.guest = requireNonNull(guest);
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
        return guest;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceLocator services() {
        return services;
    }

    @Override
    public String toString() {
        return "App[state = " + guest.state() + "] " + component.path();
    }
}
