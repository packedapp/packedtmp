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

import app.packed.bean.BeanDriver;
import app.packed.bean.BeanKind;
import app.packed.bean.BeanOldKind;
import app.packed.component.ComponentConfiguration;
import app.packed.component.UserOrExtension;
import app.packed.inject.Factory;
import packed.internal.bean.hooks.usesite.HookModel;
import packed.internal.container.ContainerSetup;
import packed.internal.container.RealmSetup;
import packed.internal.inject.InternalFactory;

/** The implementation of {@link BeanDriver}. */
public final class PackedBeanDriver<T> implements BeanDriver<T> {

    final Class<?> beanType;

    public ComponentConfiguration configuration;

    final ContainerSetup container;

    boolean extensionBean;

    final InternalFactory<?> factory;

    /** A model of the hooks on the bean. */
    public final HookModel hookModel;

    private final BeanKind kind;
    BeanOldKind Oldkind = BeanOldKind.CONTAINER_BEAN;

    final RealmSetup realm;

    final Object source;

    public PackedBeanDriver(BeanKind kind, ContainerSetup container, UserOrExtension userOrExtension, Class<?> beanType, InternalFactory<?> factory,
            Object source) {
        this.kind = requireNonNull(kind);
        this.container = requireNonNull(container);
        if (userOrExtension.isUser()) {
            this.realm = container.realm;
        } else {
            this.realm = container.extensions.get(userOrExtension.extension()).realm();
        }
        this.beanType = requireNonNull(beanType);
        this.source = source;
        this.factory = factory;
        this.hookModel = realm.accessor().beanModelOf(beanType);
    }

    public Class<?> beanClass() {
        return beanType;
    }

    public void extensionBean() {
        this.extensionBean = true;
    }

    public BeanKind kind() {
        return kind;
    }

    /** {@inheritDoc} */
    public BeanSetup newSetup(ComponentConfiguration configuration) {
        if (this.configuration != null) {
            throw new IllegalStateException("This driver can only be bound once");
        }
        this.configuration = requireNonNull(configuration);
        realm.wirePrepare();

        // Skal lave saa mange checks som muligt inde vi laver BeanSetup

        BeanSetup bs = new BeanSetup(container, container.realm, container.lifetime, this);
        realm.wireCommit(bs);
        return bs;
    }

    /** {@inheritDoc} */
    @Override
    public void prototype() {
//        else if (kind != BeanType.BASE) {
//            throw new UnsupportedOperationException("Can only bind instances to singleton beans, kind = " + kind);
//        }
        this.Oldkind = BeanOldKind.PROTOTYPE_UNMANAGED;
    }

    private static BeanKind checkKind(BeanKind kind, int type) {

        return kind;
    }

    public static <T> PackedBeanDriver<T> ofFactory(BeanKind kind, ContainerSetup container, UserOrExtension owner, Factory<T> factory) {
        // Hmm, vi boer vel checke et eller andet sted at Factory ikke producere en Class eller Factorys
        requireNonNull(factory, "factory is null");
        InternalFactory<T> f = InternalFactory.canonicalize(factory);
        return new PackedBeanDriver<>(kind, container, owner, f.rawType(), f, f);
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
        return new PackedBeanDriver<>(kind, container, owner, instance.getClass(), null, instance);
    }

}
