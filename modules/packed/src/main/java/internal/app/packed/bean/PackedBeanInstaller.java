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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.BeanTemplate;
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
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOS;
import internal.app.packed.operation.PackedOperationTemplate;
import sandbox.extension.operation.OperationTemplate;

/** This class is responsible for installing new beans. */
public final class PackedBeanInstaller implements BeanTemplate.Installer {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Align with Key
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

    /** Initially null, set to the installed bean once it is installed. */
    @Nullable
    private BeanSetup bean;

    /** The container the bean will be installed into. */
    final ContainerSetup container;

    /** The extension that is installing the bean. */
    final ExtensionSetup installingExtension;

    /** Initial bean locals for the new bean. */
    final IdentityHashMap<PackedBeanLocal<?>, Object> locals;

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
     * @param template
     *            a template for the new bean
     * @param installingExtension
     *            the extension who created the installer
     * @param owner
     *            the owner of the new bean
     */
    PackedBeanInstaller(PackedBeanTemplate template, ExtensionSetup installingExtension, AuthoritySetup owner) {
        this.template = requireNonNull(template, "template is null");
        this.installingExtension = requireNonNull(installingExtension);
        this.owner = requireNonNull(owner);
        this.container = installingExtension.container;
        this.locals = new IdentityHashMap<>(template.beanLocals());
    }

    /**
     * Checks that the installer has not already been used to create a new bean.
     * <p>
     * There is technically no reason to not allow this installer to be reused. But we will need to make a copy of the
     * locals if we want to support this.
     */
    private void checkNotInstalledYet() {
        if (bean != null) {
            throw new IllegalStateException("A bean has already been created from this installer");
        }
    }

    /**
     * Called from {@link BeanConfiguration#BeanConfiguration(sandbox.extension.bean.BeanHandle.Installer)}
     */
    public PackedBeanHandle<?> initializeBeanConfiguration() {
        // Should we check that this method is only called once???
        // We can create multiple bean configurations from this installer
        // Maybe that is okay
        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public <C extends BeanConfiguration> BeanHandle<C> install(Class<?> beanClass, Function<? super BeanTemplate.Installer, C> newConfiguration) {
        requireNonNull(beanClass, "beanClass is null");
        return newBean(beanClass, BeanSourceKind.CLASS, beanClass, newConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public <C extends BeanConfiguration> BeanHandle<C> install(Op<?> op, Function<? super BeanTemplate.Installer, C> newConfiguration) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return newBean(beanClass, BeanSourceKind.OP, pop, newConfiguration);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanTemplate.Installer, T> configurationCreator, Consumer<? super BeanHandle<?>> onNew) {
        requireNonNull(beanClass, "beanClass is null");

        BeanClassKey e = new BeanClassKey(owner.authority(), beanClass);
        BeanSetup existingBean = container.beans.beanClasses.get(e);
        if (existingBean != null) {
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

        BeanHandle<T> handle = newBean(beanClass, BeanSourceKind.CLASS, beanClass, configurationCreator);
        onNew.accept(handle);
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installInstance(Object instance, Function<? super BeanTemplate.Installer, T> configurationCreator) {
        requireNonNull(instance, "instance is null");
        return newBean(instance.getClass(), BeanSourceKind.INSTANCE, instance, configurationCreator);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends BeanConfiguration> BeanHandle<T> installSourceless(Function<? super BeanTemplate.Installer, T> configurationCreator) {
        if (template.kind() != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return newBean(void.class, BeanSourceKind.SOURCELESS, null, configurationCreator);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller namePrefix(String prefix) {
        checkNotInstalledYet();
        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;
    }

    /**
     * Creates the new bean using this installer as the configuration.
     *
     * @param <C>
     *            the type of bean configuration that is returned to the user
     * @param beanClass
     *            the bean class
     * @param sourceKind
     *            the source of the bean
     * @param source
     *            the source of the bean
     * @param newConfiguration
     *            a function responsible for creating the bean's configuration
     * @return a handle for the bean
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <C extends BeanConfiguration> BeanHandle<C> newBean(Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            Function<? super BeanTemplate.Installer, C> newConfiguration) {
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        checkNotInstalledYet();
        // TODO check extension/assembly can still install beans

        // Create the Bean, this also marks this installer as no longer buildable
        BeanSetup bean = this.bean = new BeanSetup(this, beanClass, sourceKind, source);

        // Transfer any locals that have been set in the template or installer
        locals.forEach((l, v) -> bean.locals().set((PackedBeanLocal) l, bean, v));

        // Initialize the name of the bean
        container.beans.installAndSetBeanName(bean, namePrefix);

        // Creating an bean factory operation representing the Op if created from one.
        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;
            PackedOperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = (PackedOperationTemplate) OperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            OperationSetup os = op.newOperationSetup(new NewOS(bean, bean.installedBy, ot, null));
            bean.operations.all.add(os);
        }

        if (bean.owner instanceof ExtensionSetup es && bean.beanKind == BeanKind.CONTAINER) {
            es.sm.addBean(bean);
        }

        // Scan the bean class for annotations if it has a source
        if (sourceKind != BeanSourceKind.SOURCELESS) {
            new BeanScanner(bean).introspect();
        }

        BeanConfiguration apply = newConfiguration.apply(this);
        bean.initConfiguration(apply);

        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanInstaller setLocal(BeanLocal<T> local, T value) {
        checkNotInstalledYet();
        this.locals.put((PackedBeanLocal<?>) requireNonNull(local, "local is null"), requireNonNull(value, "value is null"));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller specializeMirror(Supplier<? extends BeanMirror> supplier) {
        checkNotInstalledYet();
        this.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }
}
