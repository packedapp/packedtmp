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
import java.util.function.Supplier;

import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.extension.BeanLocal;
import app.packed.extension.InternalExtensionException;
import app.packed.extension.bean.BeanBuilder;
import app.packed.extension.bean.BeanHandle;
import app.packed.extension.bean.BeanTemplate;
import app.packed.extension.operation.OperationTemplate;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import app.packed.util.Key;
import app.packed.util.Nullable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ContainerSetup.BeanClassKey;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.PackedBeanTemplate;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;

/**
 * This class is responsible for installing new beans.
 */
public final class PackedBeanBuilder implements BeanBuilder {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Allign with Key
    public static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    /** The container the bean is being installed into. */
    final ContainerSetup container;

    /** The extension that is installing the bean */
    final ExtensionSetup installingExtension;

    // TODO: If an installer should be reusable we need to copy locals
    // Right now we just store this map in BeanSetup
    final IdentityHashMap<PackedBeanLocal<?>, Object> locals = new IdentityHashMap<>();

    boolean multiInstall;

    String namePrefix;

    /** The owner of the bean. */
    final BeanOwner owner;

    /** A bean mirror supplier */
    @Nullable
    Supplier<? extends BeanMirror> supplier;

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
    public PackedBeanBuilder(ExtensionSetup installingExtension, BeanOwner owner, BeanTemplate template) {
        this.container = installingExtension.container;
        this.installingExtension = requireNonNull(installingExtension);
        this.owner = requireNonNull(owner);
        this.template = (PackedBeanTemplate) requireNonNull(template, "template is null");
    }

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

        BeanClassKey e = new BeanClassKey(owner.realm(), beanClass);
        Object object = container.beanClassMap.get(e);
        if (object != null) {
            if (object instanceof BeanSetup b) {
                return new PackedBeanHandle<>(b);
            } else {
                throw new IllegalArgumentException("MultiInstall Bean");
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


    /**
     * Allows multiple beans of the same type in a container.
     * <p>
     * By default, a container only allows a single bean of particular type if non-void.
     *
     * @return this builder
     * @throws UnsupportedOperationException
     *             if bean kind is {@link BeanKind#FUNCTIONAL} or {@link BeanKind#STATIC}
     */
    // I don't see this not living on BeanTemplate
    // IDK det er en af de ting
    // allowClones?

    // Den fungere bare andre steder end paa BeanConfiguration

    // Lad os sige vi vil have SE.schedule og SE.scheduleMulti...
    // Det giver ingen mening. Den skal vaere paa BeanConfiguration
    public BeanBuilder multi() {
        if (template.kind() == BeanKind.STATIC) {
            throw new InternalExtensionException("multiInstall is not supported for static beans");
        }
        multiInstall = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanBuilder namePrefix(String prefix) {
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
    private <T> BeanHandle<T> newBean(Class<T> beanClass, BeanSourceKind sourceKind, Object source) {
        if (sourceKind != BeanSourceKind.SOURCELESS && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }

        String prefix = namePrefix;
        if (prefix == null) {
            prefix = "Functional";
            BeanModel beanModel = sourceKind == BeanSourceKind.SOURCELESS ? null : new BeanModel(beanClass);

            if (beanModel != null) {
                prefix = beanModel.simpleName();
            }
        }
        // TODO virker ikke med functional beans og naming
        String n = prefix;

        BeanSetup bean = new BeanSetup(this, beanClass, sourceKind, source);

        BeanClassKey key = new BeanClassKey(owner.realm(), beanClass);
        if (beanClass != void.class) {
            if (multiInstall) {
                MuInst i = (MuInst) container.beanClassMap.compute(key, (c, o) -> {
                    if (o == null) {
                        return new MuInst();
                    } else if (o instanceof BeanSetup) {
                        throw new BeanInstallationException("Oops");
                    } else {
                        ((MuInst) o).counter += 1;
                        return o;
                    }
                });
                int next = i.counter;
                if (next > 0) {
                    n = prefix + next;
                }
                while (container.children.putIfAbsent(n, bean) != null) {
                    n = prefix + ++next;
                    i.counter = next;
                }
            } else {
                container.beanClassMap.compute(key, (c, o) -> {
                    if (o == null) {
                        return bean;
                    } else if (o instanceof BeanSetup) {
                        // singular???
                        throw new BeanInstallationException("A bean of type [" + bean.beanClass + "] has already been added to " + container.path());
                    } else {
                        // We already have some multiple beans installed
                        throw new BeanInstallationException("Oops");
                    }
                });
                // Not multi install, so should be able to add it first time
                int size = 0;
                while (container.children.putIfAbsent(n, bean) != null) {
                    n = prefix + ++size;
                }
            }
        }
        bean.name = n;

        if (sourceKind == BeanSourceKind.OP) {
            PackedOp<?> op = (PackedOp<?>) bean.beanSource;
            OperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = OperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            OperationSetup os = op.newOperationSetup(bean, bean.installedBy, ot, null);
            bean.operations.add(os);
        }

        if (bean.owner instanceof ExtensionSetup es && bean.beanKind == BeanKind.CONTAINER) {
            es.sm.addBean(bean);
        }

        // Scan the bean class for annotations unless the bean class is void
        if (sourceKind != BeanSourceKind.SOURCELESS) {
            new BeanScanner(bean).introspect();
        }

        // Bean was successfully created, add it to the container
        BeanSetup siebling = container.beanLast;
        if (siebling == null) {
            container.beanFirst = bean;
        } else {
            siebling.beanSiblingNext = bean;
        }
        container.beanLast = bean;

        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanBuilder setLocal(BeanLocal<T> local, T value) {
        locals.put((PackedBeanLocal<?>) local, value);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanBuilder specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        checkNotUsed();
        this.supplier = supplier;
        return this;
    }

    /**
     * Checks that the builder has not been used to create a new bean.
     * <p>
     * There is technically no reason to not allow this. But we will need to make a copy of the locals if we want to support
     * this.
     */
    private void checkNotUsed() {

    }

    static class MuInst {
        int counter;
    }
}