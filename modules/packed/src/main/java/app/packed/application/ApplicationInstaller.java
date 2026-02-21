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

import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import internal.app.packed.application.PackedApplicationInstaller;

/**
 * An installer for applications.
 * <p>
 * An application installer can only be used to install a single application. Using an application installer after it
 * has been used will result in an {@link IllegalStateException} being thrown for any method on this interface.
 */
public sealed interface ApplicationInstaller<H extends ApplicationHandle<?, ?>> permits PackedApplicationInstaller {

    /**
     * Add the specified tags to the application.
     *
     * @param tags
     *            the tags to add
     * @return this installer
     * @see ApplicationMirror#componentTags()
     * @see ApplicationConfiguration#componentTag(String...)
     * @see ApplicationConfiguration#componentTags()
     * @see ApplicationHandle#componentTag(String...)
     * @see ApplicationHandle#componentTags()
     */
    ApplicationInstaller<H> componentTag(String... tags);

    default ApplicationInstaller<H> expectsResult(Class<?> resultType) {
        throw new UnsupportedOperationException();
    }

    /**
     * Installs the new application represented by the specified assembly.
     * <p>
     * The handle that is returned will no longer be configurable.
     *
     * @param assembly
     *            the application represented by an assembly
     * @param wirelets
     *            optional wirelets
     * @return a handle representing the application
     */
    H install(Assembly assembly, Wirelet... wirelets);

    /**
     * Sets the name of the application
     *
     * @param name
     *            the name of the application
     * @return this installer
     */
    ApplicationInstaller<H> named(String name);

    /**
     * Sets the value of the specified application build local.
     * <p>
     * This method will override any value set by
     * {@link ApplicationTemplate.Configurator#setLocal(ApplicationBuildLocal, Object)} for the same local.
     *
     * @param <T>
     *            the type of value the local holds
     * @param local
     *            the local to set
     * @param value
     *            the value of the local
     * @return this installer
     */
    <T> ApplicationInstaller<H> setLocal(ApplicationLocal<T> local, T value);
}

// Det er nok ikke her man skal installere det...

// Paa selve extensionene maaske. Det skal maaske startes inde hoved applicationen.
// Men det skal jo ikke noedvidigvis bygges inde

// <H> ApplicationInstaller<H> newDependency(ApplicationTemplate<H> t);

//lazy start single application. and make the following services available
//Her taenker jeg fx ElasticSearch
//Som kunne vaere en kaempe kompliceret application.
//Som vi gerne vil starte foer hoved applicationen

//prestartIt. And then block operations?

//Bootstrap App har ogsaa en template