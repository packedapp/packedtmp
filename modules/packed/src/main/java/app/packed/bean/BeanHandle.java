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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.base.NamespacePath;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.oldservice.InternalServiceUtil;

/**
 * A bean handle is a reference to an installed bean, private to the extension that installed the bean.
 * <p>
 * Instances of {@code BeanHandle} are never exposed directly to end-users. Instead they are returned wrapped in
 * {@link BeanConfiguration} or a subclass hereof.
 */
public final /* primitive */ class BeanHandle<T> {

    /** The bean we are wrapping. */
    private final BeanSetup bean;

    /**
     * Creates a new BeanHandle.
     * 
     * @param bean
     *            the bean to wrap
     */
    BeanHandle(BeanSetup bean) {
        this.bean = requireNonNull(bean);
    }

    // We need a extension bean
    // Dem der resolver bindings, skal goeres mens man introspector...
    public OperationHandle addFunctionalOperation(InstanceBeanConfiguration<?> operator, Class<?> functionalInterface, OperationType type,
            Object functionInstance) {
        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    /** {@return the bean class.} */
    public Class<?> beanClass() {
        return bean.beanClass;
    }

    /** {@return the bean kind.} */
    public BeanKind beanKind() {
        return bean.beanKind;
    }

    public void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The bean is no longer configurable");
        }
    }

    /**
     * Returns the key that the bean will be made available under if provided.
     * 
     * @return
     * 
     * @see ProvideableBeanConfiguration#provide()
     * @see ProvideableBeanConfiguration#export()
     * @throws UnsupportedOperationException
     *             if called on a functional bean {@code (beanClass == void.class)}
     */
    @SuppressWarnings("unchecked")
    public Key<T> defaultKey() {
        if (beanKind() == BeanKind.FUNCTIONAL) {
            throw new UnsupportedOperationException("This method is not supported for functional beans");
        }
        return (Key<T>) Key.of(beanClass());
    }

    /**
     * Returns whether or not the bean is still configurable.
     * 
     * @return {@code true} if the bean is still configurable
     */
    public boolean isConfigurable() {
        return !bean.realm.isClosed();
    }

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#computeMethodHandleInvoker()}. Otherwise a build exception will be thrown. Maybe this goes for
     * all operation customizers.
     * 
     * @return
     */
    // Ideen er jo foerst og fremmest taenkt paa 
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    public void named(String name) {
        checkIsConfigurable();
        bean.named(name);
    }

    public <K> OperationHandle overrideService(Key<K> key, K instance) {
        checkIsConfigurable();

        throw new UnsupportedOperationException();
    }

    public NamespacePath path() {
        return bean.path();
    }

    // Spoergsmaalet er om vi vil have dem her...
    // Eller kun i ProvideBeanConfiguration
    public void serviceExportAs(Key<? super T> key) {
        checkIsConfigurable();

        bean.container.sm.serviceExport(key, bean.accessOperation());
        
        
        bean.container.injectionManager.ios.exportsOrCreate().export(bean, null);


    }

    /**
     * Provides of the bean as a service.
     * <p>
     * This method is rarely called directly, but instead via the various provide methods on
     * {@link ProvideableBeanConfiguration}.
     * 
     * @param key
     *            the key of the provided serviceto which to provide the service as
     * @throws ClassCastException
     *             if a service could not be provided because the key is not assignable to the type of the underlying bean
     * @throws UnsupportedOperationException
     *             if instances of the bean cannot be provided as a service
     * 
     * @see ProvideableBeanConfiguration#provide()
     * @see ProvideableBeanConfiguration#provideAs(Class)
     * @see ProvideableBeanConfiguration#provideAs(Key)
     */
    public void serviceProvideAs(Key<? super T> key) {
        Key<?> k = InternalServiceUtil.checkKey(bean.beanClass, key);
        checkIsConfigurable();

        if (beanKind() != BeanKind.CONTAINER || beanKind() != BeanKind.LAZY) {
            // throw new UnsupportedOperationException("This method can only be called on beans of kind " + BeanKind.CONTAINER + "
            // or " + BeanKind.LAZY);
        }

        bean.container.sm.provideService(k, beanKind() != BeanKind.MANYTON, bean.accessOperation());
    }

    /**
     * Sets a supplier that creates a special bean mirror instead of the generic {@code BeanMirror} when requested.
     * 
     * @param supplier
     *            the supplier used to create the bean mirror
     * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
     *          must be returned
     */
    // I think move it to the installer. Why wait?
    public void specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        checkIsConfigurable();
        bean.mirrorSupplier = supplier;
    }

    /** {@inheritDoc} */
    public String toString() {
        return bean.toString();
    }
}

class BeanHandleSandbox<T> {

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, MethodHandle methodHandle) {
        return addOperation(operator, Op.ofMethodHandle(methodHandle));
    }

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation) {
        throw new UnsupportedOperationException();
    }
    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
    }
    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }

}