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

import static java.util.Objects.requireNonNull;

import app.packed.bindings.BindingKind;
import app.packed.container.Realm;
import app.packed.extension.BaseExtension;
import app.packed.framework.Nullable;
import app.packed.service.ServiceBindingMirror;
import internal.app.packed.binding.BindingResolution;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.operation.OperationSetup;

/** Represents a binding to service (which may not exist.). */
public final class ServiceBindingSetup extends BindingSetup {

    /** The service manager entry entry corresponding to the key. */
    public final ServiceSetup entry;

    /** A binding in the same service manager for the same key. */
    @Nullable
    ServiceBindingSetup nextBinding;

    /** Whether or not the binding is required. */
    public final boolean isRequired;

    /**
     * @param beanOperation
     * @param index
     */
    ServiceBindingSetup(OperationSetup operation, int index, ServiceSetup entry, boolean isRequired) {
        super(operation, index, Realm.extension(BaseExtension.class));
        this.entry = requireNonNull(entry);
        this.isRequired = isRequired;
        this.mirrorSupplier = () -> new ServiceBindingMirror(this);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BindingResolution resolver() {
        ServiceProviderSetup provider = entry.provider();
        return provider == null ? null : provider.resolution;
    }

    /** {@return whether or not the service could be resolved.} */
    public boolean isResolved() {
        return entry.provider() != null;
    }

    /** {@inheritDoc} */
    @Override
    public BindingKind kind() {
        return BindingKind.SERVICE;
    }
}
