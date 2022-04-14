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
import app.packed.bean.BeanHandle;
import app.packed.bean.BeanKind;
import app.packed.component.Realm;
import packed.internal.bean.PackedBeanHandle.SourceType;
import packed.internal.container.ContainerSetup;
import packed.internal.container.ExtensionSetup;
import packed.internal.container.RealmSetup;

/**
 *
 */
public class PackedBeanHandleBuilder<T> implements BeanHandle.Builder<T> {

    /** The bean class, is typical void.class for functional beans. */
    final Class<?> beanClass;

    final ContainerSetup container;

    /** The kind of bean. */
    final BeanKind kind;

    final RealmSetup realm;

    /** The source (Null, Class, Factory, Instance) */
    @Nullable
    final Object source;

    /** The type of source the driver is created from. */
    final SourceType sourceType;

    public PackedBeanHandleBuilder(BeanKind kind, ContainerSetup container, Realm userOrExtension, Class<?> beanType, SourceType sourceType, Object source) {
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

    /** {@inheritDoc} */
    @Override
    public PackedBeanHandle<T> build() {
        return new PackedBeanHandle<>(this);
    }
}
