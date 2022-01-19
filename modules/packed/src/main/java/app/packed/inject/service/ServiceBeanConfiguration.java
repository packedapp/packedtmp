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
package app.packed.inject.service;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import app.packed.base.Key;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanMaker;
import app.packed.bean.UnmanagedBeanConfiguration;
import app.packed.container.BaseAssembly;
import packed.internal.bean.BeanSetup;
import packed.internal.inject.service.ServiceableBean;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * A bean which provide an instance(s) of the bean type as a service.
 * <p>
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 */
// Har vi 2 klasser? ServiceConfiguration + ExportableServiceContainer
// Taenker vi kan bruge den ved composer as well.
public class ServiceBeanConfiguration<T> extends UnmanagedBeanConfiguration<T> {

    /** A var handle that can update the {@link #configuration()} field in this class. */
    private static final VarHandle VH_BEAN_SETUP = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), BeanConfiguration.class, "bean", BeanSetup.class);
    
    private final ServiceableBean sb;
    
    public ServiceBeanConfiguration(BeanMaker<T> handle) {
        super(handle);
        this.sb = new ServiceableBean(bean());
    }

    /**
     * Makes the main component instance available as a service by binding it to the specified key. If the specified key is
     * null, any existing binding is removed.
     *
     * @param key
     *            the key to bind to
     * @return this configuration
     * @see #as(Key)
     */
    public ServiceBeanConfiguration<T> as(Class<? super T> key) {
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
     * @see #as(Class)
     */
    public ServiceBeanConfiguration<T> as(Key<? super T> key) {
        sb.provideAs(key);
        return this;
    }

    /** {@return the container setup instance that we are wrapping.} */
    private BeanSetup bean() {
        try {
            return (BeanSetup) VH_BEAN_SETUP.get((BeanConfiguration<?>) this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

//    public ServiceBeanConfiguration<T> asNone() {
//        // Ideen er vi f.eks. kan
          // exportOnlyAs()
//        // asNone().exportAs(Doo.class);
//        provideAsService(null);
//        return this;
//    }

    public ServiceBeanConfiguration<T> export() {
        sb.export();
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ServiceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    @Override
    protected void onWired() {
        super.onWired();
        sb.onWired();
    }

    public ServiceBeanConfiguration<T> provide() {
        sb.provide();
        return this;
    }
}
