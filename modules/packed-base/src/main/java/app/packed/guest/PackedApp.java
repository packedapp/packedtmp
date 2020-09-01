package app.packed.guest;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.component.ShellDriver;
import app.packed.config.ConfigSite;
import app.packed.service.ServiceRegistry;

/** The default implementation of {@link App}. */
// Don't know if want an interface at all.
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), App.class, PackedApp.class);

    /** The system component. */
    private final Component component;

    /** The guest that manages the lifecycle. */
    private final Guest guest;

    // TODO I think we need to create a lazy starting service registry...
    // or at least with findInstance/findProvider
    /** All services that are available for the user. */
    private final ServiceRegistry services;

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
    private PackedApp(Component component, ServiceRegistry services, Guest guest) {
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
    public ConfigSite configSite() {
        return component.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Component resolve(CharSequence path) {
        return component.resolve(path);
    }

    /** {@inheritDoc} */
    @Override
    public App stop(GuestStopOption... options) {
        guest.stop(options);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> stopAsync(GuestStopOption... options) {
        return guest.stopAsync(this, options);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return component.stream();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(Option... options) {
        return component.stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Class<T> key) {
        return services.use(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        return services.use(key);
    }

    @Override
    public String toString() {
        return "App " + path() + " Running";
    }
}
