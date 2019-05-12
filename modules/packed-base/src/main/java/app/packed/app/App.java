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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import app.packed.bundle.Bundle;
import app.packed.bundle.WiringOption;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.OnInitialize;
import packed.internal.app.InternalApp;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.buildtime.ContainerBuilder;

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
public interface App extends Injector, AutoCloseable {

    /** An alias for calling {@link #shutdown()} to support the {@link AutoCloseable} interface. **/
    @Override
    default void close() {
        shutdown();
    }

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
     * @see Bundle#setName(String)
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

    App start();

    /**
     * Initiates an asynchronously startup of the container. Normally, there is no need to call this methods since most
     * methods on the container will lazily start the container whenever it is needed. For example, invoking
     * {@link #use(Class)} will automatically start the container if it has not already been started by another action.
     * <p>
     * If the container is in the process of being initialized when invoking this method, for example, from a method
     * annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
     * constructed.
     * <p>
     * Invocation has no additional effect if the container has already been started or shut down.
     *
     * @return a future that can be used to query whether the container has completed startup or is still in the process of
     *         starting up. Can also be used to retrieve any exception that might have prevented the container in starting
     *         properly
     */
    CompletableFuture<App> startAsync();

    /**
     * Returns the state of application.
     * 
     * @return the state of application
     */
    LifecycleOperations<? extends App> state();

    /**
     * Creates a new container from the specified bundle. The state of the container when returned from this method is
     * initialized.
     *
     * @param bundle
     *            the bundle to create the container from
     * @param operations
     *            wiring operations
     * @return a new container
     * @throws RuntimeException
     *             if the container could not be created
     */
    static App of(Bundle bundle, WiringOption... operations) {
        requireNonNull(bundle, "bundle is null");
        ContainerBuilder builder = new ContainerBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF), bundle, operations);
        bundle.doConfigure(builder);
        return new InternalApp(builder.build());
    }

    static App of(Consumer<? super AppConfigurator> configurator, WiringOption... operations) {
        requireNonNull(configurator, "configurator is null");
        throw new UnsupportedOperationException();
    }

    static void run(Bundle bundle, WiringOption... operations) {
        // Hedder execute, for ikke at navnene minder for meget om hinanden (run + running)
        // Vi skal ogsaa
        throw new UnsupportedOperationException();
    }
}
