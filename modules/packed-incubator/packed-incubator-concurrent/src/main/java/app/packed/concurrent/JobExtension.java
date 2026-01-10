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
package app.packed.concurrent;

import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import app.packed.extension.FrameworkExtension;

/**
 * This extension allows for multiple threads within an application.
 * <p>
 * There is nothing forcing anyone to use this extension for forking threads.
 *
 */

// Er det bare på BaseExtension?? Det taenker jeg

// Controls everything about threads...

// We can have a concurrency extension as well that is more.
// What kind of model do you use...

// Vi vil gerne have vores egne executors. Saa de bliver lukket ned
// naar containeren bliver shutdown...

// Must be enabled for Fork

// Hvad hvis lukker den ned asynchront indefra containeren??

// Maybe it is built-in..
// Can't fanthom many applications not threads
public final class JobExtension extends FrameworkExtension<JobExtension> {

    /**
     * Creates a new job extension.
     *
     * @param handle
     *            the extension's handle
     */
    JobExtension(ExtensionHandle<JobExtension> handle) {
        super(handle);
    }

    /**
     * Schedules an operation.
     *
     * @param op
     *            the operation that will be invoked
     * @return a configuration object representing the scheduled operation
     */
//    public ScheduledOperationConfiguration schedule(Op<?> op) {
//        throw new UnsupportedOperationException();
//    }

    /** {@inheritDoc} */
    @Override
    protected ExtensionPoint<JobExtension> newExtensionPoint(ExtensionPointHandle usesite) {
        return new JobExtensionPoint(usesite);
    }

}

// disableThis
// disableThis+Kids
// disableThis for everyone except LifecycleExtension
// disableThis for user

// Starting Threads
// Monitoring threads
// Thread limits

// Scheduling???
// As in Thread Scheduling???

// I think yes
// IDK maybe a scheduler.

// ForkedOperation (bean, operationId)
