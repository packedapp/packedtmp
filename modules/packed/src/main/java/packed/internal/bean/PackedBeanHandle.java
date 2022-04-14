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
package packed.internal.bean;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.operation.Operation;
import app.packed.component.Realm;
import app.packed.extension.Extension;
import app.packed.inject.Factory;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.factory.InternalFactory;

/** The implementation of {@link BeanHandle}. */
public final class PackedBeanHandle<T> implements BeanHandle<T> {

    @Nullable
    private BeanSetup bean;

    /** The bean class, is typical void.class for functional beans. */
    private final Class<?> beanClass;

    final ArrayList<BeanConfiguration> configurations = new ArrayList<>(1);

    /** The container the bean is being installed in. */
    public final ContainerSetup container;

    /** The kind of bean. */
    private final BeanKind kind;

    /** Manages the operations defined by the bean. */
    public final BeanOperationManager operations = new BeanOperationManager();

    public final RealmSetup realm;

    /** The source (Null, Class, Factory, Instance) */
    @Nullable
    public final Object source;

    /** The type of source the driver is created from. */
    public final SourceType sourceType;

    public PackedBeanHandle(BeanKind kind, ContainerSetup container, Realm userOrExtension, Class<?> beanType, SourceType sourceType, Object source) {
        this.kind = requireNonNull(kind, "kind is null");
        this.container = requireNonNull(container);
        if (userOrExtension.isApplication()) {
            this.realm = container.realm;
        } else {
            ExtensionSetup extension = container.extensions.get(userOrExtension.extension());
            this.realm = extension.extensionTree;
        }
        this.beanClass = requireNonNull(beanType);

        this.source = source;
        this.sourceType = sourceType;
    }

    // Maaske er den paa BeanSupport???
    /** {@inheritDoc} */
    @Override
    public Operation addFunctionOperation(Object functionInstance) {
        // Problemer med at BeanDriver ikke har BeanSetup
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public Class<?> beanClass() {
        return beanClass;
    }

    /** {@inheritDoc} */
    public BeanKind beanKind() {
        return kind;
    }

    /** {@inheritDoc} */
    @Override
    public BeanHandle<T> commit() {
        commit0();
        return this;
    }

    private BeanSetup commit0() {
        if (bean != null) {
            return bean;
        }

        realm.wirePrepare();

        // Skal lave saa mange checks som muligt inde vi laver BeanSetup

        BeanSetup bs = bean = new BeanSetup(container, container.realm, this);
        realm.wireCommit(bs);
        return bs;
    }
    
    /** {@inheritDoc} */
    public BeanSetup newSetup(BeanConfiguration configuration) {
        requireNonNull(configuration);
        configurations.add(configuration);
        return commit0();
    }

    static BeanKind checkKind(BeanKind kind, int type) {
        return kind;
    }

    public static <T> PackedBeanHandle<T> ofClass(BeanKind kind, ContainerSetup container, Class<? extends Extension<?>> operator, Realm owner,
            Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return new PackedBeanHandle<>(kind, container, owner, implementation, SourceType.CLASS, implementation);
    }

    public static <T> PackedBeanHandle<T> ofFactory(BeanKind kind, ContainerSetup container, Class<? extends Extension<?>> operator, Realm owner,
            Factory<T> factory) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        InternalFactory<T> fac = InternalFactory.crackFactory(factory);
        return new PackedBeanHandle<>(kind, container, owner, fac.rawReturnType(), SourceType.FACTORY, fac);
    }

    public static <T> PackedBeanHandle<T> ofInstance(BeanKind kind, ContainerSetup container, Class<? extends Extension<?>> operator, Realm owner, T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return new PackedBeanHandle<>(kind, container, owner, instance.getClass(), SourceType.INSTANCE, instance);
    }

    public static BeanHandle<?> ofNone(BeanKind kind, ContainerSetup container, Class<? extends Extension<?>> operator, Realm owner) {
        return new PackedBeanHandle<>(kind, container, owner, void.class, SourceType.NONE, null);
    }

    public enum SourceType {
        CLASS, FACTORY, INSTANCE, NONE;
    }
}
