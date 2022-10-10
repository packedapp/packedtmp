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
import app.packed.bean.BeanKind;
import app.packed.bean.BeanMirror;
import app.packed.bean.BeanSourceKind;
import app.packed.operation.Op;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.RealmSetup;
import internal.app.packed.operation.op.PackedOp;

/** Implementation of BeanHandle.Builder. */
public final class PackedBeanHandleInstaller<T> implements BeanHandle.Installer<T> {

    /** The bean class, is typical void.class for functional beans. */
    public final Class<?> beanClass;

    /** A custom bean introspector that may be set via {@link #introspectWith(BeanIntrospector)}. */
    @Nullable
    BeanIntrospector introspector;

    /** The kind of bean. */
    BeanKind kind;

    /** Supplies a mirror for the operation */
    final Supplier<? extends BeanMirror> mirrorSupplier = () -> new BeanMirror();

    boolean nonUnique;

    /** The operator of the bean, or {@code null} for {@link BeanExtension}. */
    final ExtensionSetup operator;

    final RealmSetup realm;

    /** The source ({@code null}, {@link Class}, {@link PackedOp}, or an instance) */
    @Nullable
    public final Object source;

    /** The type of source the installer is created from. */
    public final BeanSourceKind sourceKind;

    private BeanSetup bean;
    /** A model of hooks on the bean class. Or null if no member scanning was performed. */
    @Nullable
    public final BeanClassModel beanModel;

    public final boolean instantiate;

    @Nullable
    public final ExtensionSetup extensionOwner;

    private PackedBeanHandleInstaller(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Class<?> beanClass,
            BeanSourceKind sourceKind, @Nullable Object source, boolean instantiate) {
        this.operator = requireNonNull(operator);
        this.realm = requireNonNull(realm);
        this.beanClass = requireNonNull(beanClass);
        this.sourceKind = requireNonNull(sourceKind);
        this.source = requireNonNull(source);
        this.instantiate = instantiate;
        this.extensionOwner = extensionOwner;
        this.beanModel = sourceKind == BeanSourceKind.NONE ? null : new BeanClassModel(beanClass);// realm.accessor().beanModelOf(driver.beanClass());
    }

    public String initialName() {
        if (beanModel != null) {
            beanModel.simpleName();
        }
        return "Functional";
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
    public BeanHandle<T> install() {
        checkNotBuild();

        // Can we call it more than once??? Why not
        realm.wireCurrentComponent();

        this.bean = new BeanSetup(this, realm);

        // bean.initName

        // Scan the bean class for annotations unless the bean class is void or scanning is disabled
        if (sourceKind != BeanSourceKind.NONE) {
            new Introspector(bean, introspector).introspect();
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
    public Installer<T> nonUnique() {
        checkNotBuild();
        nonUnique = true;
        return this;
    }

    public static <T> PackedBeanHandleInstaller<T> ofClass(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Class<T> clazz,
            boolean instantiate) {
        requireNonNull(clazz, "clazz is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return new PackedBeanHandleInstaller<>(operator, realm, extensionOwner, clazz, BeanSourceKind.CLASS, clazz, instantiate);
    }

    public static <T> PackedBeanHandleInstaller<T> ofFactory(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, Op<T> op) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        PackedOp<T> pop = PackedOp.crack(op);
        return new PackedBeanHandleInstaller<>(operator, realm, extensionOwner, pop.type().returnType(), BeanSourceKind.OP, pop, true);
    }

    public static <T> PackedBeanHandleInstaller<T> ofInstance(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner, T instance) {
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
        return new PackedBeanHandleInstaller<>(operator, realm, extensionOwner, instance.getClass(), BeanSourceKind.INSTANCE, instance, false);
    }

    public static PackedBeanHandleInstaller<?> ofNone(ExtensionSetup operator, RealmSetup realm, @Nullable ExtensionSetup extensionOwner) {
        return new PackedBeanHandleInstaller<>(operator, realm, extensionOwner, void.class, BeanSourceKind.NONE, null, false);
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> kindUnmanaged() {
        kind = BeanKind.MANYTON;
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public Installer<T> kindSingleton() {
        kind = BeanKind.CONTAINER;
        return this;
    }
}
