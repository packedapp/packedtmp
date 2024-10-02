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

import java.util.ArrayList;

import app.packed.binding.Key;
import internal.app.packed.binding.BindingAccessor;
import internal.app.packed.binding.BindingAccessor.SupplierOrInstance;
import internal.app.packed.binding.Keyed;
import internal.app.packed.context.ContextSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public sealed abstract class ServiceProviderSetup implements Keyed {

    /** All bindings that uses this provider. */
    public final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

    private final Key<?> key;

    protected ServiceProviderSetup(Key<?> key) {
        this.key = key;
    }

    public abstract BindingAccessor binding();

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    public static final class BeanServiceProviderSetup extends ServiceProviderSetup {
        private final SupplierOrInstance binding;

        public BeanServiceProviderSetup(Key<?> key, SupplierOrInstance binding) {
            super(key);
            this.binding = requireNonNull(binding);
        }

        @Override
        public BindingAccessor binding() {
            return binding;
        }

        public SupplierOrInstance bindingInstance() {
            return binding;
        }
    }

    public static final class ContextServiceProviderSetup extends ServiceProviderSetup {
        private final BindingAccessor binding;
        private final ContextSetup context;

        public ContextServiceProviderSetup(Key<?> key, ContextSetup context, BindingAccessor binding) {
            super(key);
            this.context = context;
            this.binding = binding;
        }

        @Override
        public BindingAccessor binding() {
            return binding;
        }

        public ContextSetup context() {
            return context;
        }
    }

    public static final class NamespaceServiceProviderSetup extends ServiceProviderSetup {
        private final BindingAccessor binding;

        /** Used for checking for dependency cycles. */
        public boolean hasBeenCheckForDependencyCycles;

        private final ServiceNamespaceHandle namespace;

        private final OperationSetup operation;

        public NamespaceServiceProviderSetup(Key<?> key, ServiceNamespaceHandle namespace, OperationSetup operation, BindingAccessor binding) {
            super(key);
            this.namespace = namespace;
            this.operation = operation;
            this.binding = binding;
        }

        @Override
        public BindingAccessor binding() {
            return binding;
        }

        public ServiceNamespaceHandle namespace() {
            return namespace;
        }

        public OperationSetup operation() {
            return operation;
        }
    }

    /**
     * A service that was bound on the {@link app.packed.service.advanced.ServiceProviderKind#OPERATION} level.
     *
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(Class, Object)
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(Key, Object)
     */
    public static final class OperationServiceProviderSetup extends ServiceProviderSetup {

        private final SupplierOrInstance binding;

        private final OperationSetup operation;

        public OperationServiceProviderSetup(Key<?> key, OperationSetup operation, SupplierOrInstance binding) {
            super(key);
            this.operation = operation;
            this.binding = binding;
        }

        @Override
        public BindingAccessor binding() {
            // Implement binding logic here
            return null;
        }

        public SupplierOrInstance bindingInstance() {
            return binding;
        }

        public OperationSetup operation() {
            return operation;
        }
    }
}
