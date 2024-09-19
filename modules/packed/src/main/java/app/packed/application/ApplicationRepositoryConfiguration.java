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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.assembly.Assembly;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.container.Wirelet;
import app.packed.extension.BaseExtensionPoint;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import internal.app.packed.application.DeploymentSetup;
import internal.app.packed.application.Fut;
import internal.app.packed.application.RuntimeApplicationRepository;
import internal.app.packed.bean.BeanSetup;

/**
 *
 */
// Heh Det er saadan en bean. Hvor vi helst ikke vil have at der bliver pillet ved den. Andet end navn
// Vi skal fx ikke til at injecte ting.
// Saa ma have noget reanonly i bean template
// Fordi vi kan finde configuration senere ved at itererer
public class ApplicationRepositoryConfiguration<H extends ApplicationHandle<?,?>> extends ServiceableBeanConfiguration<ApplicationRepository<H>> {

    private static final BeanTemplate TEMPLATE = BeanTemplate.of(BeanKind.CONTAINER, b -> b.createAs(RuntimeApplicationRepository.class));

    /** The application repository bean handle. */
    final ApplicationRepositoryBeanHandle<H> handle;

    @Override
    public ServiceableBeanConfiguration<ApplicationRepository<H>> provideAs(Class<? super ApplicationRepository<H>> key) {
        super.provideAs(key);
        return this;
    }

    @Override
    public ServiceableBeanConfiguration<ApplicationRepository<H>> provideAs(Key<? super ApplicationRepository<H>> key) {
        super.provideAs(key);
        return this;
    }

    /**
     * @param handle
     *            the bean's handle
     */
    ApplicationRepositoryConfiguration(ApplicationRepositoryBeanHandle<H> handle) {
        super(handle);
        this.handle = handle;
    }

    public void build1(ApplicationTemplate template, String name, Consumer<? super ApplicationTemplate.Installer> installer) {
        BeanSetup bean = BeanSetup.crack(this);
        bean.container.application.children.add(new Fut(handle.bar, template, name, installer));
    }

    @SuppressWarnings("unused")
    public void build2(ApplicationTemplate template, Assembly assembly, Function<? super ApplicationTemplate.Installer, H> newHandle, Wirelet... wirelets) {
        BeanSetup bean = BeanSetup.crack(this);
        DeploymentSetup ds = bean.container.application.deployment;
    }

    public void buildNow(ApplicationTemplate template, Assembly assembly, Consumer<? super ApplicationTemplate.Installer> installer, Wirelet... wirelets) {

    }

    public static <H extends ApplicationHandle<?,?>> ApplicationRepositoryConfiguration<H> install(BaseExtensionPoint point) {
        ApplicationRepositoryBeanHandle<H> h = point.newBean(TEMPLATE).install(RuntimeApplicationRepository.class, ApplicationRepositoryBeanHandle::new);
        return h.configuration();
    }
}
