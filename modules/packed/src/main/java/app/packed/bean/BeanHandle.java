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
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationTemplate.Installer;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.bean.PackedBeanInstaller;
import internal.app.packed.binding.BindingAccessor.FromConstant;
import internal.app.packed.context.publish.ContextualizedElement;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.util.InternalServiceUtil;

/**
 * A bean handle is a build-time reference to an installed bean. They are created by the framework when an extension
 * {@link Installer installs} a bean on behalf of the user (or an another extension).
 * <p>
 * Instances of {@code BeanHandle} should not be exposed from outside of the extension that created the bean. Instead
 * the extension should expose instances of {@link #configuration() bean configuration} to the user (or another extension).
 *
 * @see BeanTemplate.Installer
 */
public non-sealed class BeanHandle<C extends BeanConfiguration> extends ComponentHandle implements ContextualizedElement, Accessor {

    /** The internal configuration of the bean. */
    final BeanSetup bean;

    /** The lazy generated bean configuration. */
    private C configuration;

    /** The lazy generated bean mirror. */
    private BeanMirror mirror;

    public BeanHandle(BeanTemplate.Installer installer) {
        this.bean = ((PackedBeanInstaller) installer).toHandle();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkIsConfigurable();
        bean.container.application.ctm.addComponentTags(bean, tags);
    }

    /**
     *
     */
    public final void allowMultiClass() {
        checkIsConfigurable();
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
        checkIsConfigurable();
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
    public final void componentTags(String... tags) {
        throw new UnsupportedOperationException();
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
     *
     * @return the default key
     * @throws UnsupportedOperationException
     *             if called on a bean that has a void bean class
     *
     * @see app.packed.service.ServiceableBeanConfiguration#defaultKey()
     * @see #exportAs(Key)
     * @see #provideAs(Key)
     */
    public final Key<?> defaultKey() {
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
        checkIsConfigurable();
        bean.serviceNamespace().export(key, bean.instanceAccessOperation());
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return bean.owner.isConfigurable();
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
    // LifetimeOperationConfiguration???
    public final List<OperationHandle<?>> lifetimeOperations() {
        if (beanKind() != BeanKind.STATIC && beanSourceKind() != BeanSourceKind.SOURCELESS) {
            return List.of(bean.operations.first().handle());
        }
        return List.of();
    }

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
        checkIsConfigurable();
        bean.named(name);
    }

    @SuppressWarnings("unchecked")
    protected C newBeanConfiguration() {
        return (C) new BeanConfiguration(this);
    }

    protected BeanMirror newBeanMirror() {
        return new BeanMirror(this);
    }

    // add overrideServiceIfPresent? Or have a Set<Key<?>> BeanConfiguration.services()

    /** {@inheritDoc} */
    public final Installer newFunctionalOperation(OperationTemplate template, Object function) {
        return null;
    }

    protected void onAssemblyClose() {}

    /** {@inheritDoc} */
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
        checkIsConfigurable();
        if (beanKind() != BeanKind.CONTAINER || beanKind() != BeanKind.LAZY) {
            // throw new UnsupportedOperationException("This method can only be called on beans of kind " + BeanKind.CONTAINER + "
            // or " + BeanKind.LAZY);
        }

        bean.serviceNamespace().provideOperation(k, bean.instanceAccessOperation(), bean.beanInstanceBindingProvider());
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
    void peekInstance(Consumer<? super T> consumer);

    // (BeanClass) void
    void peekInstance(MethodHandle methodHandle);

    // Altssa, er det ikke en bare en
    default void runOnInitialized(Op<?> op) {
        // First parameter must be assignable to the created instance, IDK
    }

    // void setErrorHandler(ErrorHandler errorHandler);
}
