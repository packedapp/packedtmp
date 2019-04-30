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
import java.util.function.Function;

import app.packed.bundle.Bundle;
import app.packed.bundle.OldWiringOperation;
import app.packed.container.Container;
import app.packed.inject.Injector;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.OnInitialize;
import packed.internal.config.site.ConfigurationSiteType;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.container.ContainerBuilder;

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
public interface App extends Injector {

    /**
     * Returns the name of this application.
     * <p>
     * If no name is explicitly set when configuring the application, the runtime will generate a (on a best-effort basis)
     * unique name.
     *
     * @return the name of this application
     */
    String name();

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     * <p>
     * There are (currently) no method similar to {@link ExecutorService#shutdownNow()}.
     *
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    CompletableFuture<App> shutdown();// syntes sgu hellere man skal have shutdown().await(Terminated.class)

    /**
     * Initiates an orderly asynchronously shutdown of the application because of an exceptional condition. Invocation has
     * no additional effect if the application has already been shut down.
     *
     * @param cause
     *            the cause of the shutdown
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    CompletableFuture<App> shutdown(Throwable cause);

    /**
     * Initiates an asynchronously startup of the container. Normally, there is no need to call this methods since most
     * methods on the container will lazily start the container whenever it is needed. For example, invoking
     * {@link #with(Class)} will automatically start the container if it has not already been started by another action.
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
    CompletableFuture<Container> start();

    /**
     * Returns the state of application.
     * 
     * @return the state of application
     */
    LifecycleOperations<? extends App> state();

    static <T> T invoke(Bundle b, Function<App, T> function, String... args) {
        throw new UnsupportedOperationException();
    }

    // /**
    // * Creates a new container from a bundle of the specified type.
    // *
    // * @param bundleType
    // * the type of bundle to create the container from
    // * @return a new container
    // * @throws RuntimeException
    // * if the container could not be created
    // */
    // static Container of(Class<? extends Bundle> bundleType) {
    // return of(Bundles.instantiate(bundleType));
    // }

    /**
     * Creates a new container from the specified bundle.
     *
     * @param bundle
     *            the bundle to create the container from
     * @return a new container
     * @throws RuntimeException
     *             if the container could not be created
     */
    static App of(Bundle bundle) {
        throw new UnsupportedOperationException();
    }

    static App of(Consumer<? super AppConfigurator> configurator, OldWiringOperation... operations) {
        requireNonNull(configurator, "configurator is null");
        ContainerBuilder c = new ContainerBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF));
        configurator.accept(c);
        throw new UnsupportedOperationException();
        // return c.build();
    }
    // Deployment Options (Disable StackCatching)
    // Consumer<App>
    // Main string args

    static void run(Bundle b, AppOptions options, String... args) {
        throw new UnsupportedOperationException();
    }

    static void run(Bundle b, Consumer<App> consumer, AppOptions options, String... args) {
        throw new UnsupportedOperationException();
    }

    static void run(Bundle b, Consumer<App> consumer, String... args) {
        throw new UnsupportedOperationException();
    }

    static void run(Bundle b, String... args) {
        throw new UnsupportedOperationException();
    }

    static CompletableFuture<Void> runAsync(Bundle b, AppOptions options, String... args) {
        throw new UnsupportedOperationException();
    }

    static CompletableFuture<Void> runAsync(Bundle b, Consumer<App> consumer, AppOptions options, String... args) {
        throw new UnsupportedOperationException();
    }

    static CompletableFuture<Void> runAsync(Bundle b, Consumer<App> consumer, String... args) {
        throw new UnsupportedOperationException();
    }

    static CompletableFuture<Void> runAsync(Bundle b, String... args) {
        throw new UnsupportedOperationException();
    }

    // Maybe have a lazy start as an AppOptions
    static App start(Bundle b, AppOptions options, String... args) {
        throw new UnsupportedOperationException();
    }

    // Maybe have a lazy start as an AppOptions
    static App start(Bundle b, String... args) {
        throw new UnsupportedOperationException();
    }
}

// static <X extends Throwable> void runX(Bundle b, TConsumer<App, X> consumer, String... args) throws X {
// throw new UnsupportedOperationException();
// }