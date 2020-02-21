package packed.internal.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.artifact.App;
import app.packed.artifact.ArtifactContext;
import app.packed.artifact.ArtifactDriver;
import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.StopOption;

/** The default implementation of {@link App}, basically just wrapping a context object. */
public final class PackedApp implements App {

    /** An artifact driver for creating {@link App} instances. */
    public static final ArtifactDriver<App> DRIVER = new ArtifactDriver<>(true) {

        /** {@inheritDoc} */
        @Override
        public App newArtifact(ArtifactContext container) {
            return new PackedApp(container);
        }
    };

    /** The artifact context we are wrapping. */
    final ArtifactContext context;

    /**
     * Creates a new app.
     * 
     * @param context
     *            the artifact runtime context we are wrapping
     */
    PackedApp(ArtifactContext context) {
        this.context = requireNonNull(context);
    }

    /** {@inheritDoc} */
    @Override
    public void close() {
        context.stop();
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

    public void execute() {
        context.execute();
    }

    public <T> CompletableFuture<T> startAsync(T result) {
        return context.startAsync(result);
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

/// ** An artifact driver for creating {@link App} instances. */
// final class AppArtifactDriver extends ArtifactDriver<App> {
//
// /** Singleton */
// AppArtifactDriver() {}
//
// /** {@inheritDoc} */
// @Override
// public App newArtifact(ArtifactContext container) {
// return new PackedApp(container);
// }
// }
// static void runThrowing(AnyBundle bundle, Wirelet... wirelets) throws Throwable
// Basalt set har vi vel bare en Wiring property der angiver det
// Basically we unwrap exceptions accordingly to some scheme in some way

// /**
// * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
// * no additional effect if the application has already been shut down.
// *
// * @param cause
// * the cause of the shutdown
// * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
// * in the process of being shut down
// */
// App close(Throwable cause);

// /**
// * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
// * no additional effect if the application has already been shut down.
// *
// * @param cause
// * the cause of the shutdown
// * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
// * in the process of being shut down
// */
// CompletableFuture<App> closeAsync(Throwable cause);

// TODO dont know about this method... could use use(Injector.class) <- Injector.class is always the exported injector
// Injector injector();

// // Only use #close();
// /// You close the app... That makes total sense.
// // Rename of to Open....
// //// Also those, starting, stopping,... I think that is wirelets....
// App shutdown();// syntes sgu hellere man skal have shutdown().await(Terminated.class)

// /**
// * <p>
// * If the application has previous been started this method return immediately. already started
// *
// * @return this application
// */
// App start();
//
// /**
// * Initiates an asynchronously startup of the application. Normally, there is no need to call this methods since most
// * methods on the container will lazily start the container whenever it is needed. For example, invoking
// * {@link #use(Class)} will automatically start the container if it has not already been started by another action.
// * <p>
// * If the container is in the process of being initialized when invoking this method, for example, from a method
// * annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
// * constructed.
// * <p>
// * Invocation has no additional effect if the container has already been started or shut down.
// *
// * @return a future that can be used to query whether the application has completed startup or is still in the process
// * of starting up. Can also be used to retrieve any exception that might have prevented the container in
// * starting properly
// */
