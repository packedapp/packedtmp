/*
 * Copyright (c) 2008 Kasper Nielsen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.packed.artifact;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.RunState;
import app.packed.service.Injector;

/**
 * An App (application) is a type of artifact is a program.
 * <p>
 * Applications are low overhead not using more then a few kilobytes.
 * <p>
 * You can have apps running apps runnings app.
 * 
 * You can easily have Hundreds of Thousands of applications running in the same JVM.
 */
// Maybe move to artifact.... App is the default artifact type...
public interface App extends AutoCloseable {

    /**
     * An alias for {@link #shutdown()} to support the {@link AutoCloseable} interface. This method has the exact same
     * semantics as {@link #shutdown()} and both can be used interchangeable.
     **/
    @Override
    default void close() {
        shutdown();
    }

    /**
     * Returns the configuration site of this app.
     * <p>
     * If this application was created from an {@link ArtifactImage image}, this method will return the site where the image
     * was created. Unless the AI.Wiring option is used when construction the application.
     * 
     * @return the configuration site of this app
     */
    ConfigSite configSite();

    /**
     * Returns the description of this application. Or an empty optional if no description has been set
     * <p>
     * The returned description is always identical to the description of the application's top container.
     *
     * @return the description of this application. Or an empty optional if no description has been set
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    Optional<String> description();

    // TODO dont know about this method... could use use(Injector.class) <- Injector.class is always the exported injector
    Injector injector();

    /**
     * Returns the name of this application.
     * <p>
     * The returned name is always identical to the name of the application's top container.
     * <p>
     * If no name is explicitly set when creating the application, the runtime will generate a name that guaranteed to be
     * unique among any of the application's siblings.
     * 
     * @return the name of this artifact
     */
    String name();

    /**
     * Returns the path of this application.
     * <p>
     * The returned path is always identical to the path of the application's top container.
     *
     * @return the component path of this application
     * @see Component#path()
     */
    ComponentPath path();

    App shutdown();// syntes sgu hellere man skal have shutdown().await(Terminated.class)

    /**
     * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
     * no additional effect if the application has already been shut down.
     *
     * @param cause
     *            the cause of the shutdown
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    App shutdown(Throwable cause);

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     * <p>
     * There are (currently) no method similar to {@link ExecutorService#shutdownNow()}.
     *
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    CompletableFuture<App> shutdownAsync();// syntes sgu hellere man skal have shutdown().await(Terminated.class)

    /**
     * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
     * no additional effect if the application has already been shut down.
     *
     * @param cause
     *            the cause of the shutdown
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    CompletableFuture<App> shutdownAsync(Throwable cause);

    /**
     * <p>
     * If the application has previous been started this method return immediately. already started
     * 
     * @return this application
     */
    App start();

    /**
     * Initiates an asynchronously startup of the application. Normally, there is no need to call this methods since most
     * methods on the container will lazily start the container whenever it is needed. For example, invoking
     * {@link #use(Class)} will automatically start the container if it has not already been started by another action.
     * <p>
     * If the container is in the process of being initialized when invoking this method, for example, from a method
     * annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
     * constructed.
     * <p>
     * Invocation has no additional effect if the container has already been started or shut down.
     *
     * @return a future that can be used to query whether the application has completed startup or is still in the process
     *         of starting up. Can also be used to retrieve any exception that might have prevented the container in
     *         starting properly
     */
    CompletableFuture<App> startAsync();

    /**
     * Returns the state of application.
     * 
     * @return the state of application
     */
    LifecycleOperations<? extends App> state();

    /**
     * Returns a component stream consisting of this applications underlying container and all of its descendants in any
     * order.
     * <p>
     * Calling this method does not effect the lifecycle state of this application.
     * 
     * @return a component stream
     */
    ComponentStream stream();

    ComponentStream stream(ComponentStream.Option... options);

    /**
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return stuff
     * @throws UnsupportedOperationException
     *             if no service with the specified key exist
     * @see Injector#use(Class)
     */
    <T> T use(Class<T> key);

    /**
     * <p>
     * This method takes a {@link CharSequence} so it is easy to passe either a {@link String} or a {@link ComponentPath}.
     * 
     * @param path
     *            the path of the component to return
     * @throws IllegalArgumentException
     *             if no component exists with the specified path
     * @return a component with the specified path
     */
    // TODO throw UnknownPathException();;
    Component useComponent(CharSequence path);

    /**
     * Creates a new application from the specified source. The state of the returned application is
     * {@link RunState#INITIALIZED}.
     *
     * @param source
     *            the source of the application
     * @param wirelets
     *            any wirelets to use in the construction of the application
     * @return a new application
     * @throws RuntimeException
     *             if the application could not be constructed properly
     */
    static App of(ContainerSource source, Wirelet... wirelets) {
        return AppArtifactDriver.INSTANCE.newArtifact(source, wirelets);
    }

    static App initialized(ContainerSource source, Wirelet... wirelets) {
        // Skal ogsaa matche metoder på hosts...
        return AppArtifactDriver.INSTANCE.newArtifact(source, wirelets);
    }

    static App starting(ContainerSource source, Wirelet... wirelets) {
        App app = AppArtifactDriver.INSTANCE.newArtifact(source, wirelets);
        app.startAsync();
        return app;
    }

    static App started(ContainerSource source, Wirelet... wirelets) {
        return AppArtifactDriver.INSTANCE.newArtifact(source, wirelets).start();
    }

    /**
     * This method will create and start an {@link App application} from the specified container source. Blocking until the
     * run state of the application is {@link RunState#TERMINATED}.
     * <p>
     * Entry point or run to termination
     * 
     * @param source
     *            the source of the application
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     */
    static void run(ContainerSource source, Wirelet... wirelets) {
        ((PackedApp) AppArtifactDriver.INSTANCE.newArtifact(source, wirelets)).execute();
    }

    // static void runThrowing(AnyBundle bundle, Wirelet... wirelets) throws Throwable
    // Basalt set har vi vel bare en Wiring property der angiver det
    // Basically we unwrap exceptions accordingly to some scheme in some way
}

/** An artifact driver for creating {@link App} instances. */
final class AppArtifactDriver extends ArtifactDriver<App> {

    /** The single instance. */
    static final AppArtifactDriver INSTANCE = new AppArtifactDriver();

    /** Singleton */
    private AppArtifactDriver() {}

    /** {@inheritDoc} */
    @Override
    public App instantiate(ArtifactContext container) {
        return new PackedApp(container);
    }
}

/** The default implementation of {@link App}, basically just wrapping a context object. */
final class PackedApp implements App {

    /** The artifact context we are wrapping. */
    private final ArtifactContext context;

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
    public ConfigSite configSite() {
        return context.configSite();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<String> description() {
        return context.description();
    }

    public void execute() {
        start();
        runMainSync();
        // try {
        // app.state().await(RunState.TERMINATED);
        // } catch (InterruptedException e) {
        // throw new RuntimeException(e);
        // }

    }

    /** {@inheritDoc} */
    @Override
    public Injector injector() {
        return context.injector();
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

    /**
     * 
     */
    public void runMainSync() {
        // TODO Auto-generated method stub
    }

    /** {@inheritDoc} */
    @Override
    public App shutdown() {
        // throw new UnsupportedOperationException();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public App shutdown(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> shutdownAsync() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> shutdownAsync(Throwable cause) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public App start() {
        // throw new UnsupportedOperationException();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public CompletableFuture<App> startAsync() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public LifecycleOperations<? extends App> state() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream() {
        return context.stream();
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Class<T> key) {
        return context.use(key);
    }

    /** {@inheritDoc} */
    @Override
    public Component useComponent(CharSequence path) {
        return context.useComponent(path);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentStream stream(Option... options) {
        return context.stream(options);
    }
}
