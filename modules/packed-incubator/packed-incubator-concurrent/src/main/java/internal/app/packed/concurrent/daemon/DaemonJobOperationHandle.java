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

import app.packed.bean.BeanIntrospector;
import app.packed.bean.sidebean.SidebeanConfiguration;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobConfiguration;
import app.packed.concurrent.DaemonJobContext;
import app.packed.concurrent.DaemonJobMirror;
import app.packed.concurrent.ThreadKind;
import app.packed.context.ContextTemplate;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationInstaller;
import app.packed.operation.OperationTemplate;
import internal.app.packed.concurrent.ThreadNamespaceHandle;
import internal.app.packed.concurrent.ThreadedOperationHandle;

/** An operation handle for a daemon operation. */
public final class DaemonJobOperationHandle extends ThreadedOperationHandle<DaemonJobConfiguration> {

    /** An operation template for a daemon job. */
    private static final OperationTemplate OPERATION_TEMPLATE = OperationTemplate.defaults().withContext(ContextTemplate.of(DaemonJobContext.class))
            .withReturnIgnore();

    public boolean interruptOnStop;

    public boolean restart;

    public ThreadFactory threadFactory;

    public ThreadKind threadKind;

    private DaemonJobOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace) {
        super(installer, namespace);
    }

    /** {@inheritDoc} */
    @Override
    protected DaemonJobConfiguration newOperationConfiguration() {
        return new DaemonJobConfiguration(this);
    }

    /** {@inheritDoc} */
    @Override
    protected DaemonJobMirror newOperationMirror() {
        return new DaemonJobMirror(this);
    }

    @Override
    protected void onConfigured() {
        ThreadFactory tf = threadFactory;
        if (tf == null) {
            tf = switch (threadKind) {
            case DAEMON_THREAD -> Thread.ofPlatform().daemon().factory();
            case PLATFORM_THREAD -> Thread.ofPlatform().factory();
            case VIRTUAL_THREAD -> Thread.ofVirtual().factory();
            };
        }

       sidebeanAttachment().bindConstant(ThreadFactory.class, tf);
    }

    public static void onDaemonJobAnnotation(BeanIntrospector<BaseExtension> introspector, BeanIntrospector.OnMethod method, DaemonJob annotation) {
        // Lazy install the sidebean
        SidebeanConfiguration<DaemonJobSidebean> sideBean = introspector.base().installSidebeanIfAbsent(DaemonJobSidebean.class, c -> {
            c.sidebeanBindInvoker(DaemonInvoker.class);
            c.sidebeanBindConstant(ThreadFactory.class);
        });

        introspector.base().install(DaemonJobRuntimeManager.class).provide();
        introspector.base().install(HowDoesThisWork.class).provide();
        introspector.base().install(HowDoesThisWorkWithParam.class).provide();

        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(introspector.extensionHandle());

        DaemonJobOperationHandle handle = method.newOperation(OPERATION_TEMPLATE).attachToSidebean(sideBean).install(namespace, DaemonJobOperationHandle::new);
        handle.threadKind = annotation.threadKind();
        handle.interruptOnStop = annotation.interruptOnStop();
    }
}
