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

import app.packed.concurrent.ThreadKind;
import app.packed.operation.OperationHandle;
import internal.app.packed.concurrent.daemon.DaemonOperationHandle;

/**
 * A mirror for a daemon operation.
 *
 * @see Daemon
 */
public final class DaemonJobMirror extends JobMirror {

    final DaemonOperationHandle handle;

    /**
     * @param handle
     */
    public DaemonJobMirror(OperationHandle<?> handle) {
        super(handle);
        this.handle = (DaemonOperationHandle) handle;
    }

    public boolean hasThreadFactory() {
        return handle.threadFactory != null;
    }

    public boolean isInteruptAtStop() {
        return handle.interruptOnStop;
    }

    /**
     * <p>
     * If a {@link #hasThreadFactory()} has been defined for the daemon. The thread factory will take precedence over any
     * thread kind that has been defined for the daemon. As such the thread kind returned from this method may not match the
     * thread kind the thread factory produces.
     *
     * @return the thread kind of the mirror
     *
     * @see Daemon#threadKind()
     */
    public ThreadKind threadKind() {
        return handle.threadKind;
    }
}
