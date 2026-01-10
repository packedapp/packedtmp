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

import app.packed.bean.BeanIntrospector;
import app.packed.bean.SidehandleBeanConfiguration;
import app.packed.bean.SidehandleTargetKind;
import app.packed.concurrent.DaemonJob;
import app.packed.concurrent.DaemonJobConfiguration;
import app.packed.concurrent.DaemonJobContext;
import app.packed.concurrent.DaemonJobMirror;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationInstaller;
import internal.app.packed.concurrent.AbstractJobOperationHandle;
import internal.app.packed.concurrent.ThreadNamespaceHandle;

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
        // Lazy install the sidehandle and runtime manager
        SidehandleBeanConfiguration<DaemonJobSidehandle> sideBean = introspector.applicationBase().installSidebeanIfAbsent(DaemonJobSidehandle.class, SidehandleTargetKind.OPERATION, _ -> {
            introspector.applicationBase().install(DaemonJobRuntimeManager.class).provide();
        });

        ThreadNamespaceHandle namespace = ThreadNamespaceHandle.mainHandle(introspector.extensionHandle());

        DaemonJobOperationHandle handle = method.newOperation().addContext(DaemonJobContext.class).attachToSidebean(sideBean).install(namespace,
                DaemonJobOperationHandle::new);
        handle.threadKind = annotation.threadKind();
        handle.interruptOnStop = annotation.interruptOnStop();
    }
}
