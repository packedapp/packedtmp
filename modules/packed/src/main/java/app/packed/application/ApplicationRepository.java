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
import java.util.Set;
import java.util.stream.Stream;

import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import internal.app.packed.application.PackedApplicationRepository;
import internal.app.packed.extension.ExtensionSetup;

/**
 * An application repository can be used to install child applications at runtime.
 * <p>
 * For now, we don't track instances here. We need some else for this
 */
public sealed interface ApplicationRepository<H extends ApplicationHandle<?, ?>> permits PackedApplicationRepository {

    Optional<H> get(String name);

    /** {@return whether or not applications can be added or removed from the repository} */
    default boolean isReadOnly() {
        return true;
    }

    /** {@return the number of installed applications.} */
    int size();

    /**
     * {@return a stream of all applications (represented by their application handle} that have been installed into the
     * repository}
     */
    Stream<H> stream();

    /**
     * {@return the templates that are available at runtime}
     * <p>
     * Attempting to install a new application with an application template that is not in returned set will fail with a
     * build exception.
     *
     * @see ApplicationRepositoryConfiguration#addRuntimeTemplate(ApplicationTemplate)
     */
    Set<ApplicationTemplate<?>> templates();

    // An application can be, NA, INSTALLING, AVAILABLE
    // Don't know if we at runtime
    // Hvad hvis man ikke vil installere noget paa runtime...

    // boolean availableAtRuntime

    static <A, H extends ApplicationHandle<?, A>> ApplicationRepositoryConfiguration<H, A> install(BaseExtensionPoint point) {
        throw new UnsupportedOperationException();
    }

    static <A, H extends ApplicationHandle<?, A>> ApplicationRepositoryConfiguration<H, A> install(Class<H> handleType, BaseExtension extension) {
        return ApplicationRepositoryHandle.install(ExtensionSetup.crack(extension), ExtensionSetup.crack(extension).container.assembly);
    }
}
