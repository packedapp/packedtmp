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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.ParameterizedType;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.binding.Key;
import app.packed.binding.Variable;
import internal.app.packed.application.GuestBeanHandle;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.repository.AbstractApplicationRepository;
import internal.app.packed.application.repository.BuildApplicationRepository;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanTemplate;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.util.types.Types;

class ApplicationRepositoryHandle<I, H extends ApplicationHandle<I, ?>> extends BeanHandle<ApplicationRepositoryConfiguration<I, H>> {

    private static final PackedBeanTemplate REPOSITORY_BEAN_TEMPLATE = (PackedBeanTemplate) BeanTemplate.of(BeanKind.CONTAINER,
            b -> b.createAs(AbstractApplicationRepository.class));

    final BuildApplicationRepository repository;

    private ApplicationRepositoryHandle(BeanInstaller installer, PackedApplicationTemplate<H> template) {
        super(installer);
        this.repository = new BuildApplicationRepository(template);
    }

    @Override
    public Key<?> defaultKey() {
        // We need to know the template type either, from the template itself, or when creating the repository.
        // Think we might as well just do it in the template
        ParameterizedType p = Types.createNewParameterizedType(ApplicationRepository.class, repository.template.guestClass(),
                repository.template.handleClass());
        Variable v = Variable.of(p);
        Key<?> key = Key.fromVariable(v);
        return key;
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationRepositoryConfiguration<I, H> newBeanConfiguration() {
        return new ApplicationRepositoryConfiguration<>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        bindServiceInstance(BuildApplicationRepository.class, repository);
        BeanSetup.crack(this).container.application.subChildren.add(repository);
    }

    static <A, H extends ApplicationHandle<A, ?>> ApplicationRepositoryConfiguration<A, H> install(PackedApplicationTemplate<H> template, ExtensionSetup es,
            AuthoritySetup<?> owner) {
        // Install a ApplicationRepository
        ApplicationRepositoryHandle<A, H> h = REPOSITORY_BEAN_TEMPLATE.newInstaller(es, owner)
                .install(AbstractApplicationRepository.repositoryClassFor(template), i -> new ApplicationRepositoryHandle<>(i, template));

        // Create a new installer for the guest bean
        BeanInstaller i = GuestBeanHandle.GUEST_BEAN_TEMPLATE.newInstaller(es, owner);
        MethodHandle mh = GuestBeanHandle.installGuestBean(template, i);
        h.repository.onCodeGenerated(mh);
        return h.configuration();
    }
}