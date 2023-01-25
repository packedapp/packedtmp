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
package app.packed.concurrent;

import app.packed.concurrent.SchedulingExtension.ConfigurableSchedule;
import app.packed.extension.ExtensionPoint;
import app.packed.operation.OperationHandle;

/**
 *
 */
public class SchedulingExtensionPoint extends ExtensionPoint<SchedulingExtension> {
    SchedulingExtensionPoint() {}

    /**
     * Schedules an operation.
     * 
     * @param operation
     *            the operation to schedule
     * @return a configuration object
     */
    // Maybe it is initially empty schedule
    public ScheduledOperationConfiguration schedule(OperationHandle operation) {
        ConfigurableSchedule p = new ConfigurableSchedule(new Schedule(100), operation);
        extension().ops.add(p);
        return new ScheduledOperationConfiguration(p, operation);
    }
}
