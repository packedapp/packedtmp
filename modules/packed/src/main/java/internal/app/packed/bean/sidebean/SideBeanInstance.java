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

import app.packed.bean.BeanHandle;
import app.packed.bean.sidebean.SidebeanUseSite;
import app.packed.operation.OperationHandle;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreEntry;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public sealed abstract class SideBeanInstance implements SidebeanUseSite, LifetimeStoreEntry {

    /** The sidebean. */
    public final SideBeanHandle<?> handle;

    /** The bean this sidebean is applied to. */
    public final BeanSetup bean;

    @Nullable
    public LifetimeStoreIndex lifetimeStoreIndex;

    public final BeanSetup sidebean;

    SideBeanInstance(SideBeanHandle<?> handle, BeanSetup bean) {
        this.handle = handle;
        this.bean = bean;
        this.sidebean = BeanSetup.crack(handle);
    }

    public static final class OfBean extends SideBeanInstance {

        /**
         * @param handle
         * @param bean
         */
        public OfBean(SideBeanHandle<?> sideBeanHandle, BeanHandle<?> handle) {
            super(sideBeanHandle, BeanSetup.crack(handle));
        }
    }

    public static final class OfOperation extends SideBeanInstance {

        /**
         * @param handle
         * @param bean
         */
        public OfOperation(SideBeanHandle<?> sideBeanHandle, OperationHandle<?> handle) {
            super(sideBeanHandle, OperationSetup.crack(handle).bean);
        }
    }
}
