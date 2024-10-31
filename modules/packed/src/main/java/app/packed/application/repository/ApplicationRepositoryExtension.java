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

import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationInstaller;
import app.packed.application.ApplicationTemplate;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.repository.AbstractApplicationRepository;
import internal.app.packed.extension.ExtensionSetup;

/**
 * This extension allows creation of applications that consists of other applications. These other applications, may be
 * specified at build time. But they can also be installed at runtime in a parent application.
 */
public final class ApplicationRepositoryExtension extends Extension<ApplicationRepositoryExtension> {

    private static final BeanTemplate REPOSITORY_BEAN_TEMPLATE = BeanTemplate.of(BeanKind.CONTAINER);

    /**
     * @param handle
     */
    ApplicationRepositoryExtension(ExtensionHandle<ApplicationRepositoryExtension> handle) {
        super(handle);
    }

    // Dough, skal jo installeres after vi selv er blevet bygget...

//    public <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<ApplicationLauncher<I>> provideNewApplication(ApplicationTemplate<H> template,
//            Assembly assembly, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }

    // Okay, vi har elastic search in an Assembly. Byg den
    // Expose some services. Og vi har nok en masse shared services.
    // Tror ikke helt vi er klar
    // Ved ikke om det har noget med repository at goere. Det tror jeg egentlig ikke...
    // Altsaa hvorfor ikke bare have en extension der exposer nogle beans?
    // Saa maa de selv om de vil bruge Packed

    // Maybe you want an image???
    public void newDependecy(Consumer<? super ApplicationInstaller<?>> installer) {
        throw new UnsupportedOperationException();
    }

    // We need to fail on a managed appplication template in an unmanaged container

    /**
     * @param <A>
     *            the type of application instances all applications in the repository creates
     * @param <H>
     *            the type of handle that represents every application
     * @param template
     *            the application template used for all applications
     * @return a configuration representing the new repository bean
     */
    public <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> installRepository(ApplicationTemplate<H> template) {
        PackedApplicationTemplate<H> t = (PackedApplicationTemplate<H>) template;
        if (t.guestClass() == Void.class) {
            throw new IllegalArgumentException("Application templates with Void.class guest type are not supported");
        }

        ApplicationRepositoryBeanHandle<A, H> h = base().newBean(REPOSITORY_BEAN_TEMPLATE).install(AbstractApplicationRepository.repositoryClassFor(template),
                i -> new ApplicationRepositoryBeanHandle<>(i, t));

        // Create a new installer for the guest bean
        h.repository.mh = GuestBeanHandle.install(t, ExtensionSetup.crack(this), ExtensionSetup.crack(this).container.assembly);
        return h.configuration();
    }

    public <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<InstalledApplication<I>> provideApplication(ApplicationTemplate<H> template,
            Consumer<? super ApplicationInstaller<H>> installer) {
        throw new UnsupportedOperationException();
    }

    public <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> provideRepository(ApplicationTemplate<H> template) {
        return installRepository(template).provide();
    }
}
