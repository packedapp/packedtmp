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
import java.util.function.Supplier;

import app.packed.bean.BeanKind;
import app.packed.bean.BeanLocal;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.container.AuthorSetup;
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

/**
 * This class is responsible for installing new beans.
 */
public final class PackedBeanHandleBuilder implements BeanHandle.Builder {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Allign with Key
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Class.class, Key.class, Op.class, Optional.class, Provider.class);

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
    final AuthorSetup owner;

    /** The bean's template. */
    public final PackedBeanTemplate template;

    /**
     * Create a new installer.
     *
     * @param installingExtension
     *            the extension who has created the installer
     * @param owner
     *            the owner of the new bean
     * @param template
     *            a lifetime template for the new bean
     */
    public PackedBeanHandleBuilder(ExtensionSetup installingExtension, AuthorSetup owner, BeanTemplate template) {
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
    private void checkIsBuildable() {}

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> install(Class<T> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        return newBean(beanClass, BeanSourceKind.CLASS, beanClass);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanHandle<T> install(Op<T> op) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnRawType();
        return newBean((Class<T>) beanClass, BeanSourceKind.OP, pop);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall) {
        requireNonNull(beanClass, "beanClass is null");

        BeanClassKey e = new BeanClassKey(owner.author(), beanClass);
        BeanSetup existingBean = container.beans.beanClasses.get(e);
        if (existingBean != null) {
            if (ContainerBeanStore.isMultiInstall(existingBean)) {
                throw new IllegalArgumentException("MultiInstall Bean");
            } else {
                return new PackedBeanHandle<>(existingBean);
            }
        }
        BeanHandle<T> handle = newBean(beanClass, BeanSourceKind.CLASS, beanClass);
        onInstall.accept(handle);
        return handle;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanHandle<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> beanClass = instance.getClass();
        return newBean((Class<T>) beanClass, BeanSourceKind.INSTANCE, instance);
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
    public BeanHandle<Void> installSourceless() {
        if (template.kind() != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return newBean(void.class, BeanSourceKind.SOURCELESS, null);
    }

    /** {@inheritDoc} */
    @Override
    public <T> PackedBeanHandleBuilder localSet(BeanLocal<T> local, T value) {
        requireNonNull(local);
        requireNonNull(value);
        checkIsBuildable();
        locals.put(local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandleBuilder namePrefix(String prefix) {
        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;
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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T> BeanHandle<T> newBean(Class<T> beanClass, BeanSourceKind sourceKind, @Nullable Object source) {
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        // Creates Bean
        BeanSetup bean = new BeanSetup(this, beanClass, sourceKind, source);

        // Initialize the name of the bean
        container.beans.installAndSetBeanName(bean, namePrefix);

        // Copy any bean locals that have been set, we need to set this before introspection
        // I think maybe we need to do this as the last action?
        for (Entry<BeanLocal<?>, Object> e : locals.entrySet()) {
            container.application.locals.set((BeanLocal) e.getKey(), bean, e.getValue());
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

        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandleBuilder specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        checkIsBuildable();
        this.mirrorSupplier = supplier;
        return this;
    }
}
