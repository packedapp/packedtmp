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
 * A single application that has been installed into an {@link ApplicationRepository}.
 * Either at build-time using.
 * Or at runtime using {@link ApplicationRepository#install(java.util.function.Consumer)}.
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

    /** {@return the underlying (unconfigurable) handle of the applications} */
    ApplicationHandle<?, ?> handle();

    /** {@return an instance with the specified name if it exists} */
    Optional<ManagedInstance<I>> instance(String name);

    /** {@return a stream of all managed instances for this application} */
    Stream<ManagedInstance<I>> instances();

    boolean isAvailable();

    boolean isManaged();

    ApplicationLauncher<I> launcher();

    I startNew(Wirelet... wirelets);

    /**
     *
     * @throws UnsupportedOperationException
     *             if used stand-alone outside of an {@link app.packed.application.ApplicationRepository}.
     */
    default void uninstall() {}

    // States: -> Enabled->Disabled->Stopping->Stopped->UnInstalling->UnInstalled
}
