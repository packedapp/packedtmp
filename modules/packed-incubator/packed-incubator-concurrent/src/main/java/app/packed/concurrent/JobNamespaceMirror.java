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

import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceMirror;

/**
 * A mirror for a job namespace. All jobs within a
 *
 * @see JobNamespaceConfiguration
 */
public class JobNamespaceMirror extends NamespaceMirror<BaseExtension> {

    /**
     * @param handle
     */
    public JobNamespaceMirror(NamespaceHandle<BaseExtension, ?> handle) {
        super(handle);
    }

    /** {@return a stream of all jobs that have been defined in the namespace or descendant namespaces} */
    public Stream<DaemonJobMirror> allDaemons() {
        return allJobs().filter(DaemonJobMirror.class::isInstance).map(DaemonJobMirror.class::cast);
    }

    /** {@return a stream of all jobs that have been defined in the namespace or descendant namespaces} */
    public Stream<JobMirror> allJobs() {
        return Stream.of();
    }

    /** {@return a stream of all jobs that have been defined in the namespace} */
    public Stream<DaemonJobMirror> daemons() {
        return jobs().filter(DaemonJobMirror.class::isInstance).map(DaemonJobMirror.class::cast);
    }

    /** {@return a stream of all jobs that have been defined in the namespace} */
    public Stream<JobMirror> jobs() {
        return Stream.of();
    }

    /** {@return the owner of the namespace} */
    public ComponentRealm owner() {
        return ComponentRealm.application();
    }
}
