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

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanLocal.Accessor;
import app.packed.binding.Key;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.binding.BindingProvider.FromConstant;
import internal.app.packed.component.ComponentBuildState;
import internal.app.packed.context.publish.ContextualizedElement;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.ServiceProvideOperationHandle;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.util.InternalServiceUtil;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.BeanAccessHandler;
import internal.app.packed.util.accesshelper.OperationAccessHandler;

/**
 * A bean handle is a build-time reference to an installed bean. They are created by the framework when an extension
 * {@link Installer installs} a bean on behalf of the user (or an another extension).
 * <p>
 * Instances of {@code BeanHandle} should in general not be exposed to outside of the handling extension. Instead the
 * extension should expose instances of {@link #configuration() bean configuration} to the owner of the bean.
 *
 * @see BeanTemplate
 * @see BeanInstaller
 * @see BeanConfiguration
 */
public non-sealed class BeanHandle<C extends BeanConfiguration> extends ComponentHandle implements ContextualizedElement, Accessor {

    /** The internal configuration of the bean. */
    final BeanSetup bean;

    /** The lazy generated bean configuration. */
    private final Supplier<C> configuration = StableValue.supplier(() -> newBeanConfiguration());

    /** The lazy generated bean mirror. */
    private final Supplier<BeanMirror> mirror = StableValue.supplier(() -> newBeanMirror());

    /** The state of the bean. */
    private ComponentBuildState state = ComponentBuildState.CONFIGURABLE_AND_OPEN;

    /**
     * Create a new bean handle
     *
     * @param installer
     *            the installer to create the handle from
     */
    public BeanHandle(BeanInstaller installer) {
        this.bean = ((PackedBeanInstaller) installer).toSetup();
    }

    /**
     *
     */
    public final void allowMultiClass() {
        checkIsOpen();
        bean.multiInstall = bean.multiInstall | 1 << 31;
    }

    public final void attach(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public final Class<?> beanClass() {
        return bean.bean.beanClass;
    }

    /** {@return the bean kind.} */
    public final BeanLifetime beanKind() {
        return bean.beanKind;
    }

    /** {@return the bean source kind.} */
    public final BeanSourceKind beanSourceKind() {
        return bean.bean.beanSourceKind;
    }

    /**
     * Adds a "service"
     *
     * @param <K>
     * @param key
     * @param supplier
     */
    // Is lazy, or eager?
    // Eager_never_fail, Eager_fail_if_not_used, Lazy_whenFirstUsed, LazyFailIfNotUsed, Some default for the container?
    public final <K> void bindComputedConstant(Key<K> key, Supplier<? extends K> supplier) {
        checkIsOpen();
        bean.bindComputedConstant(key, supplier);
    }

    public final <K> void bindConstant(Class<K> key, K constant) {
        bindConstant(Key.of(key), constant);
    }

    // Problemet med dette navn er at folk kan tror det er en bean instance
    // Hmm vi kaldet det bind
    public final <K> void bindConstant(Key<K> key, K instance) {
        requireNonNull(key);

        // Add a service provider for the instance
        Class<?> claz = instance == null ? Object.class : instance.getClass();
        bean.serviceProviders.put(key, new BeanServiceProviderSetup(key, new FromConstant(claz, instance)));
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return bean.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkIsOpen();
        bean.container.application.componentTags.addComponentTags(bean, tags);
    }

    /** { @return the user exposed configuration of the bean} */
    public final C configuration() {
        return configuration.get();
    }

    /**
     * Returns the key that the bean will be made available under as default if provided as service.
     * <p>
     * This method can be overridden to return another key as default.
     *
     * @return the default key
     * @throws UnsupportedOperationException
     *             if called on a bean that has a void bean class
     *
     * @see app.packed.service.ServiceableBeanConfiguration#defaultKey()
     * @see #exportAs(Key)
     * @see #provideAs(Key)
     */
    public Key<?> defaultKey() {
        if (beanClass() == void.class) {
            throw new UnsupportedOperationException("This method is not supported for void bean classes");
        }
        return Key.fromClass(beanClass());
    }

    public final void exportAs(Class<?> key) {
        exportAs(Key.of(key));
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
    public final void exportAs(Key<?> key) {
        checkIsOpen();
        OperationSetup operation = OperationSetup.crack(instanceProvideOperation());
        bean.serviceNamespace().export(key, operation);
    }

    // Used from export/provide
    private ServiceProvideOperationHandle instanceProvideOperation() {
        PackedOperationTemplate template = PackedOperationTemplate.DEFAULTS.withReturnType(beanClass());

        return template.newInstallerFromBeanAccess(OperationType.of(beanClass()), bean, bean.installedBy).install(ServiceProvideOperationHandle::new);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return state == ComponentBuildState.CONFIGURABLE_AND_OPEN;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isOpen() {
        return state != ComponentBuildState.CLOSED;
    }

    /**
     * Returns a list of operation handles that corresponds to the {@link BeanTemplate#lifetimeOperations() lifetime
     * operations} on the template that created the bean.
     *
     * @return a list of lifetime operation handles
     *
     * @see BeanTemplate#lifetimeOperations()
     * @see BeanInstaller#lifetimes(app.packed.operation.OperationTemplate...)
     */
    // Need to have a CompoundInvoker

    // init-> BeanCreate ()
    // start-> void ()
    // stop -> voidd
    public final List<OperationHandle<?>> lifecycleInvokers() {
        if (beanSourceKind() != BeanSourceKind.SOURCELESS) {
//            debug();
//            IO.println(bean.operations.first().template.methodType);
            return List.of(bean.operations.first().handle());
        }
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public final BeanMirror mirror() {
        return mirror.get();
    }

    /**
     * Sets the name of the bean, overriding any existing name.
     *
     * @param name
     *            the name of the bean
     * @throws IllegalArgumentException
     *             if another bean with the specified name already exists
     */
    public final void named(String name) {
        checkIsOpen();
        bean.named(name);
    }

    @SuppressWarnings("unchecked")
    protected C newBeanConfiguration() {
        return (C) new BeanConfiguration(this);
    }

    // add overrideServiceIfPresent? Or have a Set<Key<?>> BeanConfiguration.services()

    protected BeanMirror newBeanMirror() {
        return new BeanMirror(this);
    }

    // Probably not called on beans owned by extensions
    // Or maybe we call it for users when assembly closes, and extensions when application closes
    /**
     * Invoked just before this handle is closed.
     * <p>
     * This handle can still be until the extension is being closed together with the application. So this method can be
     * used to make the last checks of the configuration of the bean.
     * <p>
     * This method should rarely be used for any kind of validation, as is is invoked very late in the build process.
     * {@link #isConfigurable()} is often a better choice for validation.
     * <p>
     * Ope
     *
     * This handle will be {@link #isConfigurable()} while calling this method, but marked as non configurable immediately
     * after.
     * <p>
     * For beans owned by the user, this method will be called when the owning assembly is being closed. For beans owned by
     * an extension, this method will be called when the application closes.
     * <p>
     * If there are multiple beans that are marked as no longer configurable at the same time. The framework may call this
     * method in any order between the beans.
     *
     * @see #isConfigurable()
     */
    protected void onClose() {}

    /**
     * Invoked after the bean is no longer configurable by the owner of the bean.
     * <p>
     * This method is primarily overridden to perform validation that can only be performed after the bean is no longer
     * configurable.
     */
    protected void onConfigured() {}

    /**
     * Called by the framework to mark that the bean is no longer configurable or open.
     *
     * @see internal.app.packed.util.handlers.BeanHandlers#invokeBeanHandleDoClose(BeanHandle, boolean)
     */
    /* package private */ final void onStateChange(boolean isClose) {
        if (state == ComponentBuildState.CONFIGURABLE_AND_OPEN) {
            state = ComponentBuildState.OPEN_BUT_NOT_CONFIGURABLE;
            onConfigured();
        }

        if (isClose) {
            int i = onStateChange(0, isClose);
            onClose();
            onStateChange(i, isClose);
            state = ComponentBuildState.CLOSED;
        }
    }

    /* package private */ final void onStateChange2(boolean isClose) {
        if (state == ComponentBuildState.CONFIGURABLE_AND_OPEN) {
            // OperationHandle.onConfigured could technically add operation to the bean
            // So make sure we use .size() comparison to catch stragglers
            int i = onStateChange(0, false);
            state = ComponentBuildState.OPEN_BUT_NOT_CONFIGURABLE;
            onConfigured();
            // Catch any operations added in OnConfigure
            // Hmm, IDK we add operations at any time now...
            // So why do anything special here...
            onStateChange(i, false);
        }

        if (isClose) {
            onClose();
            state = ComponentBuildState.CLOSED;
        }
        if (isClose) {
            int i = onStateChange(0, isClose);
            onClose();
            onStateChange(i, isClose);
            state = ComponentBuildState.CLOSED;
        } else {
            state = ComponentBuildState.OPEN_BUT_NOT_CONFIGURABLE;

            onConfigured();
        }
    }

    private int onStateChange(int listIndex, boolean isClose) {
        int i = listIndex;
        for (; i < bean.operations.all.size(); i++) {
            OperationSetup op = bean.operations.all.get(i);
            OperationAccessHandler.instance().invokeOperationHandleDoClose(op.handle(), isClose);
        }
        return i;
    }

    /** {@return the owner of the bean} */
    public final ComponentRealm owner() {
        return bean.owner();
    }

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
    public final void provideAs(Key<?> key) {
        Key<?> k = InternalServiceUtil.checkKey(bean.bean.beanClass, key);
        checkIsOpen();
//        if (beanKind() != BeanKind.CONTAINER || beanKind() != BeanKind.LAZY) {
//            // throw new UnsupportedOperationException("This method can only be called on beans of kind " + BeanKind.CONTAINER + "
//            // or " + BeanKind.LAZY);
//        }

        bean.serviceNamespace().provideService(k, instanceProvideOperation(), bean.beanInstanceBindingProvider());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bean.toString();
    }

    static {
        AccessHelper.initHandler(BeanAccessHandler.class, new BeanAccessHandler() {

            @Override
            public BeanHandle<?> getBeanConfigurationHandle(BeanConfiguration configuration) {
                return configuration.handle();
            }

            @Override
            public BeanSetup getBeanHandleBean(BeanHandle<?> handle) {
                return handle.bean;
            }

            @Override
            public BeanHandle<?> getBeanMirrorHandle(BeanMirror mirror) {
                return mirror.handle;
            }

            @Override
            public void invokeBeanHandleDoClose(BeanHandle<?> handle, boolean isClose) {
                handle.onStateChange(isClose);
            }
        });
    }
}

//Operations
//Call an operation at some point in the lifecycle
//// Take other (extension) bean instance Take
//Replace instance after creation

interface Zandbox<T> {
    OperationHandle<?> addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation);

    // onClientInitialize
    default <B> void afterInitialize(InstanceBeanConfiguration<B> extensionBean, BiConsumer<? super B, ? super T> consumer) {
        //// Ideen er at man fx kan have en BeanHandle<Driver>.onInitialize(MyEBC, (b,p)->b.drivers[i]=p); Skal godt nok passe
        /// paa med capturing her... Ellers faar man hele application setup med i lambdaen

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

    <K> OperationHandle<?> overrideService(Key<K> key, K instance);

    /**
     * @param consumer
     * @throws UnsupportedOperationException
     *             if the bean does not have or creates any instances
     */
    // Vi har ikke laengere typen, saa maaske bare MH
    // Eller ogsaa skal vi tage en class
    // When is this on
    // Maybe on BeanInitiOperationInstead???
    void peekInstance(Consumer<? super T> consumer);

    // (BeanClass) void
    // void peekInstance(MethodHandle methodHandle);

    // Altssa, er det ikke en bare en
    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    // void setErrorHandler(ErrorHandler errorHandler);
}
