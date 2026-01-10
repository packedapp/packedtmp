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
package app.packed.application.registry;

import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.containerdynamic.ManagedInstance;
import app.packed.lifecycle.RunState;
import internal.app.packed.application.repository.PackedInstalledApplication;

/**
 * A single application that has been installed into an {@link ApplicationRepository}. Either at build-time using, or at
 * runtime using {@link ApplicationRepository#install(java.util.function.Consumer)}.
 */
// Basicallly an image that can be uninstalled and jas managed instances...
public sealed interface LaunchableApplication<I> permits PackedInstalledApplication {

    /**
     * Disables all future launch from this launcher.
     * <p>
     * Trying to lunch a new application, will fail with {@link IllegalStateException}.
     */
    // Hvad saa med alle dem der er i gang??? Fx er de i initialized
    // Det kraever ihvertfald at alle instancer selv dem der ikke er running, er med i instances.
    // Saa vi kan lukke dem ned

    // Spoergsmaalet er om vi har behov for at kunne koere den her alene?
    void disable();

    /** {@return the underlying (unconfigurable) handle of the application} */
    // Brings in all the backend...
    ApplicationHandle<?, ?> handle();

    boolean isAvailable();

    /** {@return whether or not the application is managed or unmanaged} */
    boolean isManaged();

    Launcher<I> launcher();

    /**
     * {@return an application instance with the specified name if it exists}
     *
     * @param name
     *            the of the application instance to find
     * @throws UnsupportedOperationException
     *             if this application is not a {@link #isManaged() managed} application.
     */
    Optional<ManagedInstance<I>> instance(String name);

    /**
     * {@return a stream of all managed instances for this application}
     *
     * @throws UnsupportedOperationException
     *             if this application is not a {@link #isManaged() managed} application.
     */
    Stream<ManagedInstance<I>> instances();

    /** {@return the name of the application} */
    default String name() {
        return handle().name();
    }

    I startNew();

    /**
     *
     * @throws UnsupportedOperationException
     *             if used stand-alone outside of an {@link app.packed.application.ApplicationRepository}.
     */
    // https://github.com/openjdk/loom/blob/fibers/src/java.base/share/classes/java/util/concurrent/StructuredTaskScope.java
    // see close method for interruption et.
    default void uninstall() {}

    // Kune maaske shares med container
    // Skal have lidt mere koedt paa en bare saette et navn syntes jegT

    // Bruger vi den kun fra LauncherableApplication???
    // Saa syntes jeg vi skal smide den som et nested interface (Launcher)
    // Altsaa hvis vi returnere ManagedInstance... Saa er den jo ikke brugt som root
    public interface Launcher<I> {

        /**
         * Initializes a new application instance.
         *
         * @param wirelets
         *            optional wirelets
         * @return the new application instance
         */
        I initialize();

        ManagedInstance<I> launch(RunState state);

        /**
         * @param wirelets
         * @return
         * @throws UnsupportedOperationException
         *             if the application is unmanaged.
         */
        ManagedInstance<I> launch();

        /**
         * s Names the application instance to be launched.
         *
         * @param instanceName
         *            the name of the application instance
         * @return this launcher
         */
        Launcher<I> named(String instanceName);
    }

    /**
     * The state of a installed application.
     */
    // HostState, some
    enum State {

        // Installing AKA building... But not observable I think.

        /** Applications instances can no longer be created. */
        DISABLED,

        /** Application instances can be created. */
        READY,

        /**
         * All managed application instances of the application are being stopped, but some have not yet fully terminated.
         * <p>
         * When they have all successfully been stopped. The application will be uninstalled.
         */
        STOPPING_INSTANCES,

        /** The application has been fully uninstalled. */
        UNINSTALLED,

        /**
         * The application is currently in the process of being uninstalled.
         * <p>
         * Normally this is instant, but sometimes there might be some cleanup.
         */
        UNINSTALLING;
    }
    // States: -> Enabled->Disabled->Stopping->Stopped->UnInstalling->UnInstalled
}
