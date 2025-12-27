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
package internal.app.packed.bean.sidebean;

import static java.util.Objects.requireNonNull;

import app.packed.bean.BeanHandle;
import app.packed.bean.sidebean.SidebeanAttachment;
import app.packed.binding.Key;
import app.packed.operation.OperationHandle;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreEntry;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public sealed abstract class PackedSidebeanAttachment implements SidebeanAttachment, LifetimeStoreEntry {

    /** The bean this sidebean is applied to. */
    public final BeanSetup bean;

    @Nullable
    public LifetimeStoreIndex lifetimeStoreIndex;

    /** The sidebean. */
    public final BeanSetup sidebean;

    PackedSidebeanAttachment(SidebeanHandle<?> handle, BeanSetup bean) {
        this.bean = requireNonNull(bean);
        this.sidebean = BeanSetup.crack(handle);
    }


    /** {@inheritDoc} */
    @Override
    public <T> void bindBuildConstant(Key<T> key, T object) {}


    public static final class OfBean extends PackedSidebeanAttachment {

        /**
         * @param handle
         * @param bean
         */
        public OfBean(SidebeanHandle<?> sideBeanHandle, BeanHandle<?> handle) {
            super(sideBeanHandle, BeanSetup.crack(handle));
        }

    }

    public static final class OfOperation extends PackedSidebeanAttachment {

        public final OperationSetup operation;

        /**
         * @param handle
         * @param bean
         */
        public OfOperation(SidebeanHandle<?> sideBeanHandle, OperationHandle<?> handle) {
            OperationSetup operation = this.operation = OperationSetup.crack(handle);
            super(sideBeanHandle, operation.bean);
        }
    }
}
