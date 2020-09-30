package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.guest.Guest;
import app.packed.service.ServiceLocator;

/** The default implementation of {@link App}. */
// Er bare en inline record...Vi kan ignore nulls, da Packed instantiere den
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), PackedApp.class);

    /** The system component. */
    private final Component component;

    /** The guest that manages the lifecycle. */
    private final Guest guest;

    /** All services that are available for the user. */
    private final ServiceLocator serviceRegistry;

    /**
     * Creates a new app.
     * 
     * @param component
     *            the service component
     * @param services
     *            the available services
     * @param guest
     *            the guest
     */
    private PackedApp(Component component, ServiceLocator services, Guest guest) {
        this.component = requireNonNull(component);
        this.guest = requireNonNull(guest);
        this.serviceRegistry = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @Override
    public Component component() {
        return component;
    }

    /** {@inheritDoc} */
    @Override
    public Guest guest() {
        return guest;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceLocator services() {
        return serviceRegistry;
    }

    @Override
    public String toString() {
        return "App[" + guest.start() + "] " + path();
    }
}
