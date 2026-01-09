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
import app.packed.bean.SidebeanConfiguration;
import app.packed.bean.SidebeanTargetKind;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobConfiguration;
import app.packed.concurrent.DaemonJobContext;
import app.packed.concurrent.DaemonJobMirror;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationInstaller;
import internal.app.packed.concurrent.AbstractJobOperationHandle;
import internal.app.packed.concurrent.ThreadNamespaceHandle;
import internal.app.packed.concurrent.daemon.DaemonJobSidebean.DaemonOperationInvoker;

/** An operation handle for a daemon operation. */
public final class DaemonJobOperationHandle extends AbstractJobOperationHandle<DaemonJobConfiguration> {

    public boolean restart;

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

    public static void onDaemonJobAnnotation(BeanIntrospector<BaseExtension> introspector, BeanIntrospector.OnMethod method, DaemonJob annotation) {
        // Lazy install the sidebean and runtime manager
       // SidebeanInstallationConfig.operation().withBindConstant(ThreadFactory.class).withInvoker(DaemonOperationInvoker.class);
        SidebeanConfiguration<DaemonJobSidebean> sideBean = introspector.applicationBase().installSidebeanIfAbsent(DaemonJobSidebean.class, SidebeanTargetKind.OPERATION, c -> {
            c.sidebeanOperationInvoker(DaemonOperationInvoker.class);
            c.sidebeanBindConstant(ThreadFactory.class);
            introspector.applicationBase().install(DaemonJobRuntimeManager.class).provide();
        });

        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(introspector.extensionHandle());

        DaemonJobOperationHandle handle = method.newOperation().addContext(DaemonJobContext.class).attachToSidebean(sideBean).install(namespace,
                DaemonJobOperationHandle::new);
        handle.threadKind = annotation.threadKind();
        handle.interruptOnStop = annotation.interruptOnStop();
    }
}
