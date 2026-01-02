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

import app.packed.bean.Bean;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstaller;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanSourceKind;
import app.packed.context.Context;
import app.packed.extension.InternalExtensionException;
import app.packed.service.ProvidableBeanConfiguration;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.ContainerBeanStore.BeanClassKey;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.AbstractComponentInstaller;
import internal.app.packed.extension.ExtensionSetup;

/** Implementation of {@link BeanTemplate.Installer}. */
public final class PackedBeanInstaller extends AbstractComponentInstaller<BeanSetup, PackedBeanInstaller> implements BeanInstaller {

    /** The extension that is installing the bean. */
    final ExtensionSetup installledByExtension;

    String namePrefix;

    /** The owner of the bean. */
    final AuthoritySetup<?> owner;

    /** The bean's template. */
    // Maybe we can override it??? If we want to delegate
    public PackedBeanTemplate template;

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
    @SuppressWarnings("unchecked")
    @Override
    public <H extends BeanHandle<?>> H installIfAbsent(Class<?> beanClass, Class<? super H> handleClass,
            Function<? super BeanInstaller, H> handleFactory, Consumer<? super H> onNew) {
        requireNonNull(beanClass, "beanClass is null");
        BeanClassKey e = new BeanClassKey(owner.owner(), beanClass);
        BeanSetup existingBean = installledByExtension.container.beans.beanClasses.get(e);
        if (existingBean != null) {
            BeanHandle<?> existingHandle = existingBean.handle();

            if (ContainerBeanStore.isMultiInstall(existingBean)) {
                throw new IllegalArgumentException("MultiInstall Bean");
            } else if (!handleClass.isInstance(existingHandle)) {
                throw new IllegalStateException("A previous bean has been installed that used another handle type then " + existingHandle
                        + " was " + existingHandle.getClass());
            } else {
                // Probably need to check that we are the same extension that installed it
                return (H) existingBean.handle();
            }
        }

        H handle = BeanSetup.newBean(this, PackedBean.of(beanClass), handleFactory);
        onNew.accept(handle);
        return handle;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanInstaller namePrefix(String prefix) {
        checkNotUsed();
        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanInstaller setLocal(BeanLocal<T> local, T value) {
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

    /** {@inheritDoc} */
    @Override
    public <H extends BeanHandle<?>> H install(Bean<?> bean, Function<? super BeanInstaller, H> factory) {
        if (bean.beanSourceKind() == BeanSourceKind.SOURCELESS) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return BeanSetup.newBean(this, (PackedBean<?>) bean, factory);
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller addContext(Class<? extends Context<?>> contextClass) {
        return null;
    }
}
