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

import app.packed.container.Wirelet;
import app.packed.runtime.RunState;

// Kune maaske shares med container
public interface GuestLauncher<I> {

    /**
     * @param wirelets
     * @return
     * @throws UnsupportedOperationException
     *             if the underlying application template is unmanaged.
     */
    ManagedInstance<I> launchGuest(Wirelet... wirelets);

    ManagedInstance<I> launchGuest(RunState state, Wirelet... wirelets);

    I launchInitialized(Wirelet... wirelets); // maybe juse initialized

    /**
     * Names the application instance to be launched.
     *
     * @param instanceName
     *            the name of the application instance
     * @return this launcher
     */
    GuestLauncher<I> named(String instanceName);
}