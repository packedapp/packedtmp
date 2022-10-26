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
import app.packed.container.Extension;
import app.packed.lifetime.LifetimeConf;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import app.packed.service.ProvideableBeanConfiguration;
import internal.app.packed.bean.BeanInstaller;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.oldservice.InternalServiceUtil;

/**
 * A bean handle represents a successful installation of a bean.
 * <p>
 * Instances of {@code BeanHandle} are never exposed directly to end-users. Instead they are returned wrapped in
 * {@link BeanConfiguration} or a subclass hereof.
 */
public final /* primitive */ class BeanHandle<T> {

    /** The configuration of the bean we are wrapping. */
    final BeanSetup bean;

    /**
     * Creates a new BeanHandle.
     * 
     * @param bean
     *            the configuration of the bean we wrap
     */
    BeanHandle(BeanSetup bean) {
        this.bean = requireNonNull(bean);
    }

    // We need a extension bean
    public OperationHandle addFunctionalOperation(InstanceBeanConfiguration<?> operator, Class<?> functionalInterface, OperationType type,
            Object functionInstance) {
        // Function, OpType.of(void.class, HttpRequest.class, HttpResponse.class), someFunc)
        throw new UnsupportedOperationException();
    }

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, MethodHandle methodHandle) {
        return addOperation(operator, Op.ofMethodHandle(methodHandle));
    }

    public OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation) {
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

    public void decorateInstance(Function<? super T, ? extends T> decorator) {
        throw new UnsupportedOperationException();
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
    public List<OperationHandle> lifetimeOperations() {
        return List.of();
    }

    public <K> OperationHandle overrideService(Key<K> key, K instance) {
        throw new UnsupportedOperationException();
    }

    public void peekInstance(Consumer<? super T> consumer) {
        throw new UnsupportedOperationException();
    }

    public void serviceExportAs(Key<? super T> key) {
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
    public void specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        bean.mirrorSupplier = supplier;
    }

    /**
     * An builder that can used by extensions to install new beans.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     * 
     * @see BeanExtensionPoint#builder(BeanKind)
     * @see BeanExtensionPoint#builder(BeanKind, app.packed.container.ExtensionPoint.UseSite)
     */
    public sealed static abstract class Builder permits BeanInstaller {

        /**
         * Installs the bean using the specified class as the bean source.
         * 
         * @param <T>
         *            the
         * @param beanClass
         * @return a bean handle representing the installed bean
         */
        public abstract <T> BeanHandle<T> build(Class<T> beanClass);

        public abstract <T> BeanHandle<T> build(Op<T> operation);

        public abstract <T> BeanHandle<T> buildFromInstance(T instance);

        public abstract BeanHandle<Void> buildSourceless();

        protected <T> BeanHandle<T> from(BeanSetup bs) {
            return new BeanHandle<>(bs);
        }

        /**
         * An option that allows for a special bean introspector to be used when introspecting the bean for the extension.
         * Normally, the runtime would call {@link Extension#newBeanIntrospector} to obtain an introspector for the registering
         * extension.
         * 
         * @param introspector
         *            the introspector to use
         * @return the option
         * @see Extension#newBeanIntrospector
         */
        public abstract Builder introspectWith(BeanIntrospector introspector);

        public Builder lifetimes(LifetimeConf... confs) {
            return this;
        }

        /**
         * Allows multiple beans of the same type in a container.
         * <p>
         * By default, a container only allows a single bean of particular type if non-void.
         * 
         * @return this builder
         * @throws UnsupportedOperationException
         *             if bean kind is {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC}
         */
        public abstract Builder multiInstall();

        public abstract Builder namePrefix(String prefix);

        public abstract Builder onlyInstallIfAbsent(Consumer<? super BeanHandle<?>> onInstall);

        Builder spawnNew() {
            // A bean that is created per operation.
            // Obvious manyton, but should we have own kind?
            // I actually think so because, because for now it always requires manyton

            // Some questions, do we support @Schedule? Or anything like it?
            // I don't think we need to set up the support for it by default. Only if used
            // So overhead is not needed

            // But I think those annotations that make sense are always "callback" extensions
            // From other threads
            // Single threaded vs multi-threaded
            // If we are single threaded it is obviously always only the request method
            // If we are multi threaded we create own little "world"
            // I think that is the difference, between the two

            // Maybe bean is always single threaded.
            // And container is always multi threaded

            throw new UnsupportedOperationException();
        }

        /**
         * Marks the bean as synthetic.
         * 
         * @return this installer
         */
        public abstract Builder synthetic();
    }
}
