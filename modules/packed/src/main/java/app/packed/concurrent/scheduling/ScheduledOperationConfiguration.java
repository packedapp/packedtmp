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

import java.time.Duration;

import app.packed.concurrent.scheduling.SchedulingExtension.FinalSchedule;
import app.packed.framework.Nullable;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationHandle;

/**
 *
 */
// Maybe we only have 1 schedule per operation...
// I actually think so
public final class ScheduledOperationConfiguration extends OperationConfiguration {

    @Nullable
    private Schedule s;

    /**
     * @param handle
     */
    ScheduledOperationConfiguration(@Nullable Schedule is, OperationHandle handle) {
        super(handle);
        this.s = is;
    }

    public boolean isScheduled() {
        return s != null;
    }

    FinalSchedule schedule() {
        if (s == null) {
            throw new IllegalStateException("Operation " + handle() + " was never scheduled");
        }
        return new FinalSchedule(s, handle().generateMethodHandle());
    }

    public void setMillies(int millies) {
        checkConfigurable();
        s = new Schedule(Duration.ofMillis(millies));
    }

    record Schedule(Duration d) {

    }
}
