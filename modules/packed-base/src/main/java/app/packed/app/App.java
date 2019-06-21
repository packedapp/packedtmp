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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentStream;
import app.packed.config.ConfigSite;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.OnInitialize;
import app.packed.lifecycle.RunState;
import packed.internal.container.ContainerFactory;

/**
 * A application is a program.
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
// Injector injector() vs extends Injector
// Apps

// Do we expose the attachments????

// Container wrapper...Hmm A descriptor as well?
public interface App extends Injector, AutoCloseable {

    /** An alias for {@link #shutdown()} to support the {@link AutoCloseable} interface. **/
    @Override
    default void close() {
        shutdown();
    }

    /**
     * Returns the configuration site of this application.
     * 
     * @return the configuration site of this application
     */
    @Override
    ConfigSite configurationSite();

    /**
     * Returns the description of this application. Or an empty optional if no description has been set
     *
     * @return the description of this application. Or an empty optional if no description has been set
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    @Override
    Optional<String> description();

    /**
     * Returns the name of this application.
     * <p>
     * The name is always identical to the name of the top level container in the application.
     * <p>
     * If no name is explicitly set when configuring the application, the runtime will generate a (on a best-effort basis)
     * unique name.
     *
     * @return the name of this application
     * @see ContainerConfiguration#setName(String)
     */
    String name();

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
     * Creates a new application from the specified container source. The state of the returned application will be
     * initialized.
     *
     * @param source
     *            the source that creates the container that should be wrapped.
     * @param wirelets
     *            wiring operations
     * @return a new application
     * @throws RuntimeException
     *             if the application could not be constructed or initialized properly
     */
    static App of(ContainerSource source, Wirelet... wirelets) {
        return ContainerFactory.appOf(source, wirelets);
    }

    /**
     * This method will create and start an {@link App application} from the container source. Blocking until the
     * application has fully terminated.
     * 
     * @param source
     *            the source that creates the container that should be wrapped.
     * @param wirelets
     *            wirelets
     * @throws RuntimeException
     *             if the application did not execute properly
     */
    static void run(ContainerSource source, Wirelet... wirelets) {
        // CTRL-C ?? Obvious a wirelet, but default on or default off.
        // Paa Bundle syntes jeg den er paa, men ikke her...

        // If has @Main will run Main and then exit
        // Otherwise will run until application is shutdown

        try (App app = of(source, wirelets)) {
            app.start();
            try {
                app.state().await(RunState.TERMINATED);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
    // static void runThrowing(AnyBundle bundle, Wirelet... wirelets) throws Throwable
    // Basically we unwrap exceptions accordingly to some scheme in some way

    // If you can stop an app via shutdown. You should also be able to introspec it

    /**
     * Calling this method does not effect the lifecycle state of this application.
     * 
     * @return a component stream
     */
    ComponentStream stream();

    Component useComponent(CharSequence path);
}
