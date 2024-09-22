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

import static java.util.Objects.requireNonNull;

import java.util.HashMap;

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

class ApplicationRepositoryHandle<H extends ApplicationHandle<?, A>, A> extends BeanHandle<ApplicationRepositoryConfiguration<H, A>> {

    static final PackedBeanTemplate REPOSITORY_TEMPLATE = (PackedBeanTemplate) BeanTemplate.of(BeanKind.CONTAINER,
            b -> b.createAs(PackedApplicationRepository.class));

    private final ExtensionSetup baseExtension;

    private final AuthoritySetup owner;

    final BuildApplicationRepository repository = new BuildApplicationRepository();

    final HashMap<PackedApplicationTemplate<?>, Boolean> templates = new HashMap<>();

    private ApplicationRepositoryHandle(Installer installer, ExtensionSetup baseExtension, AuthoritySetup owner) {
        super(installer);
        this.baseExtension = baseExtension;
        this.owner = owner;
    }

    void addTemplate(PackedApplicationTemplate<?> template) {
        requireNonNull(template);
        if (template.guestClass() == Void.class) {
            return;
        }
        // Only add a guest bean if we haven't seen the template before
        templates.computeIfAbsent(template, t -> {
            Installer i = PackedApplicationTemplate.GB.newInstaller(baseExtension, owner);
            template.installGuestBean(i, m -> repository.onLauncherBuild(template, m));
            return Boolean.TRUE;
        });
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationRepositoryConfiguration<H, A> newBeanConfiguration() {
        return new ApplicationRepositoryConfiguration<>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void onAssemblyClose() {
        bindInstance(BuildApplicationRepository.class, repository);
        BeanSetup.crack(this).container.application.subChildren.add(repository);
    }

    static <A, H extends ApplicationHandle<?, A>> ApplicationRepositoryConfiguration<H, A> install(ExtensionSetup es, AuthoritySetup owner) {
        Installer installer = ApplicationRepositoryHandle.REPOSITORY_TEMPLATE.newInstaller(es, owner);

        ApplicationRepositoryHandle<H, A> h = installer.install(PackedApplicationRepository.class, i -> new ApplicationRepositoryHandle<>(i, es, owner));

        return h.configuration();
    }
}