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
public sealed interface ServiceProviderSetup extends Keyed {

    BindingAccessor binding();

    public static final class BeanServiceProviderSetup implements ServiceProviderSetup {
        private final SupplierOrInstance binding;
        /** All bindings that uses this provider. */
        public final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

        private final Key<?> key;

        public BeanServiceProviderSetup(Key<?> key, SupplierOrInstance binding) {
            this.key = key;
            this.binding = requireNonNull(binding);
        }

        @Override
        public BindingAccessor binding() {
            return binding;
        }

        public SupplierOrInstance bindingInstance() {
            return binding;
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }
    }

    public static final class ContextServiceProviderSetup implements ServiceProviderSetup {
        private final BindingAccessor binding;
        private final ContextSetup context;
        /** All bindings that uses this provider. */
        public final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

        private final Key<?> key;

        public ContextServiceProviderSetup(Key<?> key, ContextSetup context, BindingAccessor binding) {
            this.key = key;
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

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }
    }

    // extends OperationHandle
    public static final class NamespaceServiceProviderHandle implements ServiceProviderSetup {

        private final BindingAccessor binding;

        /** All bindings that uses this provider. */
        public final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

        private final Key<?> key;

        /** Used for checking for dependency cycles. */
        public boolean hasBeenCheckForDependencyCycles;

        private final ServiceNamespaceHandle namespace;

        private final OperationSetup operation;

        public NamespaceServiceProviderHandle(Key<?> key, ServiceNamespaceHandle namespace, OperationSetup operation, BindingAccessor binding) {
            this.key = key;
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

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }
    }

    /**
     * A service that was bound on the {@link app.packed.service.advanced.ServiceProviderKind#OPERATION} level.
     *
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(Class, Object)
     * @see app.packed.operation.OperationConfiguration#bindServiceInstance(Key, Object)
     */
    public static final class OperationServiceProviderSetup implements ServiceProviderSetup {
        /** All bindings that uses this provider. */
        public final ArrayList<ServiceBindingSetup> bindings = new ArrayList<>();

        private final Key<?> key;

        private final SupplierOrInstance binding;

        private final OperationSetup operation;

        public OperationServiceProviderSetup(Key<?> key, OperationSetup operation, SupplierOrInstance binding) {
            this.key = key;
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

        /** {@inheritDoc} */
        @Override
        public Key<?> key() {
            return key;
        }
    }
}
