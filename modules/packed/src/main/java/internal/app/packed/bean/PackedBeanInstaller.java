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

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.container.AuthoritySetup;
import internal.app.packed.container.ContainerBeanStore;
import internal.app.packed.container.ContainerBeanStore.BeanClassKey;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.PackedBeanTemplate;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import sandbox.extension.bean.BeanHandle;
import sandbox.extension.bean.BeanTemplate;
import sandbox.extension.operation.OperationTemplate;

/** This class is responsible for installing new beans. */
public final class PackedBeanInstaller implements BeanTemplate.Installer {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Align with Key
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

    /** The bean we are going to install. */
    @Nullable
    private BeanSetup beanSetup;

    /** The container the bean will be installed into. */
    final ContainerSetup container;

    /** The extension that is installing the bean. */
    final ExtensionSetup installingExtension;

    /** Stores bean locals while building. */
    final IdentityHashMap<BeanLocal<?>, Object> locals = new IdentityHashMap<>();

    /** A bean mirror supplier */
    @Nullable
    Supplier<? extends BeanMirror> mirrorSupplier;

    String namePrefix;

    /** The owner of the bean. */
    final AuthoritySetup owner;

    /** The bean's template. */
    // Maybe we can override it??? If we want to delegate
    public final PackedBeanTemplate template;

    /**
     * Create a new bean installer.
     *
     * @param installingExtension
     *            the extension who created the installer
     * @param owner
     *            the owner of the new bean
     * @param template
     *            a template for the new bean
     */
    public PackedBeanInstaller(ExtensionSetup installingExtension, AuthoritySetup owner, BeanTemplate template) {
        this.container = installingExtension.container;
        this.installingExtension = requireNonNull(installingExtension);
        this.owner = requireNonNull(owner);
        this.template = (PackedBeanTemplate) requireNonNull(template, "template is null");
    }

    /**
     * Checks that the builder has not been used to create a new bean.
     * <p>
     * There is technically no reason to not allow this. But we will need to make a copy of the locals if we want to support
     * this.
     */
    private void checkIsBuildable() {
        if (beanSetup != null) {
            throw new IllegalStateException("A bean has all been created from this builder");
        }
    }

    /**
     * Creates a new bean using the configured installer.
     *
     * @param <T>
     *            the type of bean to install
     * @param beanClass
     *            the bean class
     * @param sourceKind
     *            the source of the bean
     * @param source
     *            the source of the bean
     * @return a handle for the bean
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private BeanHandle<?> createBean(Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            Function<? super BeanTemplate.Installer, ? extends BeanConfiguration> newConfiguration) {
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        checkIsBuildable();

        // Creates Bean
        BeanSetup bean = this.beanSetup = new BeanSetup(this, beanClass, sourceKind, source);

        // Initialize the name of the bean
        container.beans.installAndSetBeanName(bean, namePrefix);

        // Copy any bean locals that have been set, we need to set this before introspection
        // I think maybe we need to do this as the last action?
        for (Entry<BeanLocal<?>, Object> e : locals.entrySet()) {
            container.locals().set((PackedBeanLocal) e.getKey(), bean, e.getValue());
        }

        // Creating an operation representing the Op if created from one.
        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;
            OperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = OperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            OperationSetup os = op.newOperationSetup(bean, bean.installedBy, ot, null);
            bean.operations.all.add(os);
        }

        if (bean.owner instanceof ExtensionSetup es && bean.beanKind == BeanKind.CONTAINER) {
            es.sm.addBean(bean);
        }

        // Scan the bean class for annotations if it has a source
        if (sourceKind != BeanSourceKind.SOURCELESS) {
            new BeanScanner(bean).introspect();
        }

        if (newConfiguration != null) {
            BeanConfiguration apply = newConfiguration.apply(this);
            beanSetup.initConfiguration(apply);
        }
        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <C extends BeanConfiguration> BeanHandle<C> install(Class<?> beanClass, Function<? super BeanTemplate.Installer, C> newConfiguration) {
        requireNonNull(beanClass, "beanClass is null");
        checkIsBuildable();

        return (BeanHandle<C>) createBean(beanClass, BeanSourceKind.CLASS, beanClass, newConfiguration);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <C extends BeanConfiguration> BeanHandle<C> install(Op<?> op, Function<? super BeanTemplate.Installer, C> newConfiguration) {
        checkIsBuildable();

        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return (BeanHandle<C>) createBean(beanClass, BeanSourceKind.OP, pop, newConfiguration);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanTemplate.Installer, T> configurationCreator, Consumer<? super BeanHandle<?>> onNew) {
        requireNonNull(beanClass, "beanClass is null");
        checkIsBuildable();

        BeanClassKey e = new BeanClassKey(owner.authority(), beanClass);
        BeanSetup existingBean = container.beans.beanClasses.get(e);
        if (existingBean != null) {
            @Nullable
            BeanConfiguration existingConfiguration = existingBean.configuration();

            if (ContainerBeanStore.isMultiInstall(existingBean)) {
                throw new IllegalArgumentException("MultiInstall Bean");
            } else if (!beanConfigurationClass.isInstance(existingConfiguration)) {
                throw new IllegalStateException("A previous bean has been installed that used another configuration type then " + beanConfigurationClass
                        + " was " + existingConfiguration.getClass());
            } else {
                return new PackedBeanHandle<T>(existingBean);
            }
        }

        BeanHandle<?> handle = createBean(beanClass, BeanSourceKind.CLASS, beanClass, configurationCreator);
        onNew.accept(handle);
        return (BeanHandle<T>) handle;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installInstance(Object instance, Function<? super BeanTemplate.Installer, T> configurationCreator) {
        requireNonNull(instance, "instance is null");
        checkIsBuildable();

        Class<?> beanClass = instance.getClass();
        return (BeanHandle<T>) createBean(beanClass, BeanSourceKind.INSTANCE, instance, configurationCreator);
    }

    /**
     * Creates a new bean without a source.
     *
     * @return a bean handle representing the new bean
     *
     * @throws IllegalStateException
     *             if this builder was created with a base template other than {@link BeanTemplate#STATIC}
     * @apiNote Currently this is an internal API only. Main reason is that I don't see any use cases as long as we don't
     *          support adding operations at will
     * @see app.packed.bean.BeanSourceKind#SOURCELESS
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installSourceless(Function<? super BeanTemplate.Installer, T> configurationCreator) {
        if (template.kind() != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        checkIsBuildable();

        return (BeanHandle<T>) createBean(void.class, BeanSourceKind.SOURCELESS, null, configurationCreator);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller namePrefix(String prefix) {
        checkIsBuildable();

        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanInstaller setLocal(BeanLocal<T> local, T value) {
        requireNonNull(local);
        requireNonNull(value);
        checkIsBuildable();

        locals.put(local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        checkIsBuildable();

        this.mirrorSupplier = supplier;
        return this;
    }

    /**
     * Called from {@link BeanConfiguration#BeanConfiguration(sandbox.extension.bean.BeanHandle.Installer)}
     */
    public PackedBeanHandle<?> newHandleFromConfiguration() {
        // Should we check this is called only once???
        // We can create multiple configurations from this
        // Okay we should do some stuff here
        return new PackedBeanHandle<>(beanSetup);
    }
}
