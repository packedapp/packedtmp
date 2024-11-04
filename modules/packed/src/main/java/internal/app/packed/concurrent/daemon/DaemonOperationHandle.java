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

import app.packed.bean.scanning.BeanIntrospector.OnMethod;
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

    public static final ContextTemplate DAEMON_CONTEXT_TEMPLATE = ContextTemplate.of(DaemonJobContext.class);

    public static final OperationTemplate DAEMON_OPERATION_TEMPLATE = OperationTemplate.defaults().withContext(DAEMON_CONTEXT_TEMPLATE).withReturnIgnore();

    public boolean interruptOnStop;

    public boolean restart;

    public ThreadFactory threadFactory;

    public ThreadKind threadKind;

    private DaemonOperationHandle(OperationInstaller installer, ThreadNamespaceHandle namespace, DaemonJob annotation) {
        super(installer, namespace);
        threadKind = annotation.threadKind();
        interruptOnStop = annotation.interruptOnStop();
    }

    public DaemonRuntimeConfiguration runtimeConfiguration() {
        return new DaemonRuntimeConfiguration(runtimeThreadFactory(), invokerAsMethodHandle());
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

    /**
     * Handles the {@link Daemon} annotation.
     *
     * @param extensionHandle
     *            an extension handle for {@link BaseExtension}
     * @param method
     *            the annotated method
     * @param annotation
     *            the Daemon annotation
     */
    public static void onAnnotatedMethod(ExtensionHandle<BaseExtension> extensionHandle, OnMethod method, DaemonJob annotation) {
        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(extensionHandle);

        // Install the operation
        method.newOperation(DaemonOperationHandle.DAEMON_OPERATION_TEMPLATE).install(namespace, (i, n) -> new DaemonOperationHandle(i, n, annotation));
    }
}
