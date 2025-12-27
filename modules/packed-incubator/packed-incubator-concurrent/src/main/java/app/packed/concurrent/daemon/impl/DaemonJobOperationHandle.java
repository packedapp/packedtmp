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
package app.packed.concurrent.daemon.impl;

import java.util.concurrent.ThreadFactory;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.sidebean.SidebeanConfiguration;
import app.packed.concurrent.ThreadKind;
import app.packed.concurrent.daemon.DaemonJob;
import app.packed.concurrent.daemon.DaemonJobConfiguration;
import app.packed.concurrent.daemon.DaemonJobContext;
import app.packed.concurrent.daemon.DaemonJobMirror;
import app.packed.concurrent.daemon.impl.DaemonJobSideBean.DaemonOperationInvoker;
import app.packed.context.ContextTemplate;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import internal.app.packed.concurrent.ThreadNamespaceHandle;
import internal.app.packed.concurrent.ThreadedOperationHandle;

/** An operation handle for a daemon operation. */
public final class DaemonJobOperationHandle extends ThreadedOperationHandle<DaemonJobConfiguration> {

    /** A context template for a daemon job. */
    private static final ContextTemplate CONTEXT_TEMPLATE = ContextTemplate.of(DaemonJobContext.class);

    /** An operation template for a daemon job. */
    private static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withContext(CONTEXT_TEMPLATE).withReturnIgnore();

    public boolean interruptOnStop;

    public boolean restart;

    public ThreadFactory threadFactory;

    public ThreadKind threadKind;

    DaemonJobOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace, DaemonJob annotation) {
        super(installer, namespace);
        this.threadKind = annotation.threadKind();
        this.interruptOnStop = annotation.interruptOnStop();
    }

    /** {@inheritDoc} */
    @Override
    protected DaemonJobConfiguration newOperationConfiguration() {
        return new DaemonJobConfiguration(this);
    }

    /** {@inheritDoc} */
    @Override
    public DaemonJobMirror newOperationMirror() {
        return new DaemonJobMirror(this);
    }

    public ThreadFactory runtimeThreadFactory() {
        ThreadFactory tf = threadFactory;
        if (tf == null) {
            tf = switch (threadKind) {
            case DAEMON_THREAD -> Thread.ofPlatform().daemon().factory();
            case PLATFORM_THREAD -> Thread.ofPlatform().factory();
            case VIRTUAL_THREAD -> Thread.ofVirtual().factory();
            };
        }
        return tf;
    }

    public static void installFromAnnotation(BeanIntrospector<BaseExtension> introspector, BeanIntrospector.OnMethod method, DaemonJob annotation) {
        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(introspector.extensionHandle());

        // Create a new operation
        DaemonJobOperationHandle handle = method.newOperation(OPERATION_TEMPLATE).install(namespace, (i, n) -> new DaemonJobOperationHandle(i, n, annotation));

        // Lazy install the sidebean
        SidebeanConfiguration<DaemonJobSideBean> sideBean = introspector.base().installSidebeanIfAbsent(DaemonJobSideBean.class, c -> {
            c.attachmentBindInvoker(DaemonOperationInvoker.class);
            c.attachmentBindConstant(ThreadFactory.class, Thread.ofPlatform().daemon().factory());
        });

        // Create a new attachment
        sideBean.attachToOperation(handle);

       // useSite.bindBuildConstant(ThreadFactory.class, Thread.ofPlatform().daemon().factory());
    }
}
