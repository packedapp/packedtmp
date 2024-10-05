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
import java.util.stream.Stream;

import app.packed.application.repository.ApplicationLauncher;
import app.packed.application.repository.ManagedInstance;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import internal.app.packed.extension.ExtensionSetup;

/**
 * An application repository that can be used to install applications at runtime or retrieve applications that were
 * installed at build-time.
 * <p>
 * This interface only supported unmanaged applications.
 * If you need to create managed applications uses {@link app.packed.application.guestmanager.ManagedApplicationRepository}.
 */
public interface ApplicationRepository<I, H extends ApplicationHandle<I, ?>> {

    Optional<H> handle(String name);

    /**
     * {@return a stream of all applications (represented by their application handle} that have been installed into the
     * repository}
     */
    Stream<H> handles();

    Optional<ManagedInstance<I>> instance(String name);

    Stream<ManagedInstance<I>> instances();

    Optional<ApplicationLauncher<I>> launcher(String name);

    Stream<ApplicationLauncher<I>> launchers();

    /**
     * Creates an installer for a new application based on {@link #template()}.
     *
     * @return an installer for a new application
     */
    ApplicationTemplate.Installer<H> newApplication();

    /** {@return the template that is used for all applications in this repository} */
    ApplicationTemplate<H> template();

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(BaseExtensionPoint point) {
        throw new UnsupportedOperationException();
    }

    // Syntes maaske vi skal tage H med.. Saa kan vi lave en god default key
    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(Class<H> handleKind, ApplicationTemplate<H> template,
            BaseExtension extension) {
        return ApplicationRepositoryHandle.install(handleKind, template, ExtensionSetup.crack(extension), ExtensionSetup.crack(extension).container.assembly);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> provide(Class<H> handleKind, ApplicationTemplate<H> template,
            BaseExtension extension) {
        return install(handleKind, template, extension).provide();
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
