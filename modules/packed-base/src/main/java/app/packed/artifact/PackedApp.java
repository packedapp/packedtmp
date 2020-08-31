package app.packed.artifact;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.lifecycleold.LifecycleOperations;
import app.packed.lifecycleold.StopOption;

/** The default implementation of {@link App}. */
final class PackedApp implements App {

    /** An driver for creating PackedApp instances. */
    static final ShellDriver<App> DRIVER = ShellDriver.of(MethodHandles.lookup(), App.class, PackedApp.class);

    /** The artifact context we are wrapping. */
    private final ShellContext context;

    /**
     * Creates a new app.
     * 
     * @param context
     *            the context to wrap
     */
    private PackedApp(ShellContext context) {
        this.context = requireNonNull(context);
    }

    /** {@inheritDoc} */
    @Override
    public Component component() {
        return context.component();
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return context.component().configSite();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends App> state() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App stop(StopOption... options) {
        context.guest().stop(options);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> stopAsync(StopOption... options) {
        return context.guest().stopAsync(this, options);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return context.component().stream();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(Option... options) {
        return context.component().stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Class<T> key) {
        return context.services().use(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        return context.services().use(key);
    }

    /** {@inheritDoc} */
    @Override
    public Component resolve(CharSequence path) {
        return context.component().resolve(path);
    }
}
