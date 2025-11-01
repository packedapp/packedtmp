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
package internal.app.packed.binding;

import java.util.function.Supplier;

import internal.app.packed.lifecycle.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifecycle.lifetime.LifetimeStoreIndex;
import internal.app.packed.operation.OperationSetup;
import sandbox.operation.mirror.BindingProviderKind;

/** Describes how the value of a binding is provided. */
public sealed interface BindingProvider {

    /** {@return the kind of provider.} */
    BindingProviderKind kind();

    sealed interface SupplierOrInstance extends BindingProvider {}

    /** Provides an instance from a constant that is created at code generation time. */
    public record FromCodeGeneratedConstant(Supplier<?> supplier, SuppliedBindingKind suppliedBindingkind) implements SupplierOrInstance {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.CONSTANT;
        }
    }

    /** Provide instance from a constant. */
    public record FromConstant(Class<?> constantType, Object constant) implements SupplierOrInstance {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.CONSTANT;
        }
    }

    /** Provides a instance from the invocation argument of operation. */
    public record FromInvocationArgument(int argumentIndex) implements BindingProvider {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.ARGUMENT;
        }
    }

    /** Provides an instance by accessing a lifetime arena. For example, a singleton bean. */
    public record FromLifetimeArena(ContainerLifetimeSetup containerLifetime, LifetimeStoreIndex index, Class<?> type) implements BindingProvider {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION_RESULT;
        }
    }

    public record FromSidebeanLifetimeArena(Class<?> type) implements BindingProvider {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION_RESULT;
        }
    }

    /** Provides an instance from the result of an operation. */
    public record FromOperationResult(OperationSetup operation) implements BindingProvider {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION_RESULT;
        }
    }
}
