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
package app.packed.concurrent.job;

import app.packed.operation.OperationHandle;
import app.packed.operation.OperationMirror;

/**
 * Like all other mirrors, a job mirror only represents jobs that are defined at build time.
 * Jobs that are submitted at runtime can only be tracked through a job tracker.
 */
public abstract class JobMirror extends OperationMirror {

    OperationHandle<?> handle;

    /**
     * @param handle
     */
    JobMirror(OperationHandle<?> handle) {
        super(handle);
    }


    /** { @return the thread namespace this daemon is a part of} */
    public JobNamespaceMirror namespace() {
        throw new UnsupportedOperationException();
   //     return (JobNamespaceMirror) handle.namespace.mirror();
    }
}
