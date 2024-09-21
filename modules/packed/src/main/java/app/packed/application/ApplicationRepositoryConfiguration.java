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

import app.packed.bean.BeanKind;
import app.packed.bean.BeanTemplate;
import app.packed.bean.BeanTemplate.Installer;
import app.packed.extension.BaseExtensionPoint;
import app.packed.service.ServiceableBeanConfiguration;
import app.packed.util.Key;
import internal.app.packed.application.PackedApplicationTemplate;
import internal.app.packed.application.RuntimeApplicationRepository;

/**
 *
 */
// Heh Det er saadan en bean. Hvor vi helst ikke vil have at der bliver pillet ved den. Andet end navn
// Vi skal fx ikke til at injecte ting.
// Saa ma have noget reanonly i bean template
// Fordi vi kan finde configuration senere ved at itererer
public final class ApplicationRepositoryConfiguration<H extends ApplicationHandle<?, A>, A> extends ServiceableBeanConfiguration<ApplicationRepository<H>> {

    private static final BeanTemplate REPOSITORY_TEMPLATE = BeanTemplate.of(BeanKind.CONTAINER, b -> b.createAs(RuntimeApplicationRepository.class));

    /** The application repository bean handle. */
    final ApplicationRepositoryHandle<H, A> handle;

    /**
     * @param handle
     *            the bean's handle
     */
    ApplicationRepositoryConfiguration(ApplicationRepositoryHandle<H, A> handle) {
        super(handle);
        this.handle = handle;
    }

    public void buildLater(String name, Consumer<? super ApplicationTemplate.Installer<A>> installer) {
        handle.bar.add(name, installer);
    }

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

    /** {@return the template being used for the repository) */
    public ApplicationTemplate<A> template() {
        return handle.template;
    }

    // We do not support configurable templates for a repository.
    // We need to build the guest bean together with the application
    // So we cannot take new templates as runtime. Unless we want like a small app like BootstrapApp inbetween
    public static <A, H extends ApplicationHandle<?, A>> ApplicationRepositoryConfiguration<H, A> install(BaseExtensionPoint point,
            ApplicationTemplate<A> template) {
        ApplicationRepositoryHandle<H, A> h = point.newBean(REPOSITORY_TEMPLATE).install(RuntimeApplicationRepository.class,
                i -> new ApplicationRepositoryHandle<>(i, template));
        PackedApplicationTemplate<A> pat = (PackedApplicationTemplate<A>) template;
        Installer i = point.newBean(PackedApplicationTemplate.GB);
        pat.installGuestBean(i, m -> h.bar.guest = m);
        return h.configuration();
    }
}
