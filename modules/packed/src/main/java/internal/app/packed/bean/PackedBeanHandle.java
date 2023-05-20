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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.OldApplicationPath;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Author;
import app.packed.operation.Op;
import app.packed.util.Key;
import internal.app.packed.binding.BindingResolution.FromConstant;
import internal.app.packed.binding.BindingSetup.ManualBindingSetup;
import internal.app.packed.service.InternalServiceUtil;
import internal.app.packed.service.ServiceBindingSetup;
import internal.app.packed.service.ServiceSetup;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.operation.OperationHandle;

/** Implementation of {@link BeanHandle}. */
public record PackedBeanHandle<T>(BeanSetup bean) implements BeanHandle<T> {

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
            return List.of(bean.operations.get(0).toHandle());
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
    public Author author() {
        return bean.author();
    }

    /** {@inheritDoc} */
    @Override
    public OldApplicationPath path() {
        return bean.path();
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<T> exportAs(Key<? super T> key) {
        checkIsConfigurable();
        bean.container.sm.export(key, bean.instanceAccessOperation());
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void provideAs(Key<? super T> key) {
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

    public <K> void overrideService(Key<K> key, K instance) {
        ServiceSetup ss = bean.container.sm.entries.get(key);
        if (ss != null) {
            List<ServiceBindingSetup> l = ss.removeBindingsForBean(bean);
            if (!l.isEmpty()) {
                for (ServiceBindingSetup s : l) {
                    s.operation.bindings[s.operationBindingIndex] = new ManualBindingSetup(s.operation, s.operationBindingIndex, s.operation.bean.author(),
                            new FromConstant(instance.getClass(), instance));
                }
                return;
            }
        }

        // TODO we should go through all bindings and see if have some where the type matches.
        // But is not resolved as a service

        // Also if we override twice, would be nice with something like. Already overridden
        throw new IllegalArgumentException("Bean does not have any service dependencies on " + key);
    }

    /**
     *
     */
    public void allowMultiClass() {
        checkIsConfigurable();
        bean.multiInstall = bean.multiInstall | 1 << 31;
    }
}

class ZBeanHandleSandbox<T> {

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation) {
        throw new UnsupportedOperationException();
    }

    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
    }

    public <B> void onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super T> consumer) {
        // checkHasInstances
        // We add a operation to this beanhandle...
    }

    public <K> OperationHandle overrideService(Key<K> key, K instance) {
//      if (!beanKind().hasInstances()) {
//      throw new UnsupportedOperationException();
//  }
        throw new UnsupportedOperationException();
    }

    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }
}
