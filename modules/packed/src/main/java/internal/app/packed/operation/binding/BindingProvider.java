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
package internal.app.packed.operation.binding;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.binding.mirror.BindingProviderKind;
import internal.app.packed.lifetime.ContainerLifetimeSetup;
import internal.app.packed.lifetime.PackedExtensionContext;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.MethodHandleUtil;

/** Provider of a value for a binding. */
public sealed abstract class BindingProvider {

    public abstract MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle);

    /** {@return the kind of provider.} */
    public abstract BindingProviderKind kind();

    public abstract MethodHandle provideSpecial();

    /** Provides values from an operation invocation argument. */
    public static final class FromArgument extends BindingProvider {

        /** The index of the argument to use. */
        public final int argumentIndex;

        /**
         * Create a new argument provider.
         * 
         * @param argumentIndex
         *            the index of the argument to use
         */
        public FromArgument(int argumentIndex) {
            this.argumentIndex = argumentIndex;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.ARGUMENT;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            throw new UnsupportedOperationException();
        }
    }

    /** Provides values from a constant. */
    public static final class FromConstant extends BindingProvider {

        /** The constant. */
        public final Object constant;

        /** The type of the constant. */
        public final Class<?> constantType;

        /**
         * @param operation
         * @param argumentIndex
         * @param target
         */
        public FromConstant(Class<?> constantType, Object constant) {
            this.constant = constant;
            this.constantType = constantType;
        }

        /** {@inheritDoc} */
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            return MethodHandles.insertArguments(methodHandle, binding.index, constant);
        }

        /** {@inheritDoc} */
        public BindingProviderKind kind() {
            return BindingProviderKind.CONSTANT;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            MethodHandle mh = MethodHandles.constant(constantType, constant);
            return MethodHandles.dropArguments(mh, 0, PackedExtensionContext.class);
        }
    }

    /** Provides values by accessing a lifetime arena. */
    public static final class FromLifetimeArena extends BindingProvider {

        public final ContainerLifetimeSetup containerLifetime;

        /** The index in the arena */
        public final int index;

        public final Class<?> type;

        public FromLifetimeArena(ContainerLifetimeSetup containerLifetime, int index, Class<?> type) {
            this.containerLifetime = requireNonNull(containerLifetime);
            this.type = type;
            this.index = index;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            // Must be calculated relative to the operation...
            MethodHandle mh = MethodHandles.insertArguments(PackedExtensionContext.MH_CONSTANT_POOL_READER, 1, index);
            return MethodHandleUtil.castReturnType(mh, type); // (LifetimePool)Object -> (LifetimePool)clazz
        }
    }

    /** Provides values from the result of an operation. */
    public static final class FromOperation extends BindingProvider {

        /** The operation that produces the value for the binding. */
        public final OperationSetup operation;

        public FromOperation(OperationSetup operation) {
            this.operation = requireNonNull(operation);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            MethodHandle mh = operation.generateMethodHandle();
            return MethodHandles.collectArguments(methodHandle, binding.index, mh);
        }

        /** {@inheritDoc} */
        @Override
        public BindingProviderKind kind() {
            return BindingProviderKind.OPERATION;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            return operation.generateMethodHandle();
        }
    }
}
