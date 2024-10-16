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

import app.packed.operation.OperationInstaller;

/** An operation handle for a daemon operation. */
final class DaemonOperationHandle extends ThreadedOperationHandle<DaemonOperationConfiguration> {

    boolean interruptOnStop;

    boolean restart;

    boolean useVirtual;

    DaemonOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace) {
        super(installer, namespace);
    }

    @Override
    protected DaemonOperationConfiguration newOperationConfiguration() {
        return new DaemonOperationConfiguration(this);
    }

    @Override
    public DaemonOperationMirror newOperationMirror() {
        return new DaemonOperationMirror(this);
    }
}
