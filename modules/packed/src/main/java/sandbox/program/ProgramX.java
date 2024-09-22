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
package sandbox.program;

import app.packed.application.ApplicationMirror;
import app.packed.application.ApplicationTemplate;
import app.packed.application.BaseImage;
import app.packed.application.BootstrapApp;
import app.packed.assembly.Assembly;
import app.packed.component.guest.FromGuest;
import app.packed.container.ContainerTemplate;
import app.packed.container.Wirelet;
import app.packed.runtime.ManagedLifecycle;
import app.packed.runtime.RunState;
import app.packed.service.ServiceLocator;
import app.packed.util.Key;

/**
 * An App (application) is a type of artifact provided by Packed.
 */
// Skal have et
// Maaske bliver den sgu app igen
public interface ProgramX extends AutoCloseable {

    /**
     * Closes the app (synchronously). Calling this method is equivalent to calling {@code host().stop()}, but this method
     * is called close in order to support try-with resources via {@link AutoCloseable}.
     *
     * @see ManagedLifetimeController#stop(ManagedLifetimeController.StopOption...)
     **/
    @Override
    default void close() {
        runtime().stop();
    }

    /** {@return the name of this application} */
    String name();

    /**
     * Returns the applications's host.
     *
     * @return this application's host.
     */
    ManagedLifecycle runtime();

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
     * Returns an {@link BootstrapApp artifact driver} for {@link Program}.
     *
     * @return an artifact driver for App
     */
    private static BootstrapApp<ProgramImplementationX> driver() {
        return ProgramImplementationX.DRIVER;
    }

    /**
     * Creates a new app image from the specified assembly.
     * <p>
     * The state of the applications returned by {@link ApplicationLauncher#launch(Wirelet...)} will be unless
     * GuestWirelet.delayStart
     *
     * @param assembly
     *            the assembly to use for creating the image
     * @param wirelets
     *            optional wirelets
     * @return a new app image
     * @see ApplicationImageWirelets
     * @see BootstrapApp#newImage(Assembly, Wirelet...)
     */
    static BaseImage<ProgramX> imageOf(Assembly assembly, Wirelet... wirelets) {
        return driver().imageOf(assembly, wirelets).map(e -> e);
    }

    static ApplicationMirror mirrorOf(Assembly assembly, Wirelet... wirelets) {
        return driver().mirrorOf(assembly, wirelets);
    }

    /**
     * Build and start a new application using the specified assembly. The state of the returned application is running
     * <p>
     * Should be used with try-with-resources
     * <p>
     * Applications that are created using this method is always automatically started. If you wish to delay the start
     * process you can use LifetimeWirelets#lazyStartE. Which will return an application in the {@link RunState#INITIALIZED}
     * phase instead.
     *
     * @param assembly
     *            the assembly to use for creating the application
     * @param wirelets
     *            optional wirelets
     * @return the new application
     * @throws RuntimeException
     *             if the application could not be build, initialized or started
     */
    static ProgramX start(Assembly assembly, Wirelet... wirelets) {
        return driver().launch(RunState.RUNNING, assembly, wirelets);
    }
}

/** The default implementation of {@link Program}. */
record ProgramImplementationX(@FromGuest String name, @FromGuest ServiceLocator services,
        @FromGuest ManagedLifecycle runtime) implements ProgramX {

    ProgramImplementationX {
        // System.out.println(services.keys());
    }

    /** An driver for creating App instances. */

    static final BootstrapApp<ProgramImplementationX> DRIVER =
            ApplicationTemplate.of(ProgramImplementationX.class, c -> c.container(ContainerTemplate.MANAGED)).newBootstrapApp();


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "App[name = " + name() + ", state = " + runtime.currentState() + "] ";
    }
}
