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
package app.packed.bean.operation.usage;

import java.util.List;

import app.packed.bean.operation.OperationMirror;

/**
 *
 */

// Hvis vi har noget dataflow aktie ting... Fungere den maaske ikke

public abstract class ScheduleTaskMirror extends OperationMirror {

    /** {@return the key that the service is exported with.} */
    public abstract Object schedule();

    /// Hmm, Hmm, Hmm. Det er jo det her graf ting...
    public abstract List<ScheduleTaskMirror> subtasks();
}
