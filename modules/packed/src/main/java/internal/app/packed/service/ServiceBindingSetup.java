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
package internal.app.packed.service;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.bindings.mirror.BindingClassifierKind;
import app.packed.container.Realm;
import app.packed.framework.Nullable;
import app.packed.service.ServiceBindingMirror;
import app.packed.service.ServiceExtension;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.BindingSetup;

/**
 * A binding to a service.
 */
// ContextService or ContainerService
//// Vi kan ikke bestemme hvad foerened til sidst...
//// fx initialize with er jo efter bean introspection

public final class ServiceBindingSetup extends BindingSetup {

    /** An entry corresponding to the key. */
    public final ServiceManagerEntry entry;

    /** A binding in the same container for the same key */
    @Nullable
    public ServiceBindingSetup nextFriend;

    /** Whether or not the binding is required. */
    public final boolean required;

    /**
     * @param beanOperation
     * @param index
     */
    ServiceBindingSetup(OperationSetup operation, int index, ServiceManagerEntry entry, boolean required) {
        super(operation, index, Realm.extension(ServiceExtension.class));
        this.entry = entry;
        this.required = required;
        mirrorSupplier = () -> new ServiceBindingMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
        MethodHandle mh = entry.provider.resolution.provideSpecial();
        return MethodHandles.collectArguments(methodHandle, index, mh);
    }

    /** {@return whether or not the service could be resolved.} */
    public boolean isResolved() {
        return entry.provider != null;
    }

    public BindingClassifierKind kind() {
        return BindingClassifierKind.KEY;
    }
}
