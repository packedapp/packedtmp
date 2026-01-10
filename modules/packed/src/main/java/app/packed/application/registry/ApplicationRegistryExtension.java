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
import app.packed.bean.Bean;
import app.packed.bean.BeanLifetime;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.FrameworkExtension;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.bean.sidehandle.SidehandleBeanHandle;
import internal.app.packed.application.repository.AbstractApplicationRepository;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.invoke.ServiceSupport;

/**
 * This extension allows creation of applications that consists of other (child) applications. These other applications,
 * may be specified at build time, but can also be installed (and uninstalled) at runtime from a parent application.
 */

// Maybe this is more of Codegen at runtime thing???
// But then again
public final class ApplicationRegistryExtension extends FrameworkExtension<ApplicationRegistryExtension> {

    /**
     * @param handle
     */
    ApplicationRegistryExtension(ExtensionHandle<ApplicationRegistryExtension> handle) {
        super(handle);
    }

    /**
     * Installs a new application repository.
     *
     * @param <A>
     *            the type of application instances all applications in the repository creates
     * @param <H>
     *            the type of handle that represents every application
     * @param template
     *            the application template used for all applications
     * @return a configuration representing the new repository bean
     * @throws IllegalArgumentException
     *             if the specified template has void.class guest type.
     */
    public <A, H extends ApplicationHandle<A, ?>> ApplicationRegistryBeanConfiguration<A, H> installRegistry(ApplicationTemplate<H> template) {
        PackedApplicationTemplate<H> t = (PackedApplicationTemplate<H>) template;
        if (t.guestClass() == Void.class) {
            throw new IllegalArgumentException("Application templates for Void.class guest type are not supported");
        }

        // Create a new repository bean
        ApplicationRegistryBeanHandle<A, H> h = base().newBean(BeanLifetime.SINGLETON)
                .install(Bean.of(AbstractApplicationRepository.repositoryClassFor(template)), i -> new ApplicationRegistryBeanHandle<>(i, t));

        // Create a new installer for the guest bean

        SidehandleBeanHandle<?> gbh = SidehandleBeanHandle.install(t, ExtensionSetup.crack(this), ExtensionSetup.crack(this).container.assembly);
        h.repository.mh = ServiceSupport.newApplicationBaseLauncher(gbh);
        return h.configuration();
    }

    public <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<LaunchableApplication<I>> provideApplication(ApplicationTemplate<H> template,
            Consumer<? super ApplicationInstaller<H>> installer) {
        throw new UnsupportedOperationException();
        // installRepository(template).installApplication(installer);
    }

    public <A, H extends ApplicationHandle<A, ?>> ApplicationRegistryBeanConfiguration<A, H> provideRegistry(ApplicationTemplate<H> template) {
        return installRegistry(template).provide();
    }
}

// Samme type

// ApplicationBeanConfiguration App.installChild(registry, Assembly assembly, Wirelet... wirelets);
// ApplicationBeanConfiguration<App>  basApp = App.install(registry, new BasAppAssembly);
// ApplicationBeanConfiguration<App>  validationApp = App.install(registry, new ValidationAppAssembly()).dependsOn(basApp);
// ApplicationBeanConfiguration<App>  agreementApp = App.install(registry, new AgreementAppAssembly()).dependsOn(basApp);

// Dough, skal jo installeres after vi selv er blevet bygget... Saa bliver noedt til en consumer

//public <I, H extends ApplicationHandle<I, ?>> ProvidableBeanConfiguration<ApplicationLauncher<I>> provideNewApplication(ApplicationTemplate<H> template,
//        Assembly assembly, Wirelet... wirelets) {
//    throw new UnsupportedOperationException();
//}

// Okay, vi har elastic search in an Assembly. Byg den
// Expose some services. Og vi har nok en masse shared services.
// Tror ikke helt vi er klar
// Ved ikke om det har noget med repository at goere. Det tror jeg egentlig ikke...
// Altsaa hvorfor ikke bare have en extension der exposer nogle beans?
// Saa maa de selv om de vil bruge Packed

// Maybe you want an image???
//public void newDependecy(Consumer<? super ApplicationInstaller<?>> installer) {
//    throw new UnsupportedOperationException();
//}

// We need to fail on a managed appplication template in an unmanaged container
