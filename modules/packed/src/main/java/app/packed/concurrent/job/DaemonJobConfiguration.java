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

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ThreadFactory;

import app.packed.concurrent.ThreadKind;
import app.packed.operation.OperationHandle;
import internal.app.packed.concurrent.daemon.DaemonOperationHandle;

/**
 * The configuration of a daemon operation.
 *
 * @see Daemon
 * @see DaemonOperationMirror
 */
// Missing Error Handling
public final class DaemonJobConfiguration extends JobConfiguration {

    /** The daemon handle. */
    private final DaemonOperationHandle handle;

    /**
     * @param handle
     */
    public DaemonJobConfiguration(OperationHandle<?> handle) {
        super(handle);
        this.handle = (DaemonOperationHandle) handle;
    }

    public DaemonJobConfiguration interruptOnStop(boolean value) {
        checkIsConfigurable();
        handle.interruptOnStop = value;
        return this;
    }

    // error handling...
    public DaemonJobConfiguration restart(boolean restart) {
        checkIsConfigurable();
        handle.restart = restart;
        return this;
    }

    /**
     * <p>
     * A {@link #threadFactory(ThreadFactory)} will always take precedens over any thread kind by this method.
     *
     * @param threadKind
     * @return
     */
    public DaemonJobConfiguration threadKind(ThreadKind threadKind) {
        checkIsConfigurable();
        handle.threadKind = requireNonNull(threadKind);
        return this;
    }

    /**
     * Sets the factory to use when creating the daemon thread
     * <p>
     * For consistent reporting in the mirror api {@link DaemonMirror#threadKind()}, {@link #threadKind(ThreadKind)} should
     * be set to type of threads created by the specified threadFactory.
     *
     * @param threadFactory
     *            the factory to use when creating the daemon thread
     * @return this configuration
     */
    //
    public DaemonJobConfiguration threadFactory(ThreadFactory threadFactory) {
        checkIsConfigurable();
        handle.threadFactory = requireNonNull(threadFactory);
        return this;
    }
}
