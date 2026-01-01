/*
x * Copyright (c) 2008 Kasper Nielsen.
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
package app.packed.concurrent.oldscheduling;

import java.time.Duration;

import app.packed.concurrent.job2.impl.ScheduledOperationHandle;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;
import internal.app.packed.concurrent.old.ScheduleImpl;

/**
 *
 */
// Maybe we only have 1 schedule per operation...
// I actually think so
// Alternative we introduce a CompositeSchedule
public final class ScheduledOperationConfiguration extends OperationConfiguration {

    final ScheduledOperationHandle handle;

    /**
     * @param handle
     */
    public ScheduledOperationConfiguration(OperationHandle<?> handle) {
        super(handle);
        this.handle = (ScheduledOperationHandle) handle;
    }

    public boolean isScheduled() {
        return handle.s != null;
    }

    public void setMillies(int millies) {
        checkIsConfigurable();
        handle.s = new ScheduleImpl(Duration.ofMillis(millies));
    }

}
