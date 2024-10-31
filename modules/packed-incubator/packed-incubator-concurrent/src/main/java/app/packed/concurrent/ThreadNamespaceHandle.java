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

import app.packed.bean.BeanConfiguration;
import app.packed.build.BuildActor;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceInstaller;
import app.packed.namespace.NamespaceTemplate;
import internal.app.packed.concurrent.ExecutorConfiguration;
import internal.app.packed.concurrent.NewScheduledOperation;
import internal.app.packed.concurrent.ScheduledDaemon;
import internal.app.packed.concurrent.ScheduledOperation;

/**
 * A namespace for the thread management in Packed.
 */
final class ThreadNamespaceHandle extends NamespaceHandle<ThreadExtension, ThreadNamespaceConfiguration> {

    /** The default thread namespace template. */
    static final NamespaceTemplate<ThreadNamespaceHandle> TEMPLATE = NamespaceTemplate.of(ThreadNamespaceHandle.class, ThreadNamespaceHandle::new);

    ExecutorConfiguration scheduler;

    /**
     * Create a new handle.
     *
     * @param installer
     *            the namespace installer
     */
    protected ThreadNamespaceHandle(NamespaceInstaller<?> installer) {
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

    /** {@inheritDoc} */
    @Override
    protected void onNamespaceClose() {
        // Find all scheduling operations in the namespace.

        @SuppressWarnings("unused")
        NewScheduledOperation[] newSo = operations(ScheduledOperationHandle.class).map(h -> h.invokerAs(NewScheduledOperation.class, h.s))
                .toArray(NewScheduledOperation[]::new);

        ScheduledOperation[] so = operations(ScheduledOperationHandle.class).map(h -> new ScheduledOperation(h.s, h.invokerAsMethodHandle()))
                .toArray(ScheduledOperation[]::new);

        // Find all daemon operations in the namespace.
        ScheduledDaemon[] sd = operations(DaemonOperationHandle.class).map(h -> new ScheduledDaemon(h.useVirtual, h.invokerAsMethodHandle()))
                .toArray(ScheduledDaemon[]::new);

        // Install a scheduling bean if we have any scheduling or daemon operations.
        if (so.length > 0 || sd.length > 0) {
            BeanConfiguration b = rootExtension().newSchedulingBean();
            b.bindServiceInstance(ScheduledOperation[].class, so);
            b.bindServiceInstance(ScheduledDaemon[].class, sd);
        }
    }
}
