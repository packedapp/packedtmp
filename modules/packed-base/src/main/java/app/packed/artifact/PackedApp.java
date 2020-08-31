package app.packed.artifact;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.guest.Guest;
import app.packed.lifecycleold.LifecycleOperations;
import app.packed.lifecycleold.StopOption;
import app.packed.service.ServiceRegistry;

/** The default implementation of {@link App}. */
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), App.class, PackedApp.class);

    /** The system component. */
    private final Component component;

    private final Guest guest;

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
    public LifecycleOperations<? extends App> state() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App stop(StopOption... options) {
        guest.stop(options);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> stopAsync(StopOption... options) {
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
}
