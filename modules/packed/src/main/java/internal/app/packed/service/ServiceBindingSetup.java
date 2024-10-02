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

import app.packed.binding.Key;
import app.packed.binding.BindingKind;
import app.packed.service.advanced.ServiceProviderKind;
import app.packed.service.advanced.ServiceResolver;
import app.packed.service.mirror.NamespaceServiceBindingMirror;
import app.packed.util.Nullable;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.binding.BindingSetup;
import internal.app.packed.binding.PackedBindingHandle;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.service.ServiceProviderSetup.BeanServiceProviderSetup;
import internal.app.packed.service.ServiceProviderSetup.ContextServiceProviderSetup;
import internal.app.packed.service.ServiceProviderSetup.NamespaceServiceProviderSetup;
import internal.app.packed.service.ServiceProviderSetup.OperationServiceProviderSetup;

/** Represents a binding to service (which may not exist.). */
public final class ServiceBindingSetup extends BindingSetup {

    /** Whether or not the binding is required. */
    public final boolean isRequired;

    public final Key<?> key;

    @Nullable
    public ServiceProviderSetup resolvedProvider;

    /** How the service binding is resolved. */
    final ServiceResolver resolver;

    /**
     * @param beanOperation
     * @param index
     */
    public ServiceBindingSetup(Key<?> key, OperationSetup operation, int index, boolean isRequired, ServiceResolver resolver) {
        super(operation, index, operation.installedByExtension.authority());
        this.key = key;
        this.isRequired = isRequired;
        this.resolver = requireNonNull(resolver);
        this.mirrorSupplier = () -> new NamespaceServiceBindingMirror(new PackedBindingHandle(this), this);
    }

    /** {@return whether or not the service could be resolved.} */
    public boolean isResolved() {
        return resolvedProvider != null;
    }

    /** {@inheritDoc} */
    @Override
    public BindingKind kind() {
        return BindingKind.SERVICE;
    }

    public ServiceProviderSetup resolve() {
        resolvedProvider = resolve0();
        return resolvedProvider;
    }

    // We will do one method that is just default, then we don't need to run through order, ect
    private ServiceProviderSetup resolve0() {
        for (ServiceProviderKind s : resolver.order()) {
            switch (s) {
            case OPERATION -> {
                OperationServiceProviderSetup sps = operation.serviceProviders.get(key);
                if (sps != null) {
                    return sps;
                }
            }
            case BEAN -> {
                BeanServiceProviderSetup sps = operation.bean.serviceProviders.get(key);
                if (sps != null) {
                    return sps;
                }
            }
            case CONTEXT -> {
                // I think two options -> If empty we must be unique
                // If non-empty we take them 1 at a time

                // We need to look in all contexts that the operation is in
                // If any of the context have matching keys we need to do something about
                ContextServiceProviderSetup sps = null; // Should initialize to first value
                for (ContextSetup c : operation.contexts.values()) {
                    ContextServiceProviderSetup cs = c.serviceProvides.get(key);
                    if (sps != null) {
                        throw new Error();
                    }
                    sps = cs;
                }
                if (sps != null) {
                    return sps;
                }
            }
            case NAMESPACE -> {
                for (@SuppressWarnings("unused")
                String namespace : resolver.namespaces()) {
                    // We only use main for now
                    MainServiceNamespaceHandle h = operation.bean.container.servicesMain();
                    NamespaceServiceProviderSetup sps = h.provider(key);
                    if (sps != null) {
                        return sps;
                    }
                }
            }
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BindingAccessor resolver() {
        return resolvedProvider == null ? null : resolvedProvider.binding();
    }
}
