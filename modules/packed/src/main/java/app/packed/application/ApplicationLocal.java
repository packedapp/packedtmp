/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.assembly.Assembly;
import app.packed.build.BuildLocal;
import app.packed.component.ComponentRealm;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.Wirelet;

/** A build local that has application scope. */
public non-sealed interface ApplicationLocal<T> extends BuildLocal<ApplicationLocal.Accessor, T> {

    /**
     * Returns a wirelet that can be used to set the value of this application local.
     * <p>
     * The wirelet can only applied to the application's root assembly. Attempting to use it on a non-application-root
     * assembly will result in a {@link app.packed.container.WireletException} being thrown.
     *
     * @param value
     *            the value to set the local to
     * @return the new wirelet
     */
    // no error message, Fx med namespace scope. Saa kan vi jo ikke sige noget som helt om hvilken type wirelet det er.
    // Maaske kan man istedet have en constructor der tager den her local?
    // Og saa kan man
    // Men saa er der hele spoergsmaalet omkring consuming? Det goer vi saa ikkes
    Wirelet wireletSetter(T value); // no error message

    // Ideen er lidt vi kan share et object mellem flere wirelet typer
    // Kan selvfoelige kun bruges med of(Supplier). Eller vi kan checke if bound
    //Wirelet wireletCompute(Consumer<T> value);

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
     * Creates a new application local.
     *
     * @param <T>
     *            the type of value to store
     * @return a new application local
     */
    static <T> ApplicationLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        throw new UnsupportedOperationException();
    }

    static <T> ApplicationLocal<T> of(Function<ComponentRealm, ? extends T> initialValueSupplier) {
        throw new UnsupportedOperationException();
    }

    /** An entity where {@link ApplicationLocal application local} values can be manipulated. */
    public sealed interface Accessor permits ContainerBuildLocal.Accessor, ApplicationConfiguration, ApplicationHandle, ApplicationMirror, Assembly {}
}
