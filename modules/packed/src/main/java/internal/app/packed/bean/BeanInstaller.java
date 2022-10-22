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
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.Installer;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanSourceKind;
import app.packed.container.InternalExtensionException;
import app.packed.operation.Op;
import app.packed.operation.Provider;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedExtensionPointContext;

/**
 *
 */
public final class BeanInstaller extends BeanHandle.Installer {

    /** Illegal bean classes. */
    static final Set<Class<?>> ILLEGAL_BEAN_CLASSES = Set.of(Void.class, Key.class, Op.class, Optional.class, Provider.class);

    final ExtensionSetup beanExtension;

    BeanIntrospector introspector;

    final BeanKind kind;

    boolean multiInstall;

    String namePrefix;

    boolean synthetic;

    @Nullable
    final PackedExtensionPointContext useSite;

    public BeanInstaller(ExtensionSetup beanExtension, BeanKind kind, @Nullable PackedExtensionPointContext useSite) {
        this.beanExtension = requireNonNull(beanExtension);
        this.kind = requireNonNull(kind, "kind is null");
        this.useSite = useSite;
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> install(Class<T> beanClass) {
        requireNonNull(beanClass, "beanClass is null");
        BeanSetup bs = BeanSetup.installx(this, beanClass, BeanSourceKind.CLASS, beanClass);
        return from(bs);
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> install(Op<T> operation) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Installer installIfAbsent(Consumer<? super BeanHandle<?>> onInstall) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> BeanHandle<T> installInstance(T instance) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<Void> installSourceless() {
        if (kind != BeanKind.FUNCTIONAL) {
            throw new InternalExtensionException("Only functional beans can be source less");
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Installer introspectWith(BeanIntrospector introspector) {
        if (!kind.hasInstances()) {
            throw new InternalExtensionException("Cannot set a custom introspector for functional or static beans");
        }
        this.introspector = requireNonNull(introspector, "introspector is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer multiInstall() {
        if (!kind.hasInstances()) {
            throw new InternalExtensionException("multiInstall is not supported for functional or static beans");
        }
        multiInstall = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer namePrefix(String prefix) {
        this.namePrefix = requireNonNull(prefix, "prefix is null");
        return this;

    }

    /** {@inheritDoc} */
    @Override
    public Installer synthetic() {
        synthetic = true;
        return this;
    }
}
