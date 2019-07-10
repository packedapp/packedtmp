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
package app.packed.app;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import app.packed.component.Component;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.container.Artifact;
import app.packed.container.ArtifactDriver;
import app.packed.container.ArtifactSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.RunState;
import packed.internal.container.PackedApp;
import packed.internal.container.PackedContainerContext;

/**
 * An App (application) is a type of artifact is a program.
 * <p>
 * Applications are low overhead not using more then a few kilobytes.
 * <p>
 * You can have apps running apps runnings app.
 * 
 * 
 * 
 * You can easily have Hundreds of Thousands of applications running in the same JVM.
 * 
 */
// Do we expose the attachments????
public interface App extends Artifact, AutoCloseable {

    /**
     * Returns the component path of this app.
     * <p>
     * The returned path is always identical to the path of the app's root container.
     *
     * @return the component path of this app
     * @see Component#path()
     */
    ComponentPath path();

    /**
     * An alias for {@link #shutdown()} to support the {@link AutoCloseable} interface. This method has the exact same
     * semantics as {@link #shutdown()} and can be used interchangeable.
     **/
    @Override
    default void close() {
        shutdown();
    }

    // TODO dont know about this method... could use use(Injector.class) <- Injector.class is always the exported injector
    Injector injector();

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
    static App of(ArtifactSource source, Wirelet... wirelets) {
        return AppArtifactDriver.INSTANCE.create(source, wirelets);
    }

    /**
     * This method will create and start an {@link App application} from the specified container source. Blocking until the
     * run state of the application is {@link RunState#TERMINATED}.
     * 
     * @param source
     *            the source of the application
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     */
    static void run(ArtifactSource source, Wirelet... wirelets) {
        // CTRL-C ?? Obvious a wirelet, but default on or default off.
        // Paa Bundle syntes jeg den er paa, men ikke her...

        // If has @Main will run Main and then exit
        // Otherwise will run until application is shutdown

        // If you can stop an app via shutdown. You should also be able to introspec it

        try (PackedApp app = AppArtifactDriver.INSTANCE.create(source, wirelets)) {
            app.start();
            app.runMainSync();
            // try {
            // app.state().await(RunState.TERMINATED);
            // } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            // }
        }
    }

    // static void runThrowing(AnyBundle bundle, Wirelet... wirelets) throws Throwable
    // Basalt set har vi vel bare en Wiring property der angiver det
    // Basically we unwrap exceptions accordingly to some scheme in some way
}

class AppArtifactDriver extends ArtifactDriver<PackedApp> {

    /** The single instance. */
    static final AppArtifactDriver INSTANCE = new AppArtifactDriver();

    /** Singleton */
    private AppArtifactDriver() {}

    /** {@inheritDoc} */
    @Override
    public PackedApp newArtifact(PackedContainerContext container) {
        return new PackedApp(container);
    }
}
