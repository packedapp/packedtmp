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
package app.packed.concurrent.job.impl;

import app.packed.concurrent.ThreadNamespaceMirror;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;
import internal.app.packed.concurrent.ThreadedOperationHandle;

/**
 *
 */
// Doesn't really make sense OnStart can be run in its own thread, but is not mandatory
public abstract class ThreadOperationMirror extends OperationMirror {

    final ThreadedOperationHandle<?> handle;

    /**
     * @param handle
     */
    ThreadOperationMirror(OperationHandle<?> handle) {
        super(handle);
        this.handle = (ThreadedOperationHandle<?>) handle;
    }

    public final ThreadNamespaceMirror namespace() {
        return (ThreadNamespaceMirror) handle.namespace.mirror();
    }
}
