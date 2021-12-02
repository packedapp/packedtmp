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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

import app.packed.base.Key;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.ContainerBeanConfiguration;
import app.packed.component.ComponentConfiguration;
import app.packed.container.BaseAssembly;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.ComponentSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

/**
 * A bean which provide an instance(s) of the bean type as a service.
 * <p>
 * This class represents the configuration of a component. Actual instances of this interface is usually obtained by
 * calling one of the install methods on, for example, {@link BaseAssembly}.
 */
//ProvidableComponentConfiguration
// Serviceable
public class ServiceBeanConfiguration<T> extends ContainerBeanConfiguration<T> {

    /** A handle that can access superclass private ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentConfiguration.class, "component", ComponentSetup.class),
            MethodType.methodType(BeanSetup.class, BeanConfiguration.class));

    /** {@return the container setup instance that we are wrapping.} */
    private BeanSetup bean() {
        try {
            return (BeanSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact((BeanConfiguration<?>) this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    protected void provideAsService(Key<?> key) {
        bean().sourceProvideAs(key);
    }

    protected Optional<Key<?>> sourceProvideAsKey() {
        return bean().sourceProvideAsKey();
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
        return as(Key.of(key));
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
        provideAsService(key);
        return this;
    }
    public ServiceBeanConfiguration<T> asNone() {
        // Ideen er vi f.eks. kan
        // asNone().exportAs(Doo.class);
        provideAsService(null);
        return this;
    }
    
    public ExportedServiceConfiguration<T> export() {
        return bean().sourceExport();
    }

    // Overvejer at smide... istedet for optional
    public Optional<Key<?>> key() {
        return sourceProvideAsKey();
    }

    // The key unless asNone()

    /** {@inheritDoc} */
    @Override
    public ServiceBeanConfiguration<T> named(String name) {
        super.named(name);
        return this;
    }

    public ServiceBeanConfiguration<T> provide() {
        bean().sourceProvide();
        return this;
    }
}
