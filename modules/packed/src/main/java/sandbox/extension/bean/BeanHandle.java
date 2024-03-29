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
package sandbox.extension.bean;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanLocalAccessor;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.component.Authority;
import app.packed.component.ComponentHandle;
import app.packed.component.InstalledComponent;
import app.packed.errorhandling.ErrorHandler;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.util.Key;
import internal.app.packed.bean.PackedBeanHandle;
import internal.app.packed.bean.PackedBeanHandleBuilder;
import internal.app.packed.context.publish.ContextualizedElement;
import sandbox.extension.operation.OperationHandle;

/**
 * A bean handle is a build-time reference to an installed bean.
 * <p>
 * Instances of {@code BeanHandle} should never be exposed outside of the extension that created the bean. Instead a
 * handle should be returned wrapped in {@link BeanConfiguration} (or a subclass hereof).
 *
 * @see BeanBuilder#install(Class)
 * @see BeanBuilder#install(Op)
 * @see BeanBuilder#installIfAbsent(Class, java.util.function.Consumer)
 * @see BeanBuilder#installInstance(Object)
 */
public sealed interface BeanHandle extends ComponentHandle , ContextualizedElement , BeanLocalAccessor permits PackedBeanHandle {

    /** {@return the bean class.} */
    Class<?> beanClass();

    /** {@return the bean kind.} */
    BeanKind beanKind();

    /** {@return the bean source kind.} */
    BeanSourceKind beanSourceKind();

    // Primarily specified by the user and for used for building or mirrors
    void componentTags(String... tags);

    default <C extends BeanConfiguration> C configure(Function<BeanHandle, C> configure) {
        return configure.apply(this);
    }

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
    default Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("This method is not supported for beans with a void bean class");
        }
        return Key.fromClass(beanClass());
    }

    default BeanHandle exportAs(Class<?> key) {
        return exportAs(Key.of(key));
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
    BeanHandle exportAs(Key<?> key);

    /**
     * Returns a list of operation handles that corresponds to the {@link BeanTemplate#lifetimeOperations() lifetime
     * operations} on the template that created the bean.
     *
     * @return a list of lifetime operation handles
     *
     * @see BeanTemplate#lifetimeOperations()
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

    /**
     * Prepares t
     *
     * @param function
     *            the user defined function that should be invoked
     * @return a new operation handle handle
     */
    default OperationHandle.Builder newFunctionalOperation(Object function) {
        // De giver faktisk ret god mening at tage funktionen nu
        // Det er jo ligesom at BeanMethod giver metoden videre til builderen
        throw new UnsupportedOperationException();
    }

    /** {@return the owner of the bean} */
    Authority owner();

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
    void provideAs(Key<?> key);

    /**
     * An installer for installing beans into a container.
     * <p>
     * The various install methods can be called multiple times to install multiple beans. However, the use cases for this
     * are limited.
     *
     * @see BaseExtensionPoint#newBean(BeanKind)
     * @see BaseExtensionPoint#newBeanForExtension(BeanKind, app.packed.extension.ExtensionPoint.UseSite)
     */
    // The reason we have Builder and not just 1 class. Is because of the scanning.
    // It is super confusing what you can do before and after
    // But maybe this is better...
    public sealed interface Builder permits PackedBeanHandleBuilder {

        /**
         * Installs the bean using the specified class as the bean source.
         *
         * @param <T>
         *            the type of bean
         * @param beanClass
         *            the bean class
         * @return a bean handle representing the installed bean
         *
         * @see app.packed.bean.BeanSourceKind#CLASS
         */
        <T> BeanHandle install(Class<T> beanClass);

        /**
         * @param <T>
         * @param <C>
         * @param beanClass
         * @param newConfiguration
         *            a supplier for the configuration that should be returned to the owner of the new bean
         * @return
         */
        <T, C extends BeanConfiguration> InstalledComponent<BeanHandle, C> install(Class<T> beanClass, Supplier<? extends C> newConfiguration);

        <T> BeanHandle install(Op<T> operation);

        // These things can never be multi
        // AbsentInstalledComponent(boolean wasInstalled)
        <T> BeanHandle installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle> onInstall);

        // We will need to remove <T> from BeanHandle, unless we want to specify a class when we create the handle
        default <T, C extends BeanConfiguration> C installIfAbsent2(Class<T> beanClass, Function<? super BeanHandle, C> onInstall) {
            throw new UnsupportedOperationException();
        }

        // instance = introspected bean
        // constant = non-introspected bean
        <T> BeanHandle installInstance(T instance);

        BeanHandle installSourceless();

        Builder namePrefix(String prefix);

        /**
         * Sets the value of the specified bean local for the new bean.
         *
         * @param <T>
         *            the type of value the bean local holds
         * @param local
         *            the bean local to set
         * @param value
         *            the value of the local
         * @return this builder
         */
        <T> Builder setLocal(BeanLocal<T> local, T value);

        /**
         * Sets a supplier that creates a special bean mirror instead of a generic {@code BeanMirror} if a mirror for the bean
         * is requested.
         *
         * @param supplier
         *            the supplier used to create the bean mirror
         * @apiNote the specified supplier may be called multiple times for the same bean. In which case an equivalent mirror
         *          must be returned
         */
        Builder specializeMirror(Supplier<? extends BeanMirror> supplier);
    }
}

interface Zandbox<T> {
    OperationHandle addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation);

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

    void decorateInstance(Function<? super T, ? extends T> decorator);

    <B> void onInitialize(Class<B> extensionBeanClass, BiConsumer<? super B, ? super T> consumer);

    default Class<? extends Extension<?>> operator() {
        return BaseExtension.class;
    }

    <K> OperationHandle overrideService(Key<K> key, K instance);

    void peekInstance(Consumer<? super T> consumer);

    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    void setErrorHandler(ErrorHandler errorHandler);
}
