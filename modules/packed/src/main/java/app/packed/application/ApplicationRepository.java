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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.application.repository.ApplicationLauncher;
import app.packed.application.repository.ManagedInstance;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.repository.AbstractApplicationRepository;
import internal.app.packed.extension.ExtensionSetup;

/**
 * An application repository is used for launching new applications at runtime. The applications can be installed at
 * build time using {@link ApplicationRepositoryConfiguration#installChildApplication(Consumer)}. Or at runtime using
 * {@link #install(Consumer)}.
 * <p>
 * Once installed and application can be launched by using it {@link ApplicationLauncher}.
 * <p>
 * Managed vs Unmanaged
 *
 * <p>
 * If an application is no longer needed it can be uninstalled by calling {@link ApplicationLauncher#uninstall()}.
 * This will, first disable launch of any
 */
public sealed interface ApplicationRepository<I, H extends ApplicationHandle<I, ?>> permits AbstractApplicationRepository {

    /**
     * Installs a new application in the repository based on {@link #template()}.
     * <p>
     * Applications can be uninstalled by calling {@link ApplicationLauncher#uninstall()}.
     *
     * @return an application launcher representing the new application
     */
    ApplicationLauncher<I> install(Consumer<? super ApplicationInstaller<H>> installer);

    /** {@return a concatenated stream of all instances managed by every launcher in this repository} */
    default Stream<ManagedInstance<I>> instances() {
        return launchers().flatMap(l -> l.instances());
    }

    /**
     * @param name
     *            the name of the application
     * @return
     */
    Optional<ApplicationLauncher<I>> launcher(String name);

    /**
     * {@return a stream of all applications (represented by an application launcher) that have been installed into the
     * repository}
     */
    Stream<ApplicationLauncher<I>> launchers();

    /** {@return the template that is used for every application in this repository} */
    ApplicationTemplate<H> template();

    // We need to fail on a managed appplication template in an unmanaged container
    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(ApplicationTemplate<H> template, BaseExtension extension) {
        // Hmm, maaske flyt tilbage til config, skal lave de samme checks for extension der laver det
        PackedApplicationTemplate<H> t = (PackedApplicationTemplate<H>) template;
        if (t.guestClass() == Void.class) {
            throw new UnsupportedOperationException("Does not support application templates of Void.class guest type");
        }
        return ApplicationRepositoryHandle.install(t, ExtensionSetup.crack(extension), ExtensionSetup.crack(extension).container.assembly);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(BaseExtensionPoint point) {
        throw new UnsupportedOperationException();
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> provide(ApplicationTemplate<H> template, BaseExtension extension) {
        return install(template, extension).provide();
    }

    // Shared with ContainerRepository
    interface Entry {
        boolean isBuild();

        boolean isLazy(); // Built on first usage

        // An application can be, NA, INSTALLING, AVAILABLE
        // Don't know if we at runtime
        // Hvad hvis man ikke vil installere noget paa runtime...
    }
}
