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

import java.util.List;

import app.packed.bean.BeanConfiguration;
import app.packed.build.BuildActor;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import app.packed.namespace.NamespaceTemplate;
import internal.app.packed.concurrent.ExecutorConfiguration;
import internal.app.packed.concurrent.ScheduledDaemon;
import internal.app.packed.concurrent.ScheduledOperation;

/**
 * A namespace for the thread management in Packed.
 */
final class ThreadNamespaceHandle extends NamespaceHandle<ThreadExtension, ThreadNamespaceConfiguration> {

    /** The default thread namespace template. */
    static final NamespaceTemplate TEMPLATE = NamespaceTemplate.of(ThreadNamespaceHandle.class, c -> {});

    ExecutorConfiguration scheduler;

    /**
     * Create a new handle.
     *
     * @param installer
     *            the namespace installer
     */
    protected ThreadNamespaceHandle(NamespaceInstaller installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceConfiguration newNamespaceConfiguration(ThreadExtension e, BuildActor actor) {
        return new ThreadNamespaceConfiguration(this, e, actor);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceMirror newNamespaceMirror() {
        return new ThreadNamespaceMirror(this);
    }

    @Override
    protected void onNamespaceClose() {
        // Get all t
        List<ScheduledOperationHandle> l = operations(ScheduledOperationHandle.class).toList();
        List<DaemonOperationHandle> daemons = operations(DaemonOperationHandle.class).toList();

        if (l.size() > 0) {
            BeanConfiguration b = rootExtension().initSchedulingBean();

            b.bindCodeGenerator(ScheduledOperation[].class,
                    () -> operations(ScheduledOperationHandle.class).map(ScheduledOperationHandle::schedule).toArray(ScheduledOperation[]::new));

            b.bindCodeGenerator(ScheduledDaemon[].class,
                    () -> operations(DaemonOperationHandle.class).map(DaemonOperationHandle::schedule).toArray(ScheduledDaemon[]::new));
        }
        System.out.println("CLosing " + daemons.size());
    }
}
