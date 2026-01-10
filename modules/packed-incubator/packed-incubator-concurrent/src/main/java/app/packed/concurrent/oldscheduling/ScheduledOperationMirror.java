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
package app.packed.concurrent.oldscheduling;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import app.packed.concurrent.job2.impl.ScheduledOperationHandle;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * An mirror for a scheduled operation.
 */
public class ScheduledOperationMirror extends OperationMirror {

    final ScheduledOperationHandle handle;

    /**
     * @param handle
     */
    public ScheduledOperationMirror(OperationHandle<?> handle) {
        super(handle);
        this.handle = (ScheduledOperationHandle) requireNonNull(handle);
    }

    public String schedule() {
        return Objects.toString(handle.s);
    }
}
