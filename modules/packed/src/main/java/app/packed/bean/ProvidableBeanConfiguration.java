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
package app.packed.bean;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.container.BaseAssembly;
import app.packed.inject.service.ServiceExtension;
import packed.internal.bean.BeanSetup;
import packed.internal.bean.PackedBeanHandle;
import packed.internal.inject.service.ContainerInjectionManager;
import packed.internal.inject.service.InternalServiceUtil;
import packed.internal.inject.service.build.BeanInstanceServiceSetup;

/**
 * A bean which provide an instance(s) of the bean type as a service.
 * <p>
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 */
// Har vi 2 klasser? ServiceConfiguration + ExportableServiceContainer
// Taenker vi kan bruge den ved composer as well.

// Tror vi dropper den her, og saa kun har ProvideableBeanConfiguration
public class ProvidableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    private final ServiceableBean sb;

    public ProvidableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        this.sb = new ServiceableBean(beanHandle);
        handle.addWiringAction(() -> sb.onWired());

    }

    public ProvidableBeanConfiguration<T> export() {
        sb.export();
        return this;
    }

    public ProvidableBeanConfiguration<T> exportAs(Class<? super T> key) {
        sb.export();
        return this;
    }

    public ProvidableBeanConfiguration<T> exportAs(Key<? super T> key) {
        sb.export();
        return this;
    }
    
    
    public ProvidableBeanConfiguration<T> provide() {
        sb.provide();
        return this;
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Key)
     */
    public ProvidableBeanConfiguration<T> provideAs(Class<? super T> key) {
        sb.provideAs(key);
        return this;
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #provideAs(Class)
     */
    public ProvidableBeanConfiguration<T> provideAs(Key<? super T> key) {
        sb.provideAs(key);
        return this;
    }

    // Ser dum ud naar man laver completion
    public Optional<Key<?>> providedAs() {
        return sb.providedAs();
    }

    static final class ServiceableBean {
        final BeanSetup bean;

        Key<?> export;

        Key<?> provide;

        final PackedBeanHandle<?> handle;

        ServiceableBean(PackedBeanHandle<?> handle) {
            this.handle = handle;
            this.bean = handle.bean();
        }

        public void export() {
            export = InternalServiceUtil.checkKey(bean.beanClass(), handle.defaultKey());
            bean.parent.useExtension(ServiceExtension.class);
        }

        public void onWired() {
            if (provide == null && export == null) {
                return;
            }
            ContainerInjectionManager sms = bean.parent.injectionManager;
            BeanInstanceServiceSetup setup = new BeanInstanceServiceSetup(bean, provide);
            if (provide != null) {
                sms.addService(setup);
            }
            if (export != null) {
                sms.ios.exportsOrCreate().export(setup);
            }
        }

        public void provide() {
            provide = InternalServiceUtil.checkKey(bean.beanClass(), handle.defaultKey());
            bean.parent.useExtension(ServiceExtension.class);
        }

        public void provideAs(Class<?> key) {
            provide = InternalServiceUtil.checkKey(bean.beanClass(), key);
            bean.parent.useExtension(ServiceExtension.class);
        }

        public void provideAs(Key<?> key) {
            provide = InternalServiceUtil.checkKey(bean.beanClass(), key);
            bean.parent.useExtension(ServiceExtension.class);
        }

        // Ser dum ud naar man laver completion
        public Optional<Key<?>> providedAs() {
            return Optional.ofNullable(provide);
        }
    }
}
