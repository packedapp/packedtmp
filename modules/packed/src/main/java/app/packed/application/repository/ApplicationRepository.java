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
import java.util.stream.Stream;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import internal.app.packed.application.PackedApplicationRepository;
import internal.app.packed.extension.ExtensionSetup;

/**
 * An application repository that can be used to install applications at runtime or retrieve applications that were
 * installed at build-time.
 * <p>
 * For now, we don't track instances here. We need some else for this
 */
public sealed interface ApplicationRepository<H extends ApplicationHandle<?, ?>> permits PackedApplicationRepository {

    Optional<H> get(String name);

    /**
     * Creates an installer for a new application based on {@link #template()}.
     *
     * @return an installer for a new application
     */
    ApplicationTemplate.Installer<H> newApplication();

    /**
     * {@return the number of installed applications}
     * <p>
     * This may include application that are in the process of being installed
     */
    int size();

    /**
     * {@return a stream of all applications (represented by their application handle} that have been installed into the
     * repository}
     */
    Stream<H> stream();

    /** {@return the template that is used for all applications in this repository} */
    // Don't know if we want this...
    ApplicationTemplate<H> template();

    // An application can be, NA, INSTALLING, AVAILABLE
    // Don't know if we at runtime
    // Hvad hvis man ikke vil installere noget paa runtime...

    // boolean availableAtRuntime

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<H> install(ApplicationTemplate<H> template, BaseExtension extension) {
        return ApplicationRepositoryHandle.install(template, ExtensionSetup.crack(extension), ExtensionSetup.crack(extension).container.assembly);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<H> install(BaseExtensionPoint point) {
        throw new UnsupportedOperationException();
    }
}
