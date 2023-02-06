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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanInstallationException;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.bindings.Key;
import app.packed.bindings.Provider;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.BaseExtensionPoint.BeanInstaller;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import app.packed.lifetime.BeanLifetimeTemplate;
import app.packed.operation.BeanOperationTemplate;
import app.packed.operation.Op;
import internal.app.packed.bean.BeanSetupClassMapContainer.MuInst;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.PackedOp;

/**
 * This class is responsible for installing new beans.
 */
// Tror den bliver en nested classe paa BeanSetup
public final class PackedBeanInstaller implements BaseExtensionPoint.BeanInstaller {

    /** A list ofIllegal bean classes. Void is technically allowed but {@link #installWithoutSource()} needs to used. */
    // Allign with Key
    static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    @Nullable
    Map<Class<?>, Object> attachments;

    /** The container the bean is being installed into. */
    final ContainerSetup container;

    /** The extension that is installing the bean */
    final ExtensionSetup installingExtension;

    @Nullable
    BeanIntrospector introspector;

    boolean multiInstall;

    String namePrefix;

    /** The owner of the bean. */
    final BeanOwner owner;

    /** A bean mirror supplier */
    @Nullable
    Supplier<? extends BeanMirror> supplier;

    boolean synthetic;

    /** A template for the lifetime of the bean. */
    public final PackedBeanLifetimeTemplate template;

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
    public PackedBeanInstaller(ExtensionSetup installingExtension, BeanOwner owner, BeanLifetimeTemplate template) {
        this.container = installingExtension.container;
        this.installingExtension = requireNonNull(installingExtension);
        this.owner = requireNonNull(owner);
        this.template = (PackedBeanLifetimeTemplate) requireNonNull(template, "template is null");
    }

    /** {@inheritDoc} */
    @Override
    public <A> BeanInstaller attach(Class<A> attachmentType, A attachment) {
        requireNonNull(attachmentType, "attachmentType is null");
        requireNonNull(attachment, "attachment is null");
        if (!attachmentType.isInstance(attachment)) {
            throw new IllegalArgumentException("The specified attachement is not an instance of " + attachmentType);
        }
        Map<Class<?>, Object> a = attachments;
        if (a == null) {
            a = attachments = HashMap.newHashMap(1);
        }
        a.put(attachmentType, attachment);
        return this;
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
        HashMap<Class<?>, Object> bcm = container.beanClassMap;
        if (owner instanceof ExtensionSetup e) {
            bcm = e.beanClassMap;
        }
        Object object = bcm.get(beanClass);
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

    /** {@inheritDoc} */
    @Override
    public BeanHandle<Void> installWithoutSource() {
        if (template.kind != BeanKind.STATIC) {
            throw new InternalExtensionException("Only static beans can be source less");
        }
        return newBean(void.class, BeanSourceKind.NONE, null);
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller introspectWith(BeanIntrospector introspector) {
        this.introspector = requireNonNull(introspector, "introspector is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller multi() {
        if (template.kind == BeanKind.STATIC) {
            throw new InternalExtensionException("multiInstall is not supported for static beans");
        }
        multiInstall = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller namePrefix(String prefix) {
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
        if (sourceKind != BeanSourceKind.NONE && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }

        String prefix = namePrefix;
        if (prefix == null) {
            prefix = "Functional";
            BeanModel beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanModel(beanClass);

            if (beanModel != null) {
                prefix = beanModel.simpleName();
            }
        }
        // TODO virker ikke med functional beans og naming
        String n = prefix;

        HashMap<Class<?>, Object> bcm = container.beanClassMap;
        if (owner instanceof ExtensionSetup e) {
            bcm = e.beanClassMap;

        }

        BeanSetup bean = new BeanSetup(this, beanClass, sourceKind, source);

        ContainerSetup container = bean.container;

        if (beanClass != void.class) {
            if (multiInstall) {
                MuInst i = (MuInst) bcm.compute(beanClass, (c, o) -> {
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
                bcm.compute(beanClass, (c, o) -> {
                    if (o == null) {
                        return bean;
                    } else if (o instanceof BeanSetup) {
                        // singular???
                        throw new BeanInstallationException("A non-multi bean has already been defined for " + bean.beanClass);
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
            BeanOperationTemplate ot;
            if (bean.lifetime.lifetimes().isEmpty()) {
                ot = BeanOperationTemplate.defaults();
            } else {
                ot = bean.lifetime.lifetimes().get(0).template;
            }

            OperationSetup os = op.newOperationSetup(bean, bean.installedBy, ot, null);
            bean.operations.add(os);
        }

        if (bean.owner instanceof ExtensionSetup e && bean.beanKind == BeanKind.CONTAINER) {
            e.sm.addBean(bean);
        }

        // Scan the bean class for annotations unless the bean class is void
        if (sourceKind != BeanSourceKind.NONE) {
            new BeanScanner(bean, introspector, attachments).introspect();
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
    public BeanInstaller specializeMirror(Supplier<? extends BeanMirror> supplier) {
        requireNonNull(supplier, "supplier is null");
        this.supplier = supplier;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller synthetic() {
        synthetic = true;
        return this;
    }
}