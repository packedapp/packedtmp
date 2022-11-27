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

import app.packed.operation.BindingKind;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public sealed abstract class BindingResolutionSetup {

    public abstract MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle);

    public abstract BindingKind kind();

    public static final class ArgumentResolution extends BindingResolutionSetup {

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.ARGUMENT;
        }
    }

    
    public static final class ConstantResolution extends BindingResolutionSetup {

        /** The constant. */
        public final Object constant;

        /** The type of the constant. */
        public final Class<?> constantType;

        /**
         * @param operation
         * @param index
         * @param target
         */
        public ConstantResolution(Class<?> constantType, Object constant) {
            this.constant = constant;
            this.constantType = constantType;
        }

        /** {@inheritDoc} */
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            return MethodHandles.insertArguments(methodHandle, binding.index, constant);
        }

        /** {@inheritDoc} */
        public BindingKind kind() {
            return BindingKind.CONSTANT;
        }
    }
    
    public static final class LifetimePoolResolution extends BindingResolutionSetup {

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingKind kind() {
            return BindingKind.LIFETIME_POOL;
        }
    }

    public static final class OperationResolution extends BindingResolutionSetup {

        /** The operation that produces the value for the binding. */
        public final OperationSetup operation;

        public OperationResolution(OperationSetup operation) {
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
        public BindingKind kind() {
            return BindingKind.OPERATION;
        }
    }
}
