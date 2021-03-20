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

import java.util.Map;
import java.util.NoSuchElementException;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.component.ComponentStream.Option;
import app.packed.container.BaseAssembly;
import app.packed.inject.ServiceLocator;
import app.packed.state.Host;
import app.packed.state.RunState;
import app.packed.state.StateWirelets;

/**
 * An App (application) is a type of artifact provided by Packed.
 */
// Skal have et 
public interface PreviousKnownAsApp extends AutoCloseable {

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
     * The component this is delegating
     * 
     * @return the component
     */
    Component component();

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
     * <p>
     * This method takes a {@link CharSequence} as parameter, so it is easy to passe either a {@link String} or a
     * {@link NamespacePath}.
     * 
     * @param path
     *            the path of the component to return
     * @throws IllegalArgumentException
     *             if no component exists with the specified path
     * @return a component with the specified path
     */
    default Component resolve(CharSequence path) {
        return component().resolve(path);
    }

    /**
     * Returns this app's service locator.
     * 
     * @return the service locator for this app
     */
    ServiceLocator services();

    /**
     * Returns a component stream consisting of this applications underlying container and all of its descendants in any
     * order.
     * <p>
     * Calling this method does <strong>not</strong> effect the lifecycle state of this application.
     * 
     * @return a component stream
     * @see #stream(Option...)
     */
    default ComponentStream stream() {
        return component().stream();
    }

    /**
     * Returns a component stream consisting of all the components in this image.
     * 
     * @param options
     *            stream options
     * @return the component stream
     * @see Component#stream(app.packed.component.ComponentStream.Option...)
     */
    default ComponentStream stream(ComponentStream.Option... options) {
        return component().stream(options);
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
     * Creates a new app image from the specified assembly.
     * <p>
     * The state of the applications returned by {@link ApplicationImage#use(Wirelet...)} will be {@link RunState#RUNNING}. unless
     * GuestWirelet.delayStart
     * 
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return a new app image
     * @see ImageWirelets
     */
    static ApplicationImage<PreviousKnownAsApp> buildImage(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().buildImage(assembly, wirelets);
    }

    /**
     * Returns an {@link ApplicationDriver artifact driver} for {@link PreviousKnownAsApp}.
     * 
     * @return an artifact driver for App
     */
    static ApplicationDriver<PreviousKnownAsApp> driver() {
        return PreviousKnownAsDefault.DRIVER;
    }

    /**
     * Build and start a new application using the specified assembly. The state of the returned application is
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
     *             if the application could not be build, initialized or started
     */
    static PreviousKnownAsApp start(Assembly<?> assembly, Wirelet... wirelets) {
        return driver().use(assembly, wirelets);
    }
}

class Ddd extends BaseAssembly {

    /** {@inheritDoc} */
    @Override
    protected void build() {}

    public static void main(String[] args) {
        try (PreviousKnownAsApp app = PreviousKnownAsApp.start(new Ddd())) {
            app.use(Map.class).isEmpty();
        }
    }
}

interface Zapp extends PreviousKnownAsApp {

    static PreviousKnownAsApp lazyStart(Assembly<?> assembly, Wirelet... wirelets) {
        // Altsaa der er vel disse interessant

        // initialized - lazy start
        // initialized - require explicit start
        // Starting
        // Started
        return PreviousKnownAsApp.driver().use(assembly, StateWirelets.lazyStart().andThen(wirelets));
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
    static ApplicationImage<PreviousKnownAsApp> singleImageOf(Assembly<?> assembly, Wirelet... wirelets) {
        return PreviousKnownAsApp.driver().buildImage(assembly, wirelets/* , ImageWirelet.single() */);
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
