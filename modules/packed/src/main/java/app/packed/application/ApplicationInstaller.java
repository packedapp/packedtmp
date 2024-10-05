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

import app.packed.application.repository.ApplicationLauncher;
import app.packed.assembly.Assembly;
import app.packed.container.Wirelet;
import internal.app.packed.application.PackedApplicationInstaller;

/** An installer for applications. */
public sealed interface ApplicationInstaller<H extends ApplicationHandle<?, ?>> permits PackedApplicationInstaller {

    /**
     * Add the specified tags to the application.
     *
     * @param tags
     *            the tags to add
     * @return this configurator
     * @see ApplicationMirror#componentTags()
     * @see ApplicationHandle#componentTag(String...)
     * @see ApplicationConfiguration#componentTag(String...)
     */
    ApplicationInstaller<H> componentTag(String... tags);

    /**
     * <p>
     * The handle that is returned will be non-configurable.
     *
     * @param assembly
     * @param wirelets
     * @return
     */
    H install(Assembly assembly, Wirelet... wirelets);

    default ApplicationLauncher<?> installAtRuntime(Assembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    ApplicationInstaller<H> named(String name);

    <T> ApplicationInstaller<H> setLocal(ApplicationBuildLocal<T> local, T value);
}