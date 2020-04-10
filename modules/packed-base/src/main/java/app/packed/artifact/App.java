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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Component;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.ComponentStream.Option;
import app.packed.config.ConfigSite;
import app.packed.container.Wirelet;
import app.packed.lifecycle.LifecycleOperations;
import app.packed.lifecycle.RunState;
import app.packed.lifecycle.StopOption;
import app.packed.service.ServiceExtension;

/**
 * An App (application) is the main type of artifact available in Packed.
 * <p>
 * Applications are low overhead not using more then a few kilobytes.
 * <p>
 * You can have apps running apps runnings app.
 * 
 * You can easily have Hundreds of Thousands of applications running in the same JVM.
 */
public interface App extends AutoCloseable {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code app.stop()}, but the method is
     * here in order to support try-with resources via {@link AutoCloseable}.
     **/
    @Override
    default void close() {
        stop();
    }

    /**
     * Returns the configuration site of this application.
     * <p>
     * If this application was created from an {@link ArtifactImage image}, this method will return the site where the image
     * was created. Unless the AI.Wiring option is used when construction the application.
     * 
     * @return the configuration site of this application
     */
    ConfigSite configSite();

    /**
     * Returns the description of this application. Or an empty optional if no description has been set.
     * <p>
     * The returned description is always identical to the description of the application's root container.
     *
     * @return the description of this application
     *
     * @see ComponentConfiguration#setDescription(String)
     */
    Optional<String> description();

    /**
     * Returns the name of this application.
     * <p>
     * The returned name is identical to the name of the application's top container.
     * <p>
     * If no name is explicitly set when creating the application, the runtime will generate a name. If the applications has
     * siblings the name is guaranteed to be unique among them.
     * 
     * @return the name of this application
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

    /**
     * Returns the state of application.
     * 
     * @return the state of application
     */
    LifecycleOperations<? extends App> state();

    App stop(StopOption... options);

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     *
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    // StopOption.async() //OnStop.Option (nah det her er specifikke container options)
    CompletableFuture<App> stopAsync(StopOption... options);

    /**
     * Returns a component stream consisting of this applications underlying container and all of its descendants in any
     * order.
     * <p>
     * Calling this method does <strong>not</strong> effect the lifecycle state of this application.
     * 
     * @return a component stream
     * @see #stream(Option...)
     */
    ComponentStream stream();

    ComponentStream stream(ComponentStream.Option... options);

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing
     * {@link UnsupportedOperationException}.
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws UnsupportedOperationException
     *             if a service with the specified key exist. Or if the application does not use {@link ServiceExtension}.
     */
    default <T> T use(Class<T> key) {
        return use(Key.of(key));
    }

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing
     * {@link UnsupportedOperationException}.
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws UnsupportedOperationException
     *             if a service with the specified key exist. Or if the application does not use {@link ServiceExtension}.
     */
    <T> T use(Key<T> key);

    /**
     * <p>
     * This method takes a {@link CharSequence} as parameter, so it is easy to passe either a {@link String} or a
     * {@link ComponentPath}.
     * 
     * @param path
     *            the path of the component to return
     * @throws IllegalArgumentException
     *             if no component exists with the specified path
     * @return a component with the specified path
     */
    // TODO throw UnknownPathException();;
    //
    Component useComponent(CharSequence path);

    /**
     * Returns a driver for producing {@link App} instances.
     * <p>
     * This method is mainly used by advanced users.
     * 
     * @return a driver for producing App instances
     */
    static ArtifactDriver<App> driver() {
        return PackedApp.DRIVER;
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
    static void execute(ArtifactSource source, Wirelet... wirelets) {
        driver().execute(source, wirelets);
    }

    /**
     * Create an application (but does not start it) from the specified source. The state of the returned application is
     * {@link RunState#INITIALIZED}. The returned application will lazily start itself when needed. For example, on first
     * invocation of {@link #use(Class)}.
     *
     * @param source
     *            the source of the application
     * @param wirelets
     *            any wirelets to use in the construction of the application
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be initialized properly
     */
    static App of(ArtifactSource source, Wirelet... wirelets) {
        // Rename fordi vi gerne vil have at ArtifactDriver hedder det samme og
        // AppHost.xxx() .. Dumt det hedder App.of og AppHost.instantiate

        // Muligheder -> build... instantiate... create... initialize

        // Build -> Image.of -> App.build() hmmm Image.Build <- kun assemble delen...

        // Maaske build,,, you build an artifact...
        return driver().instantiate(source, wirelets);
    }

    /**
     * Create and start an application from the specified source. The state of the returned application is
     * {@link RunState#RUNNING}.
     *
     * @param source
     *            the source of the application
     * @param wirelets
     *            any wirelets to use in the construction of the application
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be initialized or started properly
     */
    static App start(ArtifactSource source, Wirelet... wirelets) {
        return driver().start(source, wirelets);
        // 10 seconds is from start.. Otherwise people must use an exact deadline
        // start(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS));
        // start(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS), ()-> New CancelledException()); (failure)
    }
}
//
//
//// Ved ikke om CompletableFuture giver mening. Hvis App returnere initialize() betyder det jo
//// at vi maa have nogle metoder vi kan afvente state paa...
//static CompletableFuture<App> startAsync(Assembly source, Wirelet... wirelets) {
//    // initialize().startAsync()
//    // Why return CompletableFuture???
//    PackedApp app = (PackedApp) driver().initialize(source, wirelets);
//    return app.startAsync(app);
//}