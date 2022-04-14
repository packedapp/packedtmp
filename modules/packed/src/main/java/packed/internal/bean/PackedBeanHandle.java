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

import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.bean.operation.Operation;

/** The implementation of {@link BeanHandle}. */
public record PackedBeanHandle<T>(BeanSetup bean) implements BeanHandle<T> {

    /** {@inheritDoc} */
    @Override
    public Class<?> beanClass() {
        return bean.beanClass();
    }

    /** {@inheritDoc} */
    @Override
    public BeanKind beanKind() {
        return bean.beanKind();
    }

    /** {@inheritDoc} */
    @Override
    public Operation addFunctionOperation(Object functionInstance) {
        throw new UnsupportedOperationException();
    }

//    @Nullable
//    private BeanSetup bean;
//
//    /** The bean class, is typical void.class for functional beans. */
//    private final Class<?> beanClass;
//
//    final ArrayList<BeanConfiguration> configurations = new ArrayList<>(1);
//
//    /** The container the bean is being installed in. */
//    public final ContainerSetup container;
//
//    /** The kind of bean. */
//    private final BeanKind kind;
//
//    /** Manages the operations defined by the bean. */
//    public final BeanOperationManager operations = new BeanOperationManager();
//
//    public final RealmSetup realm;
//
//    /** The source (Null, Class, Factory, Instance) */
//    @Nullable
//    public final Object source;
//
//    /** The type of source the driver is created from. */
//    public final SourceType sourceType;
//
//    PackedBeanHandle(PackedBeanHandleBuilder<T> builder) {
//        this.kind = builder.kind;
//        this.container = builder.container;
//        this.beanClass = builder.beanClass;
//        this.source = builder.source;
//        this.sourceType = builder.sourceType;
//        this.realm = builder.realm;
//    }
//
//    public PackedBeanHandle(BeanKind kind, ContainerSetup container, Realm userOrExtension, Class<?> beanType, SourceType sourceType, Object source) {
//        this.kind = requireNonNull(kind, "kind is null");
//        this.container = requireNonNull(container);
//        if (userOrExtension.isApplication()) {
//            this.realm = container.realm;
//        } else {
//            ExtensionSetup extension = container.extensions.get(userOrExtension.extension());
//            this.realm = extension.extensionTree;
//        }
//        this.beanClass = requireNonNull(beanType);
//
//        this.source = source;
//        this.sourceType = sourceType;
//    }
//
//    // Maaske er den paa BeanSupport???
//    /** {@inheritDoc} */
//    @Override
//    public Operation addFunctionOperation(Object functionInstance) {
//        // Problemer med at BeanDriver ikke har BeanSetup
//        throw new UnsupportedOperationException();
//    }
//
//    /** {@inheritDoc} */
//    public Class<?> beanClass() {
//        return beanClass;
//    }
//
//    /** {@inheritDoc} */
//    public BeanKind beanKind() {
//        return kind;
//    }
//
//    BeanSetup commit0() {
//        if (bean != null) {
//            return bean;
//        }
//
//        realm.wirePrepare();
//
//        // Skal lave saa mange checks som muligt inde vi laver BeanSetup
//
//        BeanSetup bs = bean = new BeanSetup(container, container.realm, this);
//        realm.wireCommit(bs);
//        return bs;
//    }

}
