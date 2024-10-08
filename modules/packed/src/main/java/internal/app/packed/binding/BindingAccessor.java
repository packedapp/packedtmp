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

import internal.app.packed.lifecycle.lifetime.RegionalLifetimeSetup;
import internal.app.packed.operation.OperationSetup;
import sandbox.operation.mirror.BindingProviderKind;

/** How do I access Provides values for bindings. */
public sealed interface BindingAccessor {

    /** {@return the kind of provider.} */
    BindingProviderKind kind();

    sealed interface SupplierOrInstance extends BindingAccessor {}

    /** This provider will create a constant at code generation time. */
    public record FromCodeGenerated(Supplier<?> supplier, SuppliedBindingKind suppliedBindingkind) implements SupplierOrInstance {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.CONSTANT;
        }
    }

    /** Provides values from a constant. */
    public record FromConstant(Class<?> constantType, Object constant) implements SupplierOrInstance {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.CONSTANT;
        }
    }

    /** Provides values from an operation invocation argument. */
    public record FromInvocationArgument(int argumentIndex) implements BindingAccessor {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.ARGUMENT;
        }
    }

    /** Provides values by accessing a lifetime arena. */
    public record FromLifetimeArena(RegionalLifetimeSetup containerLifetime, int index, Class<?> type) implements BindingAccessor {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION_RESULT;
        }
    }

    /** Provides values from the result of an operation. */
    public record FromOperationResult(OperationSetup operation) implements BindingAccessor {

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION_RESULT;
        }
    }
}
