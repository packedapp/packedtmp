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
package app.packed.application;

import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.build.BuildLocal;
import app.packed.container.ContainerLocal;
import app.packed.container.Wirelet;

/** A component local that has application scope. */
public non-sealed interface ApplicationLocal<T> extends BuildLocal<ApplicationLocal.ApplicationLocalAccessor, T> {

    /**
     * Returns a wirelet that can be used to set the value of this application local.
     * <p>
     * The wirelet can only applied to the application's root assembly. Attempting to use it on a non-application-root
     * assembly will result in a {@link app.packed.container.WireletException} being thrown.
     * <p>
     * Attempting to use the returned wirelet at runtime will result in a WireletException being thrown.
     *
     * @param value
     *            the value to set the local to
     * @return the new wirelet
     */
    Wirelet wireletSetter(T value);

    /**
     * Creates a new local with application scope.
     * <p>
     * Application scope means that a separate value is stored for every application.
     *
     * @param <T>
     *            the type of values to store
     * @return the new application local
     */
    static <T> ApplicationLocal<T> of() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new container local with application scope.
     * <p>
     * Application scope means that <strong>all containers in the same application</strong> will always see the same value
     * for the container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local with application scope
     */
    static <T> ApplicationLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        throw new UnsupportedOperationException();
    }

    /** An entity where {@link ApplicationLocal application local} values can be manipulated. */
    public sealed interface ApplicationLocalAccessor permits ContainerLocal.ContainerLocalAccessor, ApplicationConfiguration, ApplicationMirror, Assembly {}
}
