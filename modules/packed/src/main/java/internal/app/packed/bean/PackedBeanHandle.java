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
package internal.app.packed.bean;

import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.component.Authority;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.util.Key;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingSetup.ManualBindingSetup;
import internal.app.packed.service.InternalServiceUtil;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceSetup;
import sandbox.extension.operation.OperationHandle;

/** Implementation of {@link BeanHandle}. */
public /* value */ record PackedBeanHandle<B extends BeanConfiguration>(BeanSetup bean) implements BeanHandle<B> {

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return bean.beanClass;
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind beanKind() {
        return bean.beanKind;
    }

    /** {@inheritDoc} */
    @Override
    public BeanSourceKind beanSourceKind() {
        return bean.beanSourceKind;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return bean.owner.isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public List<OperationHandle> lifetimeOperations() {
        if (beanKind() != BeanKind.STATIC && beanSourceKind() != BeanSourceKind.SOURCELESS) {
            return List.of(bean.operations.all.get(0).toHandle());
        }
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public void named(String name) {
        checkIsConfigurable();
        bean.named(name);
    }

    /** {@inheritDoc} */
    @Override
    public Authority owner() {
        return bean.owner();
    }

    /** {@inheritDoc} */
    @Override
    public void exportAs(Key<?> key) {
        checkIsConfigurable();
        bean.container.sm.export(key, bean.instanceAccessOperation());
    }

    /** {@inheritDoc} */
    @Override
    public void provideAs(Key<?> key) {
        Key<?> k = InternalServiceUtil.checkKey(bean.beanClass, key);
        checkIsConfigurable();

        if (beanKind() != BeanKind.CONTAINER || beanKind() != BeanKind.LAZY) {
            // throw new UnsupportedOperationException("This method can only be called on beans of kind " + BeanKind.CONTAINER + "
            // or " + BeanKind.LAZY);
        }

        bean.container.sm.provide(k, bean.instanceAccessOperation(), bean.beanInstanceBindingProvider());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bean.toString();
    }

    // add overrideServiceIfPresent? Or have a Set<Key<?>> BeanConfiguration.services()
    public <K> void overrideService(Key<K> key, K instance) {
        // Find any existing bindings for the specified key
        ServiceSetup ss = bean.container.sm.entries.get(key);
        if (ss != null) {
            List<ServiceBindingSetup> l = ss.removeBindingsForBean(bean);
            if (!l.isEmpty()) {
                for (ServiceBindingSetup s : l) {
                    s.operation.bindings[s.operationBindingIndex] = new ManualBindingSetup(s.operation, s.operationBindingIndex, s.operation.bean.owner(),
                            new FromConstant(instance.getClass(), instance));
                }
                return;
            }
        }

        // TODO we should go through all bindings and see if have some where the type matches.
        // But is not resolved as a service

        // Also if we override twice, would be nice with something like. Already overridden
        throw new IllegalArgumentException("Bean '" + componentPath() + "' does not have a dependency for a service with " + key
                + ". Services that can be overridden: " + bean.container.sm.entries.keySet());
    }

    /**
     *
     */
    public void allowMultiClass() {
        checkIsConfigurable();
        bean.multiInstall = bean.multiInstall | 1 << 31;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return bean.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public void componentTags(String... tags) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param key
     * @param supplier
     */
    @Override
    public <K> void addComputedConstant(Key<K> key, Supplier<? extends K> supplier) {
        checkIsConfigurable();
        bean.addCodeGenerated(key, supplier);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public B configuration() {
        return (B) bean.configuration();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentKind componentKind() {
        return ComponentKind.BEAN;
    }
}
