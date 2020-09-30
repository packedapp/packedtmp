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
package app.packed.component;

import java.util.NoSuchElementException;

import app.packed.base.Key;
import app.packed.base.TreePath;
import app.packed.config.ConfigSite;
import app.packed.guest.Guest;
import app.packed.guest.GuestState;
import app.packed.guest.GuestWirelets;
import app.packed.service.ServiceLocator;

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
        guest().stop();
    }

    /**
     * Returns the configuration site of this application.
     * <p>
     * If this application was created from an {@link Image image}, this method will return the site where the image was
     * created. Unless the AI.Wiring option is used when construction the application.
     * 
     * @return the configuration site of this application
     */
    // Altsaa vil vi have den her????
    default ConfigSite configSite() {
        return component().configSite();
    }

    /**
     * Returns the guest.
     * <p>
     * Calling this method will never effect the state of the app.
     * 
     * @return this application's guest.
     */
    Guest guest();

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
    default TreePath path() {
        return component().path();
    }

    /**
     * Returns the service locator for this app.
     * 
     * @return the service locator for this app
     */
    ServiceLocator services();

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing {@link NoSuchElementException}.
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws NoSuchElementException
     *             if a service with the specified key exist.
     */
    default <T> T use(Class<T> key) {
        return services().use(key);
    }

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing {@link NoSuchElementException}.
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws NoSuchElementException
     *             if a service with the specified key exist.
     */
    default <T> T use(Key<T> key) {
        return services().use(key);
    }

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

    // Once used the image will return an App in the started state. unless GuestWirelet.delayStart
    static Image<App> imageOf(Bundle<?> bundle, Wirelet... wirelets) {
        return driver().newImage(bundle, wirelets);
    }

    /**
     * Create and start a new application using the specified bundle. The state of the returned application is
     * {@link GuestState#RUNNING}.
     * <p>
     * Should be used with try-with-resources
     * <p>
     * If you do not wish to immediately start the application use {@link GuestWirelets#delayStart()}.
     * 
     * @param bundle
     *            the bundle to use for creating the application
     * @param wirelets
     *            optional wirelets
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be created or started
     */
    static App of(Bundle<?> bundle, Wirelet... wirelets) {
        return driver().newShell(bundle, wirelets);
    }

    static App of(Bundle<?> bundle) {
        return driver().newShell(bundle);
    }
}
//
///**
// * @param options
// *            optional guest stop options
// * @return this app
// */
//default App stop(GuestStopOption... options) {
//    guest().stop(options);
//    return this;
//}
//
///**
// * Initiates an orderly asynchronously shutdown of the application. In which currently running tasks will be executed,
// * but no new tasks will be started. Invocation has no additional effect if the application has already been shut down.
// *
// * @param options
// *            optional guest stop options
// * @return a future that can be used to query whether the application has completed shutdown (terminated). Or is still
// *         in the process of being shut down
// */
//default CompletableFuture<App> stopAsync(GuestStopOption... options) {
//    return guest().stopAsync(this, options);
//}

// 10 seconds is from start.. Otherwise people must use an exact deadline
// start(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS));
// sart(new SomeBundle(), LifecycleWirelets.stopAfter(10, TimeUnit.SECONDS), ()-> New CancelledException()); (failure)

// Rename fordi vi gerne vil have at ArtifactDriver hedder det samme og
// AppHost.xxx() .. Dumt det hedder App.of og AppHost.instantiate

// Muligheder -> build... instantiate... create... initialize

// Build -> Image.of -> App.build() hmmm Image.Build <- kun assemble delen...

// Maaske build,,, you build an artifact...

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