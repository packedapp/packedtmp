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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationTemplate;
import app.packed.extension.BaseExtension;
import app.packed.extension.BaseExtensionPoint;
import app.packed.service.ProvidableBeanConfiguration;

/**
 * Ideen for 2eren i forhold til {@link ApplicationRepositoryConfiguration} er at vi ikke som default laver en bean
 * configuration. Fx hvis man bare vil installere nogle applications paa buildtime. Saa er der ingen grund til at
 * installere et application repository
 *
 * Men saa skal vi injecte et Handle et sted
 *
 */
// Heh Det er saadan en bean. Hvor vi helst ikke vil have at der bliver pillet ved den. Andet end navn
// Vi skal fx ikke til at injecte ting.
// Saa ma have noget reanonly i bean template
// Fordi vi kan finde configuration senere ved at itererer

/// OKAY I think we make this to a "BuildtimeRepositoryConfiguration"
// That has an provideRepositoryAtRuntime();
// I don't know don't we always??

// Maybe we don't extend it ServiableBean and have a provideAtRuntime();
public final class ApplicationRepositoryConfiguration2<H extends ApplicationHandle<?, ?>> {

    /** The application repository bean handle. */
    final ApplicationRepositoryHandle<H> handle;

    /**
     * @param handle
     *            the bean's handle
     */
    ApplicationRepositoryConfiguration2(ApplicationRepositoryHandle<H> handle) {
        this.handle = handle;
    }

    // Maybe this is just a consumer instead of a function
    public void buildDependecy(String name, Function<? super ApplicationTemplate.Installer<H>, H> installer) {

    }

    // Okay, vi har elastic search in an Assembly. Byg den
    // Expose some services. Og vi har nok en masse shared services.
    // Tror ikke helt vi er klar

    public ProvidableBeanConfiguration<ApplicationRepository<H>> installApplicationRepository() {
        throw new UnsupportedOperationException();
    }

    public void installChildApplication(Consumer<? super ApplicationTemplate.Installer<H>> installer) {
        throw new UnsupportedOperationException();
    }

    /** {@return the templates that are available in the repository) */
    public ApplicationTemplate<?, H> template() {
        throw new UnsupportedOperationException();
    }

    public static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration2<H> install(BaseExtension extension,
            ApplicationTemplate<A, H> template) {
        throw new UnsupportedOperationException();
    }

    public static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration2<H> install(BaseExtensionPoint extension,
            ApplicationTemplate<A, H> template) {
        throw new UnsupportedOperationException();
    }

}
