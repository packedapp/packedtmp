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

    /** The app's container. */
    private final Container container;

    /** The app's service locator. */
    private final ServiceLocator services;

    /**
     * Creates a new app.
     * 
     * @param component
     *            the service component
     * @param services
     *            the exported services
     * @param container
     *            the container
     */
    private PackedApp(Component component, ServiceLocator services, Container container) {
        this.component = requireNonNull(component);
        this.container = requireNonNull(container);
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
        return container;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceLocator services() {
        return services;
    }

    @Override
    public String toString() {
        return "App[state = " + container.state() + "] " + path();
    }
}
