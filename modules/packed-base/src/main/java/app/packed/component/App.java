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
import app.packed.container.Container;
import app.packed.container.ContainerState;
import app.packed.container.ContainerWirelets;
import app.packed.inject.ServiceLocator;

/**
 * An App (application) is the main type of shell available in Packed and should cover must usages.
 */
public interface App extends AutoCloseable, ComponentDelegate {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code container().stop()}, but this
     * method is here in order to support try-with resources via {@link AutoCloseable}.
     * 
     * @see Container#stop(Container.StopOption...)
     **/
    @Override
    default void close() {
        container().stop();
    }

    /**
     * Returns the container this app wraps.
     * 
     * @return this application's container.
     */
    Container container();

    /**
     * Returns the name of this application.
     * <p>
     * The returned name is identical to the name of the application's component.
     * 
     * @return the name of this application
     * @see Component#name()
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
     * Returns a driver that produce {@link App} instances.
     * <p>
     * This method is mainly used by advanced users.
     * 
     * @return a driver that produces App instances
     */
    static ShellDriver<App> driver() {
        return PackedApp.DRIVER;
    }

    /**
     * 
     * <p>
     * Once used the image will return an App in the started state. unless GuestWirelet.delayStart
     * 
     * @param bundle
     *            the bundle to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return a new app image
     */
    static Image<App> imageOf(Assembly<?> bundle, Wirelet... wirelets) {
        return driver().newImage(bundle, wirelets);
    }

    /**
     * Build and start a new application using the specified bundle. The state of the returned application is
     * {@link ContainerState#RUNNING}.
     * <p>
     * Should be used with try-with-resources
     * <p>
     * Applications that are created using this method is always automatically started. If you wish to delay the start
     * process you can use {@link ContainerWirelets#lazyStart()}. Which will return an application in the
     * {@link ContainerState#INITIALIZED} phase instead.
     * 
     * @param bundle
     *            the bundle to use for creating the application
     * @param wirelets
     *            optional wirelets
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be created or started
     */
    static App of(Assembly<?> bundle, Wirelet... wirelets) {
        return driver().newShell(bundle, wirelets);
    }
}
