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

import app.packed.application.ApplicationHandle;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;

/**
 *
 */
class ApplicationLauncherHandle<T> extends BeanHandle<ProvidableBeanConfiguration<T>>{

    /**
     * @param installer
     */
    public ApplicationLauncherHandle(BeanInstaller installer) {
        super(installer);
    }

    @SuppressWarnings("unused")
    private static final PackedBeanTemplate REPOSITORY_BEAN_TEMPLATE = (PackedBeanTemplate) BeanTemplate.of(BeanKind.CONTAINER,
            b -> b.createAs(ApplicationLauncher.class));


    static <A, H extends ApplicationHandle<A, ?>> ProvidableBeanConfiguration<ApplicationLauncher<A>> install(PackedApplicationTemplate<H> template, ExtensionSetup es,
            AuthoritySetup<?> owner) {

        throw new UnsupportedOperationException();

        // Install a ApplicationRepository
//        ApplicationRepositoryHandle<A, H> h = REPOSITORY_BEAN_TEMPLATE.newInstaller(es, owner)
//                .install(AbstractApplicationRepository.repositoryClassFor(template), i -> new ApplicationRepositoryHandle<>(i, template));
//
//        // Create a new installer for the guest bean
//        BeanInstaller i = PackedApplicationTemplate.GB.newInstaller(es, owner);
//        template.installGuestBean(i, h.repository::onCodeGenerated);
//
//        return h.configuration();
    }
}
