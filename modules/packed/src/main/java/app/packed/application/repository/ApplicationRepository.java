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
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.application.repository.other.ManagedInstance;
import internal.app.packed.application.repository.AbstractApplicationRepository;

/**
 * An application repository can be used to manage multiple installed applications at runtime.
 * <p>
 * Applications can be installed into a repository at build time using
 * {@link ApplicationRepositoryConfiguration#installChildApplication(Consumer)}. Or at runtime using
 * {@link #install(Consumer)}.
 * <p>
 * If an application is no longer needed it can be uninstalled by calling {@link InstalledApplication#uninstall()}.
 */
public sealed interface ApplicationRepository<I, H extends ApplicationHandle<I, ?>> permits AbstractApplicationRepository {

    /**
     * {@return a concatenated stream of all application instances managed by every installed application in this
     * repository}
     */
    default Stream<ManagedInstance<I>> allInstances() {
        return applications().flatMap(l -> l.instances());
    }

    /**
     * {@return an application with the specified name if present in this repository, otherwise empty}
     *
     * @param name
     *            the name of the application
     */
    Optional<InstalledApplication<I>> application(String name);

    /** {@return a stream of all installed applications in this repository} */
    Stream<InstalledApplication<I>> applications();

    /**
     * Installs a new application in the repository based on {@link #template()}.
     * <p>
     * Applications can be uninstalled by calling {@link InstalledApplication#uninstall()}.
     * <p>
     * If the application in which the repository is located is shutdown while calling this method. The returned application
     * will report a state of XXXXXXX?
     *
     * @return the installed application
     * @throws RuntimeException
     *             if the application failed to build, or could not be installed
     */
    InstalledApplication<I> install(Consumer<? super ApplicationInstaller<H>> installer);

    /** {@return the underlying template for every application in this repository} */
    ApplicationTemplate<H> template();
}
