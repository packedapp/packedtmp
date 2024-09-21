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

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanTemplate.Installer;
import internal.app.packed.application.BuildApplicationRepository;
import internal.app.packed.bean.BeanSetup;

class ApplicationRepositoryHandle<H extends ApplicationHandle<?, A>, A> extends BeanHandle<ApplicationRepositoryConfiguration<H, A>> {

    final BuildApplicationRepository bar;

    final ApplicationTemplate<A> template;

    ApplicationRepositoryHandle(Installer installer, ApplicationTemplate<A> template) {
        super(installer);
        this.template = requireNonNull(template);
        this.bar = new BuildApplicationRepository(template);
    }

    @Override
    protected ApplicationRepositoryConfiguration<H, A> newBeanConfiguration() {
        return new ApplicationRepositoryConfiguration<>(this);
    }

    @Override
    protected void onAssemblyClose() {
        bindInstance(BuildApplicationRepository.class, bar);
        BeanSetup.crack(this).container.application.subChildren.add(bar);
    }
}