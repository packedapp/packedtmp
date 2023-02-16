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
package app.packed.extension.bean;

import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import app.packed.application.ApplicationPath;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Realm;
import app.packed.context.Context;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.operation.OperationHandle;
import app.packed.operation.Op;
import app.packed.util.Key;
import internal.app.packed.bean.PackedBeanHandle;

/**
 * A bean handle is a reference to an installed bean, private to the extension that installed the bean. Created by
 * {@link BeanInstaller}
 * <p>
 * Instances of {@code BeanHandle} should never be exposed directly to the owner of the bean. Instead a handle should be
 * returned wrapped in {@link BeanConfiguration} (or a subclass hereof).
 */
@SuppressWarnings("rawtypes")
public sealed interface BeanHandle<T> permits PackedBeanHandle {

    // onClientInitialize
    default <B> void afterInitialize(InstanceBeanConfiguration<B> extensionBean, BiConsumer<? super B, ? super T> consumer) {
        //// Ideen er at man fx kan have en BeanHandle<Driver>.onInitialize(MyEBC, (b,p)->b.drivers[i]=p);
        //// Skal godt nok passe paa med capturing her... Ellers faar man hele application setup med i lambdaen

        // Alternativt kan man have noget foo(@SPIInject MyInterface[] mi)..
        // Og saa have registreret interfacet inde
        // Maaske den her bare er bedre...

        // state = Initialized? eller Initializing
        // Take runState???
    }

    /** {@return the bean class.} */
    Class<?> beanClass();

    /** {@return the bean kind.} */
    BeanKind beanKind();

    /** {@return the bean source kind.} */
    BeanSourceKind beanSourceKind();

    /**
     * Checks that the bean is still configurable or throws an {@link IllegalStateException} if not
     * <p>
     * A bean declared by the application is configurable as long as the assembly from which it was installed is
     * configurable. A bean declared by the application is configurable as long as the extension is configurable.
     *
     * @throws IllegalStateException
     *             if the bean is no longer configurable
     */
    default void checkIsConfigurable() {
        if (!isConfigurable()) {
            throw new IllegalStateException("The bean is no longer configurable");
        }
    }

    /** {@return a set of the contexts available for this bean.} */
    Set<Class<? extends Context<?>>> contexts();

    /**
     * Returns the key that the bean will be made available under as default if provided as service.
     *
     * @return the default key
     * @throws UnsupportedOperationException
     *             if called on a bean that has a void bean class
     *
     * @see app.packed.service.ServiceableBeanConfiguration#defaultKey()
     * @see #exportAs(Key)
     * @see #provideAs(Key)
     */
    @SuppressWarnings("unchecked")
    default Key<T> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("This method is not supported for beans with a void bean class");
        }
        // TODO we should read qualifiers on the class
        return (Key<T>) Key.of(beanClass());
    }

    /**
     * Exports the bean as a service with the specified key.
     *
     * @param key
     *            the key to export the bean a
     * @throws ClassCastException
     *             if the specified key is not assignable to the type of the underlying bean
     * @throws KeyAlreadyInUseException
     *             if another service is already exported for the specified key
     * @see #defaultKey()
     * @see #serviceProvideAs(Key)
     */
    void exportAs(Key<? super T> key);

    /**
     * Returns whether or not the bean is still configurable.
     *
     * @return {@code true} if the bean is still configurable
     */
    boolean isConfigurable();

    /**
     * If the bean is registered with its own lifetime. This method returns a list of the lifetime operations of the bean.
     * <p>
     * The operations in the returned list must be computed exactly once. For example, via
     * {@link OperationHandle#generateMethodHandle()}. Otherwise a build exception will be thrown. Maybe this goes for all
     * operation customizers.
     *
     * @return a list of lifetime operations
     *
     * @see BeanInstaller#lifetimes(app.packed.operation.OperationTemplate...)
     */
    List<OperationHandle> lifetimeOperations();

    /**
     * Sets the name of the bean, overriding any existing name.
     *
     * @param name
     *            the name of the bean
     * @throws IllegalArgumentException
     *             if another bean with the specified name already exists
     */
    void named(String name);

    default Class<? extends Extension<?>> operator() {
        return BaseExtension.class;
    }

    /**
     * @param <K>
     * @param key
     * @param instance
     */
    // Skal en handler bruge denne???
    <K> void overrideService(Key<K> key, K instance);

    /**
     * @return
     */
    Realm owner();

    /** {@return the path of the bean.} */
    ApplicationPath path();

    /**
     * Provides of the bean as a service.
     * <p>
     * This method is typically not called directly, but instead via the various provide methods on
     * {@link ProvideableBeanConfiguration}.
     *
     * @param key
     *            the key to provide the service as
     * @throws ClassCastException
     *             if the specified key is not assignable to the type of the underlying bean
     * @throws KeyAlreadyInUseException
     *             if another service is already provided for the specified key
     * @throws UnsupportedOperationException
     *             if bean cannot be provided as a service. Need a new exception
     * @see #defaultKey()
     * @see ProvideableBeanConfiguration#provide()
     * @see ProvideableBeanConfiguration#provideAs(Class)
     * @see ProvideableBeanConfiguration#provideAs(Key)
     */
    void provideAs(Key<? super T> key);

    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    void setErrorHandler(ErrorHandler errorHandler);
}