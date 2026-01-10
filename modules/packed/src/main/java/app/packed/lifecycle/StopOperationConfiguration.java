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
package app.packed.lifecycle;

import app.packed.operation.OperationHandle;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StopOperationHandle;
import internal.app.packed.lifecycle.PackedBeanLifecycleKind;

/**
 * The configuration of a {@link Stop} operation.
 */
public final class StopOperationConfiguration extends LifecycleOperationConfiguration {

    /** The handle representing the operation. */
    private final StopOperationHandle handle;

    public StopOperationConfiguration(OperationHandle<?> handle) {
        this.handle = (StopOperationHandle) handle;
        super(handle);
    }

    public boolean isNaturalOrder() {
        return handle.lifecycleKind == PackedBeanLifecycleKind.STOP_POST_ORDER;
    }

    // Maaske kan man ikke rette det
    public void setNaturalOrder(boolean isNaturalOrder) {
        throw new UnsupportedOperationException();
    }
}
