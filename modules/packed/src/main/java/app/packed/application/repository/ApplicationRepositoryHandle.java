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
import app.packed.application.ApplicationTemplate;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.bean.BeanTemplate.Installer;
import internal.app.packed.application.BuildApplicationRepository;
import internal.app.packed.application.PackedApplicationRepository;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;

class ApplicationRepositoryHandle<A, H extends ApplicationHandle<A, ?>> extends BeanHandle<ApplicationRepositoryConfiguration<A, H>> {

    static final PackedBeanTemplate REPOSITORY_BEAN_TEMPLATE = (PackedBeanTemplate) BeanTemplate.of(BeanKind.CONTAINER,
            b -> b.createAs(PackedApplicationRepository.class));

    BuildApplicationRepository repository;

    private ApplicationRepositoryHandle(Installer installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationRepositoryConfiguration<A, H> newBeanConfiguration() {
        return new ApplicationRepositoryConfiguration<>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void onAssemblyClose() {
        bindServiceInstance(BuildApplicationRepository.class, repository);
        BeanSetup.crack(this).container.application.subChildren.add(repository);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(ApplicationTemplate<A, H> template, ExtensionSetup es,
            AuthoritySetup<?> owner) {
        PackedApplicationTemplate<A, H> t = (PackedApplicationTemplate<A, H>) template;
        if (t.guestClass() == Void.class) {
            throw new UnsupportedOperationException("Does not support application templates of Void.class guest type");
        }

        // Create a new installer for the repository bean
        Installer installer = ApplicationRepositoryHandle.REPOSITORY_BEAN_TEMPLATE.newInstaller(es, owner);
        ApplicationRepositoryHandle<A, H> h = installer.install(PackedApplicationRepository.class, ApplicationRepositoryHandle::new);
        BuildApplicationRepository repository = h.repository = new BuildApplicationRepository(t);

        // Create a new installer for the guest bean
        Installer i = PackedApplicationTemplate.GB.newInstaller(es, owner);
        t.installGuestBean(i, repository::onCodeGenerated);

        return h.configuration();
    }
}