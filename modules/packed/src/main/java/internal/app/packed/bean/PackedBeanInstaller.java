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

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.bean.BeanExtensionPoint;
import app.packed.bean.BeanExtensionPoint.BeanInstaller;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.container.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.operation.op.PackedOp;

public final class PackedBeanInstaller extends BeanExtensionPoint.BeanInstaller {

    /** Illegal bean classes. */
    static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    /** The kind of bean being installed. */
    private final BeanKind kind;

    private BeanIntrospector introspector;

    private String namePrefix;

    private boolean multiInstall;

    private boolean synthetic;

    final ExtensionSetup beanExtension;

    @Nullable
    final PackedExtensionPointContext useSite;

    public PackedBeanInstaller(ExtensionSetup beanExtension, BeanKind kind, @Nullable PackedExtensionPointContext useSite) {
        this.beanExtension = requireNonNull(beanExtension);
        this.kind = requireNonNull(kind, "kind is null");
        this.useSite = useSite;
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> install(Class<T> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        return install(beanClass, BeanSourceKind.CLASS, beanClass);
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
    @SuppressWarnings("unchecked")
    @Override
    public <T> BeanHandle<T> installInstance(T instance) {
        requireNonNull(instance, "instance is null");
        Class<?> beanClass = instance.getClass();
        return install((Class<T>) beanClass, BeanSourceKind.INSTANCE, instance);
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<Void> installNoSource() {
        if (kind != BeanKind.FUNCTIONAL) {
            throw new InternalExtensionException("Only functional beans can be source less");
        }
        return install(void.class, BeanSourceKind.NONE, null);
    }

    private <T> BeanHandle<T> install(Class<T> beanClass, BeanSourceKind sourceKind, Object source) {
        if (sourceKind != BeanSourceKind.NONE && ILLEGAL_BEAN_CLASSES.contains(beanClass)) {
            throw new IllegalArgumentException("Cannot install a bean with bean class " + beanClass);
        }
        BeanSetup bs = BeanSetup.install(this, kind, beanClass, sourceKind, source, introspector, namePrefix, multiInstall, synthetic);
        return from(bs);
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
    public BeanInstaller multiInstall() {
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
    public BeanInstaller onlyInstallIfAbsent(Consumer<? super BeanHandle<?>> onInstall) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public BeanInstaller synthetic() {
        synthetic = true;
        return this;
    }
}