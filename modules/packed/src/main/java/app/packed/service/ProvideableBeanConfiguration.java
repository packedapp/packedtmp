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

import app.packed.base.Key;
import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.service.InternalServiceExtension;
import internal.app.packed.service.InternalServiceUtil;
import internal.app.packed.service.build.BeanInstanceServiceSetup;

/**
 * A configuration of a container bean.
 */
// ServiceableBeanConfiguration?
public class ProvideableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /** The internal configuration iof the bean. */
    final BeanSetup bean;

    private final OldHelper sb;

    /**
     * @param handle
     *            the bean driver to use for creating the bean
     */
    public ProvideableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        bean = BeanSetup.crack((BeanHandle<?>) handle);
        this.sb = new OldHelper();
    }

    public Key<?> defaultKey() {
        return handle().defaultKey();
    }

    ProvideableBeanConfiguration<T> describeAs(String description) {
        // describeExportAs
        // describeProvisionAs
        return this;
    }

    public ProvideableBeanConfiguration<T> export() {
        bean.container.sm.serviceExport(defaultKey(), bean.accessOperation());
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> exportAs(Class<? super T> key) {
        bean.container.sm.serviceExport(Key.of(key), bean.accessOperation());
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> exportAs(Key<? super T> key) {
        bean.container.sm.serviceExport(key, bean.accessOperation());
        sb.export();
        return this;
    }

    public ProvideableBeanConfiguration<T> provide() {
        Key<?> key = handle().defaultKey();
        bean.container.sm.serviceProvide(key, bean.accessOperation());
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
        bean.container.sm.serviceProvide(Key.of(key), bean.accessOperation());
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
        bean.container.sm.serviceProvide(key, bean.accessOperation());
        sb.provideAs(key);
        return this;
    }

    final class OldHelper {
        BeanInstanceServiceSetup setup;

        InternalServiceExtension sms;

        public void export() {
            requireNonNull(setup);
            sms.ios.exportsOrCreate().export(setup);
            // export = InternalServiceUtil.checkKey(bean.beanClass, handle.defaultKey());
            // bean.container.useExtension(ServiceExtension.class);
        }

        public void provide() {
            Key<?> provide = InternalServiceUtil.checkKey(bean.beanClass, defaultKey());

            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);

            bean.container.useExtension(ServiceExtension.class);
        }

        public void provideAs(Class<?> key) {
            Key<?> provide = InternalServiceUtil.checkKey(bean.beanClass, key);
            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);
        }

        public void provideAs(Key<?> key) {
            Key<?> provide = InternalServiceUtil.checkKey(bean.beanClass, key);
            sms = bean.container.injectionManager;
            setup = new BeanInstanceServiceSetup(bean, provide);
            sms.addService(setup);
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