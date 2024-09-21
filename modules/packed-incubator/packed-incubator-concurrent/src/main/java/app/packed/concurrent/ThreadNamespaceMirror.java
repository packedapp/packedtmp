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

import java.util.stream.Stream;

import app.packed.namespace.NamespaceMirror;

/**
 *
 */
// Ideen er at kunne forstå
public class ThreadNamespaceMirror extends NamespaceMirror<ThreadExtension> {

    /**
     * @param handle
     */
    ThreadNamespaceMirror(ThreadNamespaceHandle handle) {
        super(handle);
    }

    // A stream of all scheduled operations in the namespace
    public Stream<ScheduledOperationMirror> scheduledOperations() {
        return operations(ScheduledOperationMirror.class);
    }

    // A stream of all daemon operations in the namespace
    public Stream<DaemonOperationMirror> daemonOperations() {
        return operations(DaemonOperationMirror.class);
    }
}
