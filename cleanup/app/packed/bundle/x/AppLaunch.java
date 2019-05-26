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
package app.packed.bundle.x;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import app.packed.container.AnyBundle;
import app.packed.container.App;
import app.packed.container.Bundle;
import app.packed.container.Wirelet;

/**
 *
 */
// No Result...
// Can be run once..
// Could also
// Listener Manager????
// Can only be run once, unless

// Guest... Being added to a running App/Host/...
public class AppLaunch implements Runnable {

    public boolean isRepeatable() {
        return false;
        // Can we run it again, must a property on the bundle....
    }

    /**
     *
     * @throws UnsupportedOperationException
     *             if the bundle is not runnable (has at least one component). Maybe throw this from
     * 
     */
    @Override
    public void run() {

    }

    /**
     * Runs the application asynchronously. Equivalent to invoking {@code CompletableFuture.runAsync(app)}.
     * 
     * @return a new CompletableFuture that is asynchronously completed after application has run
     */
    public CompletableFuture<Void> runAsync() {
        return CompletableFuture.runAsync(this);
    }

    public CompletableFuture<Void> runAsync(Executor executor) {
        return CompletableFuture.runAsync(this, executor);
    }

    /**
     * Creates new application from the specified bundle and an optional array of wiring operations.
     * 
     * @param bundle
     *            the bundle to create an application for
     * @param operations
     *            an optional array of wiring operation
     * @return the new application
     */
    public static AppLaunch of(Bundle bundle, Wirelet... operations) {
        return null;
    }

    public static AppLaunch of(Bundle bundle, String[] args, Wirelet... operations) {
        return null;
    }

    /**
     * Instantiate the specified and return an application facade from it
     * 
     * @param bundle
     *            the bundle to instantiate and return
     * @param operations
     *            an optional array of wiring operations
     * @return stuff
     * @throws RuntimeException
     *             if the application failed to start properly
     */
    static App of(AnyBundle bundle, Wirelet... operations) {
        throw new UnsupportedOperationException();
    }

    static void run(AnyBundle bundle, Wirelet... operations) {
        // Hedder execute, for ikke at navnene minder for meget om hinanden (run + running)
        // Vi skal ogsaa
        throw new UnsupportedOperationException();
    }

    //
    // static <T> T invoke(Bundle b, Function<App, T> function, String... args) {
    // throw new UnsupportedOperationException();
    // }

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
    // ContainerBuilder c = new ContainerBuilder(InternalConfigurationSite.ofStack(ConfigurationSiteType.INJECTOR_OF));
    // configurator.accept(c);
    // return c.build()
    // throw new UnsupportedOperationException();
    // return c.build();
    // Deployment Options (Disable StackCatching)
    // Consumer<App>
    // Main string args

    // static void run(Bundle b, Consumer<App> consumer, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // public static void run(Bundle b, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static CompletableFuture<Void> runAsync(Bundle b, Consumer<App> consumer, String[] args, WiringOperation...
    // operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static CompletableFuture<Void> runAsync(Bundle b, Consumer<App> consumer, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static CompletableFuture<Void> runAsync(Bundle b, String[] args, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static CompletableFuture<Void> runAsync(Bundle b, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static void runWithArgs(Bundle b, String[] args, Consumer<App> consumer, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // static void runWithArgs(Bundle b, String[] args, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // // Maybe have a lazy start as an AppOptions
    // static App start(Bundle b, String[] args, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }
    //
    // // Maybe have a lazy start as an AppOptions
    // static App start(Bundle b, WiringOperation... operations) {
    // throw new UnsupportedOperationException();
    // }

    /// Take two

    // static <X extends Throwable> void runX(Bundle b, TConsumer<App, X> consumer, String... args) throws X {
    // throw new UnsupportedOperationException();
    // }
}
