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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.extension.BaseExtensionPoint;
import app.packed.extension.BaseExtensionPoint.BeanInstaller;
import app.packed.extension.InternalExtensionException;
import app.packed.framework.Nullable;
import app.packed.operation.Op;
import app.packed.operation.OperationTemplate;
import internal.app.packed.container.AssemblyModel;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.PackedOp;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

public final class PackedBeanInstaller implements BaseExtensionPoint.BeanInstaller {

    /** Illegal bean classes. */
    static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    /** A handle that can invoke {@link ComposerAssembly#doBuild(AssemblyModel, ContainerSetup)}. */
    private static final MethodHandle MH_NEW_BEAN_HANDLE = LookupUtil.lookupConstructorPrivate(MethodHandles.lookup(), BeanHandle.class, BeanSetup.class);

    @Nullable
    private Map<Class<?>, Object> attachments;

    final ExtensionSetup baseExtension;

    private BeanIntrospector introspector;

    /** The kind of bean being installed. */
    private final BeanKind kind;

    @Nullable
    public
    List<OperationTemplate> lifetimes;

    private boolean multiInstall;

    private String namePrefix;

    private boolean synthetic;

    @Nullable
    final PackedExtensionPointContext useSite;

    public PackedBeanInstaller(ExtensionSetup baseExtension, BeanKind kind, @Nullable PackedExtensionPointContext useSite) {
        this.baseExtension = requireNonNull(baseExtension);
        this.kind = requireNonNull(kind, "kind is null");
        this.useSite = useSite;
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

    private <T> BeanHandle<T> from(BeanSetup bs) {
        try {
            return (BeanHandle<T>) MH_NEW_BEAN_HANDLE.invokeExact(bs);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> install(Class<T> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        return install(beanClass, BeanSourceKind.CLASS, beanClass);
    }

    private <T> BeanHandle<T> install(Class<T> beanClass, BeanSourceKind sourceKind, Object source) {
        if (sourceKind != BeanSourceKind.NONE && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        BeanSetup bs = BeanSetup.install(this, kind, beanClass, sourceKind, source, introspector, attachments, namePrefix, multiInstall, synthetic);
        return from(bs);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanHandle<T> install(Op<T> op) {
        PackedOp<?> pop = PackedOp.crack(op);
        Class<?> beanClass = pop.type.returnType();
        return install((Class<T>) beanClass, BeanSourceKind.OP, pop);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> installIfAbsent(Class<T> beanClass, Consumer<? super BeanHandle<T>> onInstall) {
        requireNonNull(beanClass, "beanClass is null");
        HashMap<Class<?>, Object> bcm = baseExtension.container.beanClassMap;
        if (useSite != null) {
            bcm = useSite.usedBy().beanClassMap;
        }
        Object object = bcm.get(beanClass);
        if (object != null) {
            if (object instanceof BeanSetup b) {
                return from(b);
            } else {
                throw new IllegalArgumentException("MultiInstall Bean");
            }
        }
        BeanHandle<T> handle = install(beanClass, BeanSourceKind.CLASS, beanClass);
        onInstall.accept(handle);
        return handle;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanHandle<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> beanClass = instance.getClass();
        return install((Class<T>) beanClass, BeanSourceKind.INSTANCE, instance);
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<Void> installWithoutSource() {
        if (kind != BeanKind.FUNCTIONAL) {
            throw new InternalExtensionException("Only functional beans can be source less");
        }
        return install(void.class, BeanSourceKind.NONE, null);
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller introspectWith(BeanIntrospector introspector) {
        if (!kind.hasInstances()) {
            throw new InternalExtensionException("Cannot set a introspector for functional beans");
        }
        this.introspector = requireNonNull(introspector, "introspector is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller lifetimes(OperationTemplate... templates) {
        if (this.lifetimes != null) {
            throw new IllegalStateException("Lifetimes can only be set once");
        }
        this.lifetimes = List.of(templates);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller multi() {
        if (!kind.hasInstances()) {
            throw new InternalExtensionException("multiInstall is not supported for functional or static beans");
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

    /** {@inheritDoc} */
    @Override
    public BeanInstaller synthetic() {
        synthetic = true;
        return this;
    }
}