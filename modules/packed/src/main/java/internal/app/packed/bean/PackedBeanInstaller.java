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

import app.packed.bean.BeanBuildHook;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanSourceKind;
import app.packed.bean.BeanTemplate;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import app.packed.operation.Provider;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.bean.ContainerBeanStore.BeanClassKey;
import internal.app.packed.container.AuthoritySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.operation.PackedOp.NewOS;
import internal.app.packed.operation.PackedOperationTemplate;

/** This class is responsible for installing new beans. */
public final class PackedBeanInstaller implements BeanTemplate.Installer {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Align with Key
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

    /** Initially null, set to the installed bean once it is installed. */
    @Nullable
    public BeanSetup bean;

    /** The container the bean will be installed into. */
    final ContainerSetup container;

    /** The extension that is installing the bean. */
    final ExtensionSetup installingExtension;

    /** Initial bean locals for the new bean. */
    final IdentityHashMap<PackedBeanLocal<?>, Object> locals;

    String namePrefix;

    /** The owner of the bean. */
    final AuthoritySetup owner;

    /** The bean's template. */
    // Maybe we can override it??? If we want to delegate
    public final PackedBeanTemplate template;

    /**
     * Create a new bean installer.
     * <p>
     * This method should only be called through {@link PackedBeanTemplate#newInstaller(ExtensionSetup, AuthoritySetup)}.
     *
     * @param template
     *            the template for the new bean
     * @param installingExtension
     *            the extension who is installing the bean
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
    public BeanHandle<?> initializeBeanConfiguration() {
        // Should we check that this method is only called once???
        // We can create multiple bean configurations from this installer
        // Maybe that is okay
        return new BeanHandle<>(this);
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H install(Class<?> beanClass, Function<? super BeanTemplate.Installer, H> factory) {
        requireNonNull(beanClass, "beanClass is null");
        return newBean(beanClass, BeanSourceKind.CLASS, beanClass, factory);
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H install(Op<?> op, Function<? super BeanTemplate.Installer, H> factory) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return newBean(beanClass, BeanSourceKind.OP, pop, factory);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <H extends BeanHandle<T>, T extends BeanConfiguration> H installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanTemplate.Installer, H> factory, Consumer<? super BeanHandle<?>> onNew) {
        requireNonNull(beanClass, "beanClass is null");

        BeanClassKey e = new BeanClassKey(owner.authority(), beanClass);
        BeanSetup existingBean = container.beans.beanClasses.get(e);
        if (existingBean != null) {
            BeanConfiguration existingConfiguration = existingBean.handle().configuration();

            if (ContainerBeanStore.isMultiInstall(existingBean)) {
                throw new IllegalArgumentException("MultiInstall Bean");
            } else if (!beanConfigurationClass.isInstance(existingConfiguration)) {
                throw new IllegalStateException("A previous bean has been installed that used another configuration type then " + beanConfigurationClass
                        + " was " + existingConfiguration.getClass());
            } else {
                // Probably need to check that we are the same extension that installed it
                return (H) existingBean.handle;
            }
        }

        BeanHandle<T> handle = newBean(beanClass, BeanSourceKind.CLASS, beanClass, factory);
        onNew.accept(handle);

        return (H) handle;
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H installInstance(Object instance, Function<? super BeanTemplate.Installer, H> factory) {
        requireNonNull(instance, "instance is null");
        return newBean(instance.getClass(), BeanSourceKind.INSTANCE, instance, factory);
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H installSourceless(Function<? super BeanTemplate.Installer, H> factory) {
        if (template.kind() != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return newBean(void.class, BeanSourceKind.SOURCELESS, null, factory);
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
    private <H extends BeanHandle<?>> H newBean(Class<?> beanClass, BeanSourceKind sourceKind, @Nullable Object source,
            Function<? super BeanTemplate.Installer, H> factory) {
        requireNonNull(factory, "factory is null");
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        checkNotInstalledYet();
        // TODO check extension/assembly can still install beans

        // Create the Bean, this also marks this installer as unconfigurable
        BeanSetup bean = this.bean = new BeanSetup(this, beanClass, sourceKind, source);

        // Transfer any locals that have been set in the template or installer
        locals.forEach((l, v) -> bean.locals().set((PackedBeanLocal) l, bean, v));

        // Creating an bean factory operation representing the Op if an Op was specified when creating the bean.
        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;
            PackedOperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = (PackedOperationTemplate) OperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            op.newOperationSetup(new NewOS(bean, bean.installedBy, ot, null));

            // Op'en bliver resolved med BeanClassen i scanneren...
            // Ved ikke om det giver mening, vil umiddelbart sige nej
            // Vil sige den er helt uafhaendig? Men for nu er det fint
        }

        // Scan the bean class for annotations if it has a source

        // We need this here to access mirrors when binding them as constants
        // Maybe we should bind them delayed.
        BeanHandle<?> apply = factory.apply(this);
        bean.handle = apply;
        if (sourceKind != BeanSourceKind.SOURCELESS) {
            new BeanScanner(bean).introspect();
            bean.scanner = null;
        }

        // Add the bean to the container and initialize the name of the bean
        container.beans.installAndSetBeanName(bean, namePrefix);

        this.container.assembly.model.hooks.forEach(BeanBuildHook.class, h -> h.onNew(apply.configuration()));

        return (H) apply;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanInstaller setLocal(BeanLocal<T> local, T value) {
        checkNotInstalledYet();
        this.locals.put((PackedBeanLocal<?>) requireNonNull(local, "local is null"), requireNonNull(value, "value is null"));
        return this;
    }

}
