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

import app.packed.operation.BindingTargetKind;
import internal.app.packed.lifetime.LifetimeAccessor;
import internal.app.packed.lifetime.LifetimeObjectArena;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public sealed abstract class BindingResolutionSetup {

    public abstract MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle);

    public abstract BindingTargetKind kind();

    public abstract MethodHandle provideSpecial();
    
    public static final class ArgumentResolution extends BindingResolutionSetup {

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingTargetKind kind() {
            return BindingTargetKind.ARGUMENT;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            throw new UnsupportedOperationException();
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
        public BindingTargetKind kind() {
            return BindingTargetKind.CONSTANT;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            MethodHandle mh = MethodHandles.constant(constantType, constant);
            return MethodHandles.dropArguments(mh, 0, LifetimeObjectArena.class);
        }
    }

    public static final class LifetimePoolResolution extends BindingResolutionSetup {

        public final LifetimeAccessor.DynamicAccessor da;

        public LifetimePoolResolution(LifetimeAccessor.DynamicAccessor da) {
            this.da = da;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(BindingSetup binding, MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BindingTargetKind kind() {
            return BindingTargetKind.LIFETIME_POOL;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            return da.poolReader();
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
        public BindingTargetKind kind() {
            return BindingTargetKind.OPERATION;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle provideSpecial() {
            return operation.generateMethodHandle();
        }
    }
}
