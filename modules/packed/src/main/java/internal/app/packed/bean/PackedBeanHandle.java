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
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.application.ApplicationPath;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Realm;
import app.packed.context.Context;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.BeanHandle;
import app.packed.extension.DelegatingOperationHandle;
import app.packed.extension.OperationHandle;
import app.packed.operation.Op;
import app.packed.util.FunctionType;
import app.packed.util.Key;
import internal.app.packed.service.InternalServiceUtil;

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
    public Set<Class<? extends Context<?>>> contexts() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        return bean.owner.isConfigurable();
    }

    /** {@inheritDoc} */
    @Override
    public List<OperationHandle> lifetimeOperations() {
        if (beanKind() != BeanKind.STATIC && beanSourceKind() != BeanSourceKind.NONE) {
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

    protected DelegatingOperationHandle newDelegationFunctionalOperation(Class<?> functionalInterface, Object function, FunctionType operationType) {
        // We only take public exported types
        throw new UnsupportedOperationException();
    }

    protected OperationHandle newFunctionalOperation(Class<?> functionalInterface, Object function, FunctionType operationType) {
        throw new UnsupportedOperationException();
    }

    // We need a extension bean
    // Dem der resolver bindings, skal goeres mens man introspector...
    // Burde have en OperationType uden annoteringer
    // Maaske bare stripper annoteringer...
    // Men okay vi kan stadig fx bruge Logger som jo stadig skulle
    // supplies uden et hook
    @Override
    public OperationHandle newFunctionalOperation(InstanceBeanConfiguration<?> operator, Class<?> functionalInterface, FunctionType type,
            Object functionInstance) {
        // I think we can ignore the operator now.

        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Realm owner() {
        return bean.owner();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationPath path() {
        return bean.path();
    }

    /** {@inheritDoc} */
    @Override
    public void exportAs(Key<? super T> key) {
        checkIsConfigurable();
        bean.container.sm.export(key, bean.instanceAccessOperation());
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
    public void setErrorHandler(ErrorHandler errorHandler) {}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bean.toString();
    }
}

class ZBeanHandleSandbox<T> {

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation) {
        throw new UnsupportedOperationException();
    }

    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
    }

    /**
     * @param <K>
     *            the type of key
     * @param key
     *            the key
     * @param instance
     *            the instance to inject
     *
     * @throws UnsupportedOperationException
     *             if the bean handle does not have instances
     *
     * @see InstanceBeanConfiguration#initializeWithInstance(Class, Object)
     * @see InstanceBeanConfiguration#initializeWithInstance(Key, Object)
     */
    public <K> void initializeWithInstance(Key<K> key, K instance) {
//        if (!beanKind().hasInstances()) {
//            throw new UnsupportedOperationException();
//        }
        throw new UnsupportedOperationException();
    }

    public <B> void onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super T> consumer) {
        // checkHasInstances
        // We add a operation to this beanhandle...
    }

    public <K> OperationHandle overrideService(Key<K> key, K instance) {
        // checkIsConfigurable();

        throw new UnsupportedOperationException();
    }

    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }

}
