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

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bean.BeanBuildLocal;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.ContainerBeanStore.BeanClassKey;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.PackedOp;

/** Implementation of {@link BeanTemplate.Installer}. */
public final class PackedBeanInstaller extends AbstractComponentInstaller<BeanSetup, PackedBeanInstaller> implements BeanInstaller {

    /** The extension that is installing the bean. */
    final ExtensionSetup installledByExtension;

    String namePrefix;

    /** The owner of the bean. */
    final AuthoritySetup<?> owner;

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
     * @param installledByExtension
     *            the extension who is installing the bean
     * @param owner
     *            the owner of the new bean
     */
    PackedBeanInstaller(PackedBeanTemplate template, ExtensionSetup installledByExtension, AuthoritySetup<?> owner) {
        super(template.locals());
        this.template = requireNonNull(template, "template is null");
        this.installledByExtension = requireNonNull(installledByExtension);
        this.owner = requireNonNull(owner);
    }

    /** {@inheritDoc} */
    @Override
    protected ApplicationSetup application(BeanSetup setup) {
        return setup.container.application;
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H install(Class<?> beanClass, Function<? super BeanInstaller, H> factory) {
        requireNonNull(beanClass, "beanClass is null");
        return BeanSetup.newBean(this, beanClass, BeanSourceKind.CLASS, beanClass, factory);
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H install(Op<?> op, Function<? super BeanInstaller, H> factory) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return BeanSetup.newBean(this, beanClass, BeanSourceKind.OP, pop, factory);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <H extends BeanHandle<T>, T extends BeanConfiguration> H installIfAbsent(Class<?> beanClass, Class<T> beanConfigurationClass,
            Function<? super BeanInstaller, H> factory, Consumer<? super BeanHandle<?>> onNew) {
        requireNonNull(beanClass, "beanClass is null");

        BeanClassKey e = new BeanClassKey(owner.authority(), beanClass);
        BeanSetup existingBean = installledByExtension.container.beans.beanClasses.get(e);
        if (existingBean != null) {
            BeanConfiguration existingConfiguration = existingBean.handle().configuration();

            if (ContainerBeanStore.isMultiInstall(existingBean)) {
                throw new IllegalArgumentException("MultiInstall Bean");
            } else if (!beanConfigurationClass.isInstance(existingConfiguration)) {
                throw new IllegalStateException("A previous bean has been installed that used another configuration type then " + beanConfigurationClass
                        + " was " + existingConfiguration.getClass());
            } else {
                // Probably need to check that we are the same extension that installed it
                return (H) existingBean.handle();
            }
        }

        BeanHandle<T> handle = BeanSetup.newBean(this, beanClass, BeanSourceKind.CLASS, beanClass, factory);
        onNew.accept(handle);

        return (H) handle;
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H installInstance(Object instance, Function<? super BeanInstaller, H> factory) {
        requireNonNull(instance, "instance is null");
        return BeanSetup.newBean(this, instance.getClass(), BeanSourceKind.INSTANCE, instance, factory);
    }

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H installSourceless(Function<? super BeanInstaller, H> factory) {
        if (template.beanKind() != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return BeanSetup.newBean(this, void.class, BeanSourceKind.SOURCELESS, null, factory);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller namePrefix(String prefix) {
        checkNotInstalledYet();
        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanInstaller setLocal(BeanBuildLocal<T> local, T value) {
        return super.setLocal(local, value);
    }

    public static class ProvidableBeanHandle<T> extends BeanHandle<ProvidableBeanConfiguration<T>> {

        /**
         * @param installer
         */
        public ProvidableBeanHandle(BeanInstaller installer) {
            super(installer);
        }

        @Override
        protected ProvidableBeanConfiguration<T> newBeanConfiguration() {
            return new ProvidableBeanConfiguration<>(this);
        }
    }
}
