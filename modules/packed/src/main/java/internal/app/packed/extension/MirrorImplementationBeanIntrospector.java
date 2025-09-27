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
package internal.app.packed.extension;

import app.packed.application.ApplicationMirror;
import app.packed.assembly.AssemblyMirror;
import app.packed.bean.BeanMirror;
import app.packed.binding.Key;
import app.packed.build.Mirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.BaseExtension;
import app.packed.operation.OperationMirror;
import internal.app.packed.application.deployment.DeploymentMirror;
import internal.app.packed.bean.scanning.IntrospectorOnVariableUnwrapped;
import internal.app.packed.operation.OperationSetup;

/**
 * Handles the various base {@link app.packed.build.Mirror} classes.
 */
public final class MirrorImplementationBeanIntrospector extends InternalBeanIntrospector<BaseExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        Class<?> baseMirrorClass = service.baseClass();
        OnVariableUnwrapped binding = service.binder();

        OperationSetup operation = ((IntrospectorOnVariableUnwrapped) binding).var().operation;
        if (baseMirrorClass == DeploymentMirror.class) {
            onExtensionService0(service, operation.bean.container.application.deployment.mirror());
        } else if (baseMirrorClass == ApplicationMirror.class) {
            onExtensionService0(service, operation.bean.container.application.mirror());
        } else if (baseMirrorClass == ContainerMirror.class) {
            onExtensionService0(service, operation.bean.container.mirror());
        } else if (baseMirrorClass == AssemblyMirror.class) {
            onExtensionService0(service, operation.bean.container.assembly.mirror());
        } else if (baseMirrorClass == BeanMirror.class) {
            onExtensionService0(service, operation.bean.mirror());
        } else if (baseMirrorClass == OperationMirror.class) {
            onExtensionService0(service, operation.mirror());
        } else {
            super.onExtensionService(key, service);
        }
    }

    private void onExtensionService0(OnContextService service, Mirror mirror) {
        OnVariableUnwrapped binding = service.binder();
        binding.bindInstance(mirror);
    }
}
