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
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanBuildLocal.Accessor;
import app.packed.binding.Key;
import app.packed.build.BuildActor;
import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.operation.InvokerFactory;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.binding.BindingAccessor.FromConstant;
import internal.app.packed.context.publish.ContextualizedElement;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOperationInstaller;
import internal.app.packed.operation.PackedOperationTarget.BeanAccessOperationTarget;
import internal.app.packed.operation.PackedOperationTemplate;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.util.InternalServiceUtil;
import internal.app.packed.util.handlers.OperationHandlers;

/**
 * A bean handle is a build-time reference to an installed bean. They are created by the framework when an extension
 * {@link Installer installs} a bean on behalf of the user (or an another extension).
 * <p>
 * Instances of {@code BeanHandle} should in general not be exposed from outside of the extension that made it. Instead
 * the extension should expose instances of {@link #configuration() bean configuration} to the user (or another
 * extension).
 *
 * @see BeanTemplate
 * @see BeanInstaller
 * @see BeanConfiguration
 */
public non-sealed class BeanHandle<C extends BeanConfiguration> extends ComponentHandle implements ContextualizedElement, Accessor {

    /** The internal configuration of the bean. */
    final BeanSetup bean;

    /** The lazy generated bean configuration. */
    private C configuration;

    /** Whether or not the bean is configurable. */
    private boolean isConfigurationConfigurable = true;

    /** The lazy generated bean mirror. */
    private BeanMirror mirror;

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
        checkHandleIsConfigurable();
        bean.multiInstall = bean.multiInstall | 1 << 31;
    }

    /** {@inheritDoc} */
    public final Class<?> beanClass() {
        return bean.beanClass;
    }

    /** {@return the bean kind.} */
    public final BeanKind beanKind() {
        return bean.beanKind;
    }

    /** {@return the bean source kind.} */
    public final BeanSourceKind beanSourceKind() {
        return bean.beanSourceKind;
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
    public final <K> void bindCodeGenerator(Key<K> key, Supplier<? extends K> supplier) {
        checkHandleIsConfigurable();
        bean.bindCodeGenerator(key, supplier);
    }

    public final <K> void bindServiceInstance(Class<K> key, K constant) {
        bindServiceInstance(Key.of(key), constant);
    }

    // Problemet med dette navn er at folk kan tror det er en bean instance
    // Hmm vi kaldet det bind
    public final <K> void bindServiceInstance(Key<K> key, K instance) {
        requireNonNull(key);

        // Add a service provider for the instance
        Class<?> claz = instance == null ? Object.class : instance.getClass();
        bean.serviceProviders.put(key, new BeanServiceProviderSetup(key, new FromConstant(claz, instance)));

//        // Find any existing bindings for the specified key
//        NamespaceServiceProviderSetup ss = bean.serviceNamespace().nodes.get(key);
//
//        if (ss != null) {
//            List<ServiceBindingSetup> l = ss.removeBindingsForBean(bean);
//            if (!l.isEmpty()) {
//                for (ServiceBindingSetup s : l) {
//                    int index = s.index;
//                    Class<?> cl;
//                    if (instance == null) {
//                        cl = s.operation.type().toMethodType().parameterType(index);
//                    } else {
//                        cl = instance.getClass();
//                    }
//                    s.operation.bindings[s.index] = new ManualBindingSetup(s.operation, s.index, s.operation.bean.owner(), new FromConstant(cl, instance));
//                }
//                return;
//            }
//        }

        // TODO we should go through all bindings and see if have some where the type matches.
        // But is not resolved as a service

        // Also if we override twice, would be nice with something like. Already overridden
//        throw new IllegalArgumentException("Bean '" + bean.name() + "' does not have a dependency for a service with " + key
//                + ". Services that can be overridden: " + bean.serviceNamespace().keys());
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return bean.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkHandleIsConfigurable();
        bean.container.application.componentTags.addComponentTags(bean, tags);
    }

    /** { @return the user exposed configuration of the bean} */
    public final C configuration() {
        C c = configuration;
        if (c == null) {
            c = configuration = newBeanConfiguration();
        }
        return c;
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

    /**
     * Called by the framework to mark the bean as no longer be configurable.
     *
     * @see internal.app.packed.util.handlers.BeanHandlers#invokeBeanHandleDoClose(BeanHandle)
     */
    // onOwnerClosed
    final void doClose() {
        // I think we might need to methods, one before we close operations, and one after
        // So the one before is the last chance to add (synthetic) operations
        // Also we close the operation handles here. Not the configuration

        // Close all operations
        bean.operations.forEach(o -> OperationHandlers.invokeOperationHandleDoClose(o.handle()));
        // Do we want to close operations?
        // Or is this based on the installing extension and not the owner??
        onConfigurationClosed();
        isConfigurationConfigurable = false;
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
        checkHandleIsConfigurable();
        bean.serviceNamespace().export(key, instancePovideOperation());
    }

    // Used from export/provide
    // The only funny thing is the operation target
    private OperationSetup instancePovideOperation() {
        PackedOperationTemplate template = (PackedOperationTemplate) OperationTemplate.of(c -> c.returnType(beanClass()));

        PackedOperationInstaller installer = template.newInstaller(OperationType.of(beanClass()), bean, bean.installedBy);
        installer.operationTarget = new BeanAccessOperationTarget();
        installer.namePrefix = "InstantAccess";

        return OperationSetup.crack(installer.install(OperationHandle::new));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurationConfigurable() {
        return isConfigurationConfigurable;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isHandleConfigurable() {
        return bean.installedBy.isConfigurable();
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
    public final List<InvokerFactory> lifecycleInvokers() {
        if (beanKind() != BeanKind.STATIC && beanSourceKind() != BeanSourceKind.SOURCELESS) {
//            debug();
//            System.out.println(bean.operations.first().template.methodType);
            return List.of(bean.operations.first().handle());
        }
        return List.of();
    }

    /** {@inheritDoc} */
    @Override
    public final BeanMirror mirror() {
        BeanMirror m = mirror;
        if (m == null) {
            m = mirror = newBeanMirror();
        }
        return m;
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
        checkHandleIsConfigurable();
        bean.named(name);
    }

    // add overrideServiceIfPresent? Or have a Set<Key<?>> BeanConfiguration.services()

    @SuppressWarnings("unchecked")
    protected C newBeanConfiguration() {
        return (C) new BeanConfiguration(this);
    }

    protected BeanMirror newBeanMirror() {
        return new BeanMirror(this);
    }

    /** {@inheritDoc} */
    public final OperationInstaller newFunctionalOperation(OperationTemplate template, Object function) {
        return null;
    }

    // Probably not called on beans owned by extensions
    // Or maybe we call it for users when assembly closes, and extensions when application closes
    /**
     * Called by the framework when the {@link BeanConfiguration bean} can no longer be configured by the owner of the bean.
     * <p>
     * This handle can still be {@link #isHandleConfigurable() configured} until the extension is being closed together with
     * the application. So this method can be used to make the last checks of the configuration of the bean.
     * <p>
     * This handle will be {@link #isConfigurable()} while calling this method, but marked as non configurable immediately
     * after.
     * <p>
     * For beans owned by the user, this method will be called when the owning assembly is being closed. For beans owned by
     * an extension, this method will be called when the application closes.
     * <p>
     * If there are multiple beans that are marked as no longer configurable at the same time. The framework may call this
     * method in any order between the beans.
     *
     * @see #isConfigurationConfigurable()
     */
    protected void onConfigurationClosed() {}

    /** {@return the owner of the bean} */
    public final BuildActor owner() {
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
        Key<?> k = InternalServiceUtil.checkKey(bean.beanClass, key);
        checkHandleIsConfigurable();
        if (beanKind() != BeanKind.CONTAINER || beanKind() != BeanKind.LAZY) {
            // throw new UnsupportedOperationException("This method can only be called on beans of kind " + BeanKind.CONTAINER + "
            // or " + BeanKind.LAZY);
        }

        bean.serviceNamespace().provideService(bean, k, instancePovideOperation(), bean.beanInstanceBindingProvider());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return bean.toString();
    }
}

//Operations
//Call an operation at some point in the lifecycle
////Take other (extension) bean instance
////Take
//Replace instance after creation

interface Zandbox<T> {
    OperationHandle<?> addOperation(InstanceBeanConfiguration<?> operator, Op<?> operation);

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
    void peekInstance(MethodHandle methodHandle);

    // Altssa, er det ikke en bare en
    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    // void setErrorHandler(ErrorHandler errorHandler);
}
