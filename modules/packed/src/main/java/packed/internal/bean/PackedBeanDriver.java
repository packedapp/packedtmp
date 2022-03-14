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

import app.packed.base.Nullable;
import app.packed.bean.BeanDriver;
import app.packed.bean.BeanKind;
import app.packed.component.ComponentConfiguration;
import app.packed.component.UserOrExtension;
import app.packed.inject.Factory;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;

/** The implementation of {@link BeanDriver}. */
public final class PackedBeanDriver<T> implements BeanDriver<T> {

    /** The bean type, is typical void.class for functional beans. */
    final Class<?> beanType;

    public ComponentConfiguration configuration;

    /** The container the bean is being installed in. */
    final ContainerSetup container;

    /** Any extension this bean belongs to. */
    @Nullable
    final ExtensionSetup extension;

    /** The kind of bean. */
    private final BeanKind kind;

    final RealmSetup realm;

    /** The source (Null, Class, Factory, Instance) */
    @Nullable
    final Object source;

    /** The type of source the driver is created from. */
    final SourceType sourceType;

    public PackedBeanDriver(BeanKind kind, ContainerSetup container, UserOrExtension userOrExtension, Class<?> beanType, SourceType sourceType, Object source) {
        this.kind = requireNonNull(kind, "kind is null");
        this.container = requireNonNull(container);
        if (userOrExtension.isUser()) {
            this.realm = container.realm;
            this.extension = null;
        } else {
            this.extension = container.extensions.get(userOrExtension.extension());
            this.realm = extension.realm();
        }
        this.beanType = requireNonNull(beanType);

        this.source = source;
        this.sourceType = sourceType;
    }

    /** {@inheritDoc} */
    public Class<?> beanClass() {
        return beanType;
    }

    /** {@inheritDoc} */
    public BeanKind beanKind() {
        return kind;
    }

    /** {@inheritDoc} */
    public BeanSetup newSetup(ComponentConfiguration configuration) {
        if (this.configuration != null) {
            throw new IllegalStateException("This driver can only be used once");
        }

        this.configuration = requireNonNull(configuration);
        realm.wirePrepare();

        // Skal lave saa mange checks som muligt inde vi laver BeanSetup

        BeanSetup bs = new BeanSetup(container, container.realm, this);
        realm.wireCommit(bs);
        return bs;
    }

    static BeanKind checkKind(BeanKind kind, int type) {

        return kind;
    }

    public static BeanDriver<?> of(BeanKind kind, ContainerSetup container, UserOrExtension owner) {
        return new PackedBeanDriver<>(kind, container, owner, void.class, SourceType.NONE, null);
    }

    public static <T> PackedBeanDriver<T> ofClass(BeanKind kind, ContainerSetup container, UserOrExtension owner, Class<T> implementation) {
        requireNonNull(implementation, "implementation is null");
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys, eller void, eller xyz
        return new PackedBeanDriver<>(kind, container, owner, implementation, SourceType.CLASS, implementation);
    }

    public static <T> PackedBeanDriver<T> ofFactory(BeanKind kind, ContainerSetup container, UserOrExtension owner, Factory<T> factory) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        requireNonNull(factory, "factory is null");
        return new PackedBeanDriver<>(kind, container, owner, factory.rawType(), SourceType.FACTORY, factory);
    }

    public static <T> PackedBeanDriver<T> ofInstance(BeanKind kind, ContainerSetup container, UserOrExtension owner, T instance) {
        requireNonNull(instance, "instance is null");
        if (Class.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Class instance to this method, was " + instance);
        } else if (Factory.class.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot specify a Factory instance to this method, was " + instance);
        }
        // TODO check kind
        // cannot be operation, managed or unmanaged, Functional
        return new PackedBeanDriver<>(kind, container, owner, instance.getClass(), SourceType.INSTANCE, instance);
    }

    public enum SourceType {
        CLASS, FACTORY, INSTANCE, NONE;
    }
}
