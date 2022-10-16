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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.newInject.ServiceManager;
import internal.app.packed.service.InternalServiceExtension;
import internal.app.packed.service.InternalServiceUtil;
import internal.app.packed.service.build.BeanInstanceServiceSetup;

/**
 * A configuration of a container bean.
 */
// ServiceableBeanConfiguration?
public class ProvideableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    private final ServiceableBean sb;

    final ServiceManager sm;

    final BeanSetup bean;

    /**
     * @param handle
     *            the bean driver to use for creating the bean
     */
    public ProvideableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        bean = BeanSetup.crack((BeanHandle<?>) handle);
        sm = bean.container.sm;

        this.sb = new ServiceableBean((BeanHandle<?>) handle);
    }

    public ProvideableBeanConfiguration<T> export() {
        Key<?> key = handle().defaultKey();
        sm.addExport(key, OperationSetup.beanAccess(bean));
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> exportAs(Class<? super T> key) {
        sm.addExport(Key.of(key), OperationSetup.beanAccess(bean));
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> exportAs(Key<? super T> key) {
        sm.addExport(key, OperationSetup.beanAccess(bean));
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> provide() {
        Key<?> key = handle().defaultKey();
        sm.addProvision(key, OperationSetup.beanAccess(bean));
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
    public ProvideableBeanConfiguration<T> provideAs(Class<? super T> key) {
        sm.addProvision(Key.of(key), OperationSetup.beanAccess(bean));
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
    public ProvideableBeanConfiguration<T> provideAs(Key<? super T> key) {
        sm.addProvision(key, OperationSetup.beanAccess(bean));
        sb.provideAs(key);
        return this;
    }

    // Ser dum ud naar man laver completion
    // Return set???
    // also for Export
//    public Optional<Key<?>> providedAs() {
//        return sb.providedAs();
//    }

    ProvideableBeanConfiguration<T> describeAs(String description) {
        // describeExportAs
        // describeProvisionAs
        return this;
    }

    static final class ServiceableBean {
        final BeanSetup bean;

        Key<?> export;

        Key<?> provide;

        final BeanHandle<?> handle;

        ServiceableBean(BeanHandle<?> handle) {
            this.handle = handle;
            this.bean = BeanSetup.crack(handle);
        }

        public void export() {
            requireNonNull(setup);
            sms.ios.exportsOrCreate().export(setup);
            // export = InternalServiceUtil.checkKey(bean.beanClass, handle.defaultKey());
            // bean.container.useExtension(ServiceExtension.class);
        }

        InternalServiceExtension sms;
        BeanInstanceServiceSetup setup;

        public void onWired() {
            if (provide == null && export == null) {
                return;
            }

            InternalServiceExtension sms = bean.container.injectionManager;
            BeanInstanceServiceSetup setup = new BeanInstanceServiceSetup(bean, provide);
            if (provide != null) {
                sms.addService(setup);
            }
            if (export != null) {
                sms.ios.exportsOrCreate().export(setup);
            }
        }

        public void provide() {
            provide = InternalServiceUtil.checkKey(bean.beanClass, handle.defaultKey());

            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);

            bean.container.useExtension(ServiceExtension.class);
            provide = null;
        }

        public void provideAs(Class<?> key) {
            provide = InternalServiceUtil.checkKey(bean.beanClass, key);
            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);
            provide = null;
        }

        public void provideAs(Key<?> key) {
            provide = InternalServiceUtil.checkKey(bean.beanClass, key);
            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);
            provide = null;
        }

        // Ser dum ud naar man laver completion
        public Optional<Key<?>> providedAs() {
            return Optional.ofNullable(provide);
        }
    }
}
//Tror ikke den kan noget PBC ikke kan.
//Men vi kan kraeve den som parameter nogle steder.

//Nu har vi en.. Saa skal vi have lagt de services ting ud i handle...
//Maaske paanaer export()??? Hoere vel til export extension

//
//public <X extends Runnable & Callable<String>> X foo() {
//  return null;
//}