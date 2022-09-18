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

import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanHandle.Installer;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.container.ExtensionPoint.UseSite;
import app.packed.operation.Op;
import internal.app.packed.bean.hooks.BeanIntrospectionHelper;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedExtensionPointContext;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.op.PackedOp;

/** Implementation of BeanHandle.Builder. */
public final class PackedBeanHandleInstaller<T> implements BeanHandle.Installer<T> {

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** The container the bean will be installed into. */
    final ContainerSetup container;

    public boolean instanceless;

    /** A custom bean introspector that may be set via {@link #introspectWith(BeanIntrospector)}. */
    @Nullable
    BeanIntrospector introspector;

    /** The kind of bean. */
    BeanKind kind;

    /** Supplies a mirror for the operation */
    final Supplier<? extends BeanMirror> mirrorSupplier = () -> new BeanMirror();

    boolean nonUnique;

    /** The operator of the bean, or {@code null} for {@link BeanExtension}. */
    @Nullable
    final PackedExtensionPointContext operator;

    @Nullable
    PackedExtensionPointContext extensionOwner;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
    @Nullable
    public final Object source;

    /** The type of source the installer is created from. */
    public final BeanSourceKind sourceKind;

    private BeanSetup bean;

    private PackedBeanHandleInstaller(@Nullable UseSite operator, ContainerSetup container, Class<?> beanClass, BeanSourceKind sourceKind,
            @Nullable Object source) {
        this.operator = (@Nullable PackedExtensionPointContext) operator;
        this.container = requireNonNull(container);
        this.beanClass = requireNonNull(beanClass);
        this.sourceKind = requireNonNull(sourceKind);
        this.source = requireNonNull(source);
        this.instanceless = source == null;
    }

    /** {@inheritDoc} */
    public BeanKind beanKind() {
        return kind;
    }

    private void checkNotBuild() {
        if (bean != null) {
            throw new IllegalStateException("This installer can only be used once");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> forExtension(UseSite context) {
        requireNonNull(context, "context is null");
        checkNotBuild();
        this.extensionOwner = (PackedExtensionPointContext) context;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<T> install() {
        checkNotBuild();
        RealmSetup realm = extensionOwner == null ? container.realm : extensionOwner.extension().extensionRealm;

        // Can we call it more than once??? Why not
        realm.wireCurrentComponent();

        // Skal lave saa mange checks som muligt inde vi laver BeanSetup
        BeanSetup bean;
        if (extensionOwner == null) {
            this.bean = bean = new BeanSetup(this, realm);
        } else {
            this.bean = bean = new ExtensionBeanSetup(extensionOwner.extension(), this, realm);
        }

        // Scan the bean class for annotations unless the bean class is void or scanning is disabled
        if (sourceKind != BeanSourceKind.NONE) {
            new BeanIntrospectionHelper(bean, introspector).introspect();
        }

        return new PackedBeanHandle<>(bean);
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> introspectWith(BeanIntrospector introspector) {
        requireNonNull(introspector, "introspector is null");
        if (beanClass == void.class) {
            throw new UnsupportedOperationException("Cannot specify a introspector for beans that have void as their bean class");
        }
        checkNotBuild();
        this.introspector = introspector;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> instanceless() {
        if (sourceKind == BeanSourceKind.NONE) {
            throw new IllegalStateException("Beans that are specified without a source are already instanceless");
        } else if (sourceKind != BeanSourceKind.CLASS) {
            throw new IllegalStateException("Cannot call this method when a factory or instance source was specified when creating the installer");
        }
        checkNotBuild();
        instanceless = true;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> nonUnique() {
        checkNotBuild();
        nonUnique = true;
        return this;
    }

    public static <T> PackedBeanHandleInstaller<T> ofClass(@Nullable UseSite operator, ContainerSetup container, Class<T> clazz) {
        requireNonNull(clazz, "clazz is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return new PackedBeanHandleInstaller<>(operator, container, clazz, BeanSourceKind.CLASS, clazz);
    }

    public static <T> PackedBeanHandleInstaller<T> ofFactory(@Nullable UseSite operator, ContainerSetup container, Op<T> factory) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        PackedOp<T> fac = PackedOp.crack(factory);
        return new PackedBeanHandleInstaller<>(operator, container, fac.typeLiteral().rawType(), BeanSourceKind.OP, fac);
    }

    public static <T> PackedBeanHandleInstaller<T> ofInstance(@Nullable UseSite operator, ContainerSetup container, T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Op.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }

        // Optional is also not valid
        // or Provider, Lazy, ect
        // Ved heller ikke DependencyProvided beans

        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return new PackedBeanHandleInstaller<>(operator, container, instance.getClass(), BeanSourceKind.INSTANCE, instance);
    }

    public static PackedBeanHandleInstaller<?> ofNone(@Nullable UseSite operator, ContainerSetup container) {
        return new PackedBeanHandleInstaller<>(operator, container, void.class, BeanSourceKind.NONE, null);
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> kindUnmanaged() {
        kind = BeanKind.UNMANAGED;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> kindSingleton() {
        kind = BeanKind.SINGLETON;
        return this;
    }
}
