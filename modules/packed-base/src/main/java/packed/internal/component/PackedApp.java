package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;

import app.packed.component.App;
import app.packed.component.Component;
import app.packed.component.ShellDriver;
import app.packed.guest.Guest;
import app.packed.inject.ServiceLocator;

/** The default implementation of {@link App}. */
// Could be a really nice little record this one.
public final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    public static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), PackedApp.class);

    /** The system component. */
    private final Component component;

    /** The guest that manages the lifecycle. */
    private final Guest guest;

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
    private PackedApp(Component component, ServiceLocator services, Guest guest) {
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
    public Guest guest() {
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
