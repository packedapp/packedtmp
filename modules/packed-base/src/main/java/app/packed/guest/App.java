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
package app.packed.guest;

import java.util.concurrent.CompletableFuture;

import app.packed.base.Key;
import app.packed.component.Bundle;
import app.packed.component.Component;
import app.packed.component.ComponentDelegate;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentStream;
import app.packed.component.Image;
import app.packed.component.ShellDriver;
import app.packed.component.ComponentStream.Option;
import app.packed.component.Wirelet;
import app.packed.config.ConfigSite;
import app.packed.lifecycleold.LifecycleOperations;
import app.packed.service.ServiceExtension;

/**
 * An App (application) is the main type of shell available in Packed and should cover must usages.
 */
public interface App extends AutoCloseable, ComponentDelegate {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code app.stop()}, but this method is
     * here in order to support try-with resources via {@link AutoCloseable}.
     **/
    @Override
    default void close() {
        stop();
    }

    /**
     * Returns the configuration site of this application.
     * <p>
     * If this application was created from an {@link Image image}, this method will return the site where the image was
     * created. Unless the AI.Wiring option is used when construction the application.
     * 
     * @return the configuration site of this application
     */
    ConfigSite configSite();

    /**
     * Returns the name of this application.
     * <p>
     * The returned name is identical to the name of the application's top component.
     * <p>
     * If no name is explicitly set when creating the application, the runtime will generate a name. If the applications has
     * siblings the name is guaranteed to be unique among them.
     * 
     * @return the name of this application
     */
    default String name() {
        return component().name();
    }

    /**
     * Returns the path of this application. Unless the app is installed as a guest, this method always returns
     * <code>"{@literal /}"</code>.
     *
     * @return the path of this application
     * @see Component#path()
     */
    default ComponentPath path() {
        return component().path();
    }

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
    @Override
    // TODO throw UnknownPathException();;
    // componentAt
    // Altsaa maaske har vi mere et slags SystemView?
    Component resolve(CharSequence path);

    /**
     * Returns the state of application.
     * 
     * @return the state of application
     */
    LifecycleOperations<? extends App> state();

    App stop(GuestStopOption... options);

    /**
     * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
     * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
     *
     * @param options
     *            options
     * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
     *         in the process of being shut down
     */
    CompletableFuture<App> stopAsync(GuestStopOption... options); // StopOption.async() //OnStop.Option (nah det her er specifikke container options)

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
    <T> T use(Class<T> key);

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
     * Returns a driver that produces {@link App} instances.
     * <p>
     * This method is mainly used by advanced users.
     * 
     * @return a driver that produces App instances
     */
    static ShellDriver<App> driver() {
        return PackedApp.DRIVER;
    }

    /**
     * Create an application (but does not start it) from the specified source. The state of the returned application is
     * {@link GuestState#INITIALIZED}. The returned application will lazily start itself when needed. For example, on first
     * invocation of {@link #use(Class)}.
     *
     * @param bundle
     *            the source of the application
     * @param wirelets
     *            any wirelets to use in the construction of the application
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be assembled or initialized
     */
    static App initialize(Bundle<?> bundle, Wirelet... wirelets) {
        return driver().initialize(bundle, wirelets);
    }

    static Image<App> newImage(Bundle<?> bundle, Wirelet... wirelets) {
        return driver().newImage(bundle, wirelets);
    }

    /**
     * Create and start a new application using the specified bundle. The state of the returned application is
     * {@link GuestState#RUNNING}.
     *
     * @param bundle
     *            the source of the application
     * @param wirelets
     *            optional wirelets
     * @return the new (running) application
     * @throws RuntimeException
     *             if the application failed to initialize or started properly
     */
    static App start(Bundle<?> bundle, Wirelet... wirelets) {
        return driver().start(bundle, wirelets);
    }
}
// 10 seconds is from start.. Otherwise people must use an exact deadline
// start(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS));
// sart(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS), ()-> New CancelledException()); (failure)

// Rename fordi vi gerne vil have at ArtifactDriver hedder det samme og
// AppHost.xxx() .. Dumt det hedder App.of og AppHost.instantiate

// Muligheder -> build... instantiate... create... initialize

// Build -> Image.of -> App.build() hmmm Image.Build <- kun assemble delen...

// Maaske build,,, you build an artifact...

///**
// * Returns the description of this application. Or an empty optional if no description has been set.
// * <p>
// * The returned description is always identical to the description of the application's root container.
// *
// * @return the description of this application
// *
// * @see ContainerBundle#setDescription(String)
// */
//Optional<String> description();

///**
//* Initiates an asynchronously startup of the application. Normally, there is no need to call this methods since most
//* methods on the container will lazily start the container whenever it is needed. For example, invoking
//* {@link #use(Class)} will automatically start the container if it has not already been started by another action.
//* <p>
//* If the container is in the process of being initialized when invoking this method, for example, from a method
//* annotated with {@link OnInitialize}. The container will automatically be started immediately after it have been
//* constructed.
//* <p>
//* Invocation has no additional effect if the container has already been started or shut down.
//*
//* @return a future that can be used to query whether the application has completed startup or is still in the process
//* of starting up. Can also be used to retrieve any exception that might have prevented the container in
//* starting properly
//*/
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