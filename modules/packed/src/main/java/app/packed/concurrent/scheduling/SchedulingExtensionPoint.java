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
package app.packed.concurrent.scheduling;

import app.packed.extension.DelegatingOperationHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.OperationHandle;

/**
 *
 */
public class SchedulingExtensionPoint extends ExtensionPoint<SchedulingExtension> {

    /** No s. */
    SchedulingExtensionPoint() {}

    /**
     * Schedules an operation.
     *
     * @param operation
     *            the operation to schedule
     * @return a configuration object
     */
    public ScheduledOperationConfiguration schedule(DelegatingOperationHandle handle) {
        OperationHandle oh = handle.newOperation(SchedulingExtension.SOT, context());
        ScheduledOperationConfiguration p = new ScheduledOperationConfiguration(null, oh);
        extension().ops.add(p);
        return p;
    }
}
