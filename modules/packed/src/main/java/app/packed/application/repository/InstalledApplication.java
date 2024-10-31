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
package app.packed.application.repository;

import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.repository.other.ManagedInstance;
import app.packed.container.Wirelet;
import internal.app.packed.application.repository.PackedInstalledApplication;

/**
 * A single application that has been installed into an {@link ApplicationRepository}. Either at build-time using. Or at
 * runtime using {@link ApplicationRepository#install(java.util.function.Consumer)}.
 *
 */
// extends InstalledContainer
public sealed interface InstalledApplication<I> permits PackedInstalledApplication {

    /**
     * Disables all future launch from this launcher.
     * <p>
     * Trying to lunch a new application, will fail with {@link IllegalStateException}.
     */
    // Hvad saa med alle dem der er i gang??? Fx er de i initialized
    // Det kraever ihvertfald at alle instancer selv dem der ikke er running, er med i instances.
    // Saa vi kan lukke dem ned
    void disable();

    /** {@return the underlying (unconfigurable) handle of the application} */
    ApplicationHandle<?, ?> handle();

    /**
     * {@return an instance with the specified name if it exists}
     *
     * @param name
     *            the of the application instance to find
     * @throws UnsupportedOperationException
     *             if this application is not {@link #isManaged() managed}.
     */
    Optional<ManagedInstance<I>> instance(String name);

    /**
     * {@return a stream of all managed instances for this application}
     *
     * @throws UnsupportedOperationException
     *             if this application is not {@link #isManaged() managed}.
     */
    Stream<ManagedInstance<I>> instances();

    boolean isAvailable();

    /** {@return whether or not the application is managed or unmanaged} */
    boolean isManaged();

    ApplicationLauncher<I> launcher();

    /** {@return the name of the application} */
    default String name() {
        return handle().name();
    }

    I startNew(Wirelet... wirelets);

    /**
     *
     * @throws UnsupportedOperationException
     *             if used stand-alone outside of an {@link app.packed.application.ApplicationRepository}.
     */
    default void uninstall() {}

    /**
     * The state of a installed application.
     */
    enum State {

        /** Applications can be created or started normally. */
        READY,

        /** Applications can no longer be created. */
        DISABLED,

        /**
         * All managed instances of the application has been stopped, but some are still awaiting termination.
         * <p>
         * When they have all successfully been stopped. The application will be uninstalled.
         */
        STOPPING_INSTANCES,

        /**
         * The application is currently in the process of being uninstalled.
         * <p>
         * Normally this is instant, but sometimes there might be some cleanup.
         */
        UNINSTALLING,

        /** The application has been fully uninstalled. */
        UNINSTALLED;
    }
    // States: -> Enabled->Disabled->Stopping->Stopped->UnInstalling->UnInstalled
}
