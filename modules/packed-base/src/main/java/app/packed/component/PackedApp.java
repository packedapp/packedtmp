package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.container.Container;
import app.packed.inject.ServiceLocator;

/** The default implementation of {@link App}. */
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), PackedApp.class);

    /** The system component. */
    private final Component component;

    /** The guest that manages the lifecycle. */
    private final Container guest;

    /** All services that are available for the user. */
    private final ServiceLocator services;

    /**
     * Creates a new app.
     * 
     * @param component
     *            the service component
     * @param services
     *            the exported services
     * @param guest
     *            the guest
     */
    private PackedApp(Component component, ServiceLocator services, Container guest) {
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
    public Container container() {
        return guest;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceLocator services() {
        return services;
    }

    @Override
    public String toString() {
        return "App[" + guest.start() + "] " + path();
    }
}
