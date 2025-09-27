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

import app.packed.application.ApplicationHandle;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.binding.Key;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.repository.BuildApplicationRepository;
import internal.app.packed.bean.BeanSetup;

/** A handle for an application repository bean. */
final class ApplicationRegistryBeanHandle<I, H extends ApplicationHandle<I, ?>> extends BeanHandle<ApplicationRegistryConfiguration<I, H>> {

    /** A bean template for a {@link ApplicationRepository} bean. */
    static final BeanTemplate REPOSITORY_BEAN_TEMPLATE = BeanTemplate.of(BeanKind.CONTAINER);

    final BuildApplicationRepository repository;

    ApplicationRegistryBeanHandle(BeanInstaller installer, PackedApplicationTemplate<H> template) {
        this.repository = new BuildApplicationRepository(template);
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> defaultKey() {
        return Key.fromParamaterizedTypes(ApplicationRegistry.class, repository.template.guestClass(), repository.template.handleClass());
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationRegistryConfiguration<I, H> newBeanConfiguration() {
        return new ApplicationRegistryConfiguration<>(this);
    }

    /** {@inheritDoc} */
    @Override
    protected void onClose() {
        bindServiceInstance(BuildApplicationRepository.class, repository);
        BeanSetup.crack(this).container.application.childApplications.add(repository);
    }
}