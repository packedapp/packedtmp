/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.bean.sidehandle;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.bean.BeanHandle;
import app.packed.binding.Key;
import app.packed.component.Sidehandle;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreEntry;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.util.ServiceMap;

/**
 * Implementation of {@link Sidehandle}. This class defines a number a number of subclasses, each supporting a different
 * type of component.
 */
// Alternativt har vi en klasse, og saa nulls fx for bean/operation
public sealed abstract class PackedSidehandle implements Sidehandle, LifetimeStoreEntry {

    /** The bean this sidebean is applied to. */
    public final BeanSetup bean;

    public final ServiceMap<Object> constants = new ServiceMap<>();

    @Nullable
    public LifetimeStoreIndex lifetimeStoreIndex;

    /** The bean that defines the Sidehandle bean that is applied to components. */
    public final BeanSetup sidehandleBean;

    PackedSidehandle(BeanSetup sidehandleBean, BeanSetup bean) {
        this.bean = requireNonNull(bean);
        this.sidehandleBean = requireNonNull(sidehandleBean);
    }

    @Override
    public final <T> void bindComputedConstant(Key<T> key, Supplier<? extends T> supplier) {}

    /** {@inheritDoc} */
    @Override
    public final <T> void bindConstant(Key<T> key, T object) {
        // TODO check type
        if (constants.putIfAbsent(key, object) != null) {
            throw new IllegalStateException();
        }
    }

    public static final class OfBean extends PackedSidehandle {

        /**
         * @param handle
         * @param bean
         */
        public OfBean(BeanSetup sideBeanHandle, BeanHandle<?> handle) {
            super(sideBeanHandle, BeanSetup.crack(handle));
        }

    }

    public static final class OfOperation extends PackedSidehandle {

        public final OperationSetup operation;

        /**
         * @param handle
         * @param bean
         */
        public OfOperation(BeanSetup sideBeanHandle, OperationSetup operation) {
            this.operation = requireNonNull(operation);
            super(sideBeanHandle, operation.bean);
        }

    }
}
