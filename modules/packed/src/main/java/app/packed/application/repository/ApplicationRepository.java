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
 * An application repository manages 1 or more applications at runtime. The applications can be installed at build time
 * using {@link ApplicationRepositoryConfiguration#installChildApplication(Consumer)}. Or at runtime using
 * {@link #install(Consumer)}.
 * <p>
 * Once installed and application can be launched by using it {@link ApplicationLauncher}.
 * <p>
 * Managed vs Unmanaged
 * <p>
 * If an application is no longer needed it can be uninstalled by calling {@link InstalledApplication#uninstall()}. This
 * will, first disable launch of any
 */
public sealed interface ApplicationRepository<I, H extends ApplicationHandle<I, ?>> permits AbstractApplicationRepository {

    /** {@return a concatenated stream of all instances managed by every installed application in this repository} */
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

    /** {@return a stream of all applications that have been installed into the repository} */
    Stream<InstalledApplication<I>> applications();

    /**
     * Installs a new application in the repository based on {@link #template()}.
     * <p>
     * Applications can be uninstalled by calling {@link InstalledApplication#uninstall()}.
     *
     * @return the installed application
     * @throws RuntimeException
     *             if the application failed to build, or could not be installed
     */
    InstalledApplication<I> install(Consumer<? super ApplicationInstaller<H>> installer);

    /** {@return the underlying template for every application in this repository} */
    ApplicationTemplate<H> template();
}
