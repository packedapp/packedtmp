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
package app.packed.application.registry;

import java.util.function.Consumer;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.service.ProvidableBeanConfiguration;

/**
 *
 */
// Heh Det er saadan en bean. Hvor vi helst ikke vil have at der bliver pillet ved den. Andet end navn
// Vi skal fx ikke til at injecte ting.
// Saa ma have noget reanonly i bean template
// Fordi vi kan finde configuration senere ved at itererer

/// OKAY I think we make this to a "BuildtimeRepositoryConfiguration"
// That has an provideRepositoryAtRuntime();
// I don't know don't we always??

// Maybe the bean is owned by the extension?? And provided to the user

// Maybe we don't extend it ServiableBean and have a provideAtRuntime();

// Maybe it is a BaseExtension bean
// But a service it is a service...

public final class ApplicationRegistryConfiguration<I, H extends ApplicationHandle<I, ?>> extends ProvidableBeanConfiguration<ApplicationRegistry<I, H>> {

    /** The application repository bean handle. */
    final ApplicationRegistryBeanHandle<I, H> handle;

    /**
     * @param handle
     *            the bean's handle
     */
    ApplicationRegistryConfiguration(ApplicationRegistryBeanHandle<I, H> handle) {
        super(handle);
        this.handle = handle;
    }

    /**
     * @param installer
     *            a consumer that performs the actual installation of the application
     */
    public void installApplication(Consumer<? super ApplicationInstaller<H>> installer) {
        handle.repository.add(installer);
    }

//    public ApplicationRegistryConfiguration<I, H> trackInstallations(JobNamespaceConfiguration job) {
//        // I guess this means that have a JobTracker interface provided to the repository bean
//        // handle.
//        return this;
//    }

    @Override
    public ApplicationRegistryConfiguration<I, H> provide() {
        super.provide();
        return this;
    }

    /** {@return the templates that are available in the repository) */
    @SuppressWarnings("unchecked")
    public ApplicationTemplate<H> template() {
        return (ApplicationTemplate<H>) handle.repository.template;
    }
}
