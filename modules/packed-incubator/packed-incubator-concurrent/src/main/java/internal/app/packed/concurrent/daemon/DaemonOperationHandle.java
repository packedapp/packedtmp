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
package internal.app.packed.concurrent.daemon;

import java.util.concurrent.ThreadFactory;

import app.packed.bean.scanning.BeanIntrospector;
import app.packed.bean.sidebean.SideBeanConfiguration;
import app.packed.bean.sidebean.SideBeanUseSite;
import app.packed.concurrent.ThreadKind;
import app.packed.concurrent.job.DaemonJob;
import app.packed.concurrent.job.DaemonJobConfiguration;
import app.packed.concurrent.job.DaemonJobContext;
import app.packed.concurrent.job.DaemonJobMirror;
import app.packed.context.ContextTemplate;
import app.packed.extension.BaseExtension;
import app.packed.extension.ExtensionHandle;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import internal.app.packed.concurrent.ThreadNamespaceHandle;
import internal.app.packed.concurrent.ThreadedOperationHandle;

/** An operation handle for a daemon operation. */
public final class DaemonOperationHandle extends ThreadedOperationHandle<DaemonJobConfiguration> {

    /** A context template for a daemon job. */
    private static final ContextTemplate CONTEXT_TEMPLATE = ContextTemplate.of(DaemonJobContext.class);

    /** An operation template for a daemon job. */
    private static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withContext(CONTEXT_TEMPLATE).withReturnIgnore();

    public boolean interruptOnStop;

    public boolean restart;

    public ThreadFactory threadFactory;

    public ThreadKind threadKind;

    DaemonOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace, DaemonJob annotation) {
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
        ExtensionHandle<BaseExtension> extension = introspector.extensionHandle();
        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(extension);

        // Create a new operation
        OperationInstaller installer = method.newOperation(OPERATION_TEMPLATE);
        DaemonOperationHandle handle = installer.install(namespace, (i, n) -> new DaemonOperationHandle(i, n, annotation));

        //handle.lazySidebean(DaemonSideBean.class).addToOperation(handle)

        // Ahh vi kan jo ikke installere direkte til os selv fordi vi er BaseExtension
        SideBeanConfiguration<DaemonSideBean> sideBean = extension.applicationRoot().installSidebeanIfAbsent(DaemonSideBean.class,
                c -> c.operationInvoker(DaemonSideBean.DaemonOperationInvoker.class));
        SideBeanUseSite useSite = sideBean.addToOperation(handle);

        System.out.println(useSite.toString());
    }
}
