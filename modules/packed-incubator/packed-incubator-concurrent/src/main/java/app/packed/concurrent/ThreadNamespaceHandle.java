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
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.namespace.NamespaceTemplate.Installer;
import internal.app.packed.concurrent.ExecutorConfiguration;
import internal.app.packed.concurrent.ScheduledDaemon;
import internal.app.packed.concurrent.ScheduledOperation;

/**
 *
 */
final class ThreadNamespaceHandle extends NamespaceHandle<ThreadExtension, ThreadNamespaceConfiguration> {

    /** The default namespace template. */
    static final NamespaceTemplate TEMPLATE = NamespaceTemplate.of(ThreadNamespaceHandle.class, c -> {});

    ExecutorConfiguration scheduler;

    /**
     * @param installer
     */
    protected ThreadNamespaceHandle(Installer installer) {
        super(installer);
    }

    /** {@inheritDoc} */
    @Override
    protected ThreadNamespaceConfiguration newNamespaceConfiguration(ThreadExtension e) {
        return new ThreadNamespaceConfiguration(this);
    }

    @Override
    protected ThreadNamespaceMirror newNamespaceMirror() {
        return new ThreadNamespaceMirror(this);
    }

    @Override
    protected void onNamespaceClose() {
        List<ScheduledOperationHandle> l = operations(ScheduledOperationHandle.class).toList();
        if (l.size() > 0) {
            BeanConfiguration b = rootExtension().initSchedulingBean();

            b.bindCodeGenerator(ScheduledOperation[].class, () -> {
                return operations(ScheduledOperationHandle.class).map(ScheduledOperationHandle::schedule).toArray(e -> new ScheduledOperation[e]);
            });
            b.bindCodeGenerator(ScheduledDaemon[].class, () -> {
                return operations(DaemonOperationHandle.class).map(DaemonOperationHandle::schedule).toArray(e -> new ScheduledDaemon[e]);
            });

        }
        System.out.println("CLosing ");
    }

}
