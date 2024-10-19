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

import app.packed.application.repository.other.ManagedInstance;
import app.packed.container.Wirelet;
import app.packed.runtime.RunState;

// Kune maaske shares med container
// Skal have lidt mere koedt paa en bare saette et navn syntes jegT
public interface ApplicationLauncher<I> {

    /**
     * Initializes a new application instance.
     *
     * @param wirelets
     *            optional wirelets
     * @return the new application instance
     */
    I initialize(Wirelet... wirelets);

    ManagedInstance<I> launch(RunState state, Wirelet... wirelets);

    /**
     * @param wirelets
     * @return
     * @throws UnsupportedOperationException
     *             if the application is unmanaged.
     */
    ManagedInstance<I> launch(Wirelet... wirelets);

    /**s
     * Names the application instance to be launched.
     *
     * @param instanceName
     *            the name of the application instance
     * @return this launcher
     */
    ApplicationLauncher<I> named(String instanceName);
}