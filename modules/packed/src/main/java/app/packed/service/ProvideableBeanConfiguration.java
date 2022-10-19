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

import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.bean.BeanHandle;
import app.packed.bean.InstanceBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.oldservice.InternalServiceExtension;
import internal.app.packed.oldservice.InternalServiceUtil;
import internal.app.packed.oldservice.build.BeanInstanceServiceSetup;

/**
 * A configuration of a container bean.
 */
// ServiceableBeanConfiguration?
public class ProvideableBeanConfiguration<T> extends InstanceBeanConfiguration<T> {

    /** The internal configuration iof the bean. */
    final BeanSetup bean;

    BeanInstanceServiceSetup oldSetup;

    InternalServiceExtension oldSms;

    /**
     * @param handle
     *            the bean driver to use for creating the bean
     */
    public ProvideableBeanConfiguration(BeanHandle<T> handle) {
        super(handle);
        bean = BeanSetup.crack((BeanHandle<?>) handle);
    }

    public Key<T> defaultKey() {
        return handle().defaultKey();
    }

    ProvideableBeanConfiguration<T> describeAs(String description) {
        // describeExportAs
        // describeProvisionAs
        return this;
    }

    public ProvideableBeanConfiguration<T> export() {
        return exportAs(defaultKey());
    }

    public ProvideableBeanConfiguration<T> exportAs(Class<? super T> key) {
        return exportAs(Key.of(key));
    }

    public ProvideableBeanConfiguration<T> exportAs(Key<? super T> key) {
        bean.container.sm.serviceExport(key, bean.accessOperation());
        
        requireNonNull(oldSetup);
        oldSms.ios.exportsOrCreate().export(oldSetup);
        
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWith(Class<K> key, K instance) {
        super.initializeWith(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWith(Key<K> key, K instance) {
        super.initializeWith(key, instance);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWithDelayed(Class<K> key, Supplier<K> supplier) {
        super.initializeWithDelayed(key, supplier);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <K> ProvideableBeanConfiguration<T> initializeWithDelayed(Key<K> key, Supplier<K> supplier) {
        super.initializeWithDelayed(key, supplier);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ProvideableBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ProvideableBeanConfiguration<T> provide() {
        return provideAs(handle().defaultKey());
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
        return provideAs(Key.of(key));
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
        Key<?> k = InternalServiceUtil.checkKey(bean.beanClass, key);
        bean.container.sm.serviceProvide(k, bean.accessOperation());
        
        // Old code
        oldSms = bean.container.injectionManager;
        oldSetup = new BeanInstanceServiceSetup(bean, k);
        oldSms.addService(oldSetup);

        bean.container.useExtension(ServiceExtension.class);
        
        return this;
    }
}
