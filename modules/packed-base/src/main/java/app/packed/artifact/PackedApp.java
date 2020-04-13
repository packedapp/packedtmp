package app.packed.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.StopOption;

/** The default implementation of {@link App}. */
final class PackedApp implements App {

    /** An artifact driver for creating {@link App} instances. */
    static final ArtifactDriver<App> DRIVER = new ArtifactDriver<>() {

        /** {@inheritDoc} */
        @Override
        public App newArtifact(ArtifactContext context) {
            return new PackedApp(context);
        }
    };

    /** The artifact context we are wrapping. */
    private final ArtifactContext context;

    /**
     * Creates a new app.
     * 
     * @param context
     *            the context to wrap
     */
    private PackedApp(ArtifactContext context) {
        this.context = requireNonNull(context);
    }

    /** {@inheritDoc} */
    @Override
    public ConfigSite configSite() {
        return context.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return context.description();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return context.name();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath path() {
        return context.path();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends App> state() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App stop(StopOption... options) {
        context.stop(options);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> stopAsync(StopOption... options) {
        return context.stopAsync(this, options);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return context.stream();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(Option... options) {
        return context.stream(options);
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        return context.use(key);
    }

    /** {@inheritDoc} */
    @Override
    public Component useComponent(CharSequence path) {
        return context.useComponent(path);
    }
}
