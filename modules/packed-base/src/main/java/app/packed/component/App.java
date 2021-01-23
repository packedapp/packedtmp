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
import app.packed.inject.ServiceLocator;
import app.packed.state.Host;
import app.packed.state.RunState;
import app.packed.state.StateWirelets;

/**
 * An App (application) is a type of artifact provided by Packed.
 */
public interface App extends AutoCloseable, ComponentDelegate {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code host().stop()}, but this method
     * is called close in order to support try-with resources via {@link AutoCloseable}.
     * 
     * @see Host#stop(Host.StopOption...)
     **/
    @Override
    default void close() {
        host().stop();
    }

    /**
     * Returns the applications's host.
     * 
     * @return this application's host.
     */
    Host host();

    /**
     * Returns the name of this application.
     * <p>
     * The name of an application is identical to the name of the application's component.
     * 
     * @return the name of this application
     * @see Component#name()
     */
    // Her knaekker filmen saa lidt..
    // Hvis vi har multiple apps saa skal state navnet jo vaere en del af navnet???
    // Taenker her paa noget der restarter. Og hver app hedder noget nyt...
    // Men altsaa er ikke sikker paa man bruger en app saa...
    // Kan ikke se noget alternativt...
    default String name() {
        return component().name();
    }

    /**
     * Returns this app's service locator.
     * 
     * @return the service locator for this app
     */
    ServiceLocator services();

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing {@link NoSuchElementException}.
     * <p>
     * This method is shortcut for {@code services().use(key)}
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist.
     */
    default <T> T use(Class<T> key) {
        return services().use(key);
    }

    /**
     * Returns a service with the specified key, if it exists. Otherwise, fails by throwing {@link NoSuchElementException}.
     * <p>
     * This method is shortcut for {@code services().use(key)}
     * <p>
     * If the application is not already running
     * 
     * @param <T>
     *            the type of service to return
     * @param key
     *            the key of the service to return
     * @return a service of the specified type
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist.
     */
    default <T> T use(Key<T> key) {
        return services().use(key);
    }

    /**
     * Returns an {@link ArtifactDriver artifact driver} for {@link App}.
     * 
     * @return an artifact driver for App
     */
    static ArtifactDriver<App> driver() {
        return PackedApp.DRIVER;
    }

    /**
     * Creates a new app image from the specified assembly.
     * <p>
     * The state of the applications returned by {@link Image#use(Wirelet...)} will be {@link RunState#RUNNING}. unless
     * GuestWirelet.delayStart
     * 
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return a new app image
     * @see ImageWirelets
     */
    static Image<App> imageOf(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().newImage(assembly, wirelets);
    }

    /**
     * Build and start a new application using the specified bundle. The state of the returned application is
     * {@link RunState#RUNNING}.
     * <p>
     * Should be used with try-with-resources
     * <p>
     * Applications that are created using this method is always automatically started. If you wish to delay the start
     * process you can use {@link StateWirelets#lazyStart()}. Which will return an application in the
     * {@link RunState#INITIALIZED} phase instead.
     * 
     * @param assembly
     *            the assembly to use for creating the application
     * @param wirelets
     *            optional wirelets
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be created or started
     */
    static App of(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().newArtifact(assembly, wirelets);
    }

    // An image that can be used exactly, will drop any memory references...
    // Maybe make a more generic low-memory profile
    // Which drops this. And keeps
    // It is is more like single instantiable..
    // Because we can analyze it as many times as we want..
    // singleImageOf
    /**
     * 
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return the new image
     */
    static Image<App> singleImageOf(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().newImage(assembly, wirelets/* , ImageWirelet.single() */);
    }
}
///**
//* Returns the path of this application. Unless the app is installed as a guest, this method always returns
//* <code>"{@literal /}"</code>.
//*
//* @return the path of this application
//* @see Component#path()
//*/
//// The state or the component?????
////
//default NamespacePath path() {
// return component().path();
//}
