/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.operation;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import app.packed.operation.OperationTarget;
import internal.app.packed.invoke.SamType;

/**
 *
 */
// I think interface, and then records
public sealed abstract class PackedOperationTarget {

    /** {@return the initial method handle.} */
    public abstract MethodHandle methodHandle();

    /** {@return the target of the operation.} */
    public OperationTarget target() {
        return (OperationTarget) this;
    }

    /** An operation that returns the bean instance the operation is defined on. */
    public static final class BeanAccessOperationTarget extends PackedOperationTarget implements OperationTarget.OfMethodHandle {

        /**
         * @param operator
         * @param site
         */
        public BeanAccessOperationTarget() {}

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            throw new UnsupportedOperationException();
        }
    }

    /** An operation that invokes the abstract method on a {@link FunctionalInterface}. */
    public static final class FunctionOperationTarget extends PackedOperationTarget implements OperationTarget.OfFunction {

        /** The method that implements the single abstract method. */
        private final Method implementationMethod;

        private final MethodHandle methodHandle;

        /** A description of SAM type. */
        private final SamType samType;

        /**
         * @param operator
         * @param site
         */
        public FunctionOperationTarget(MethodHandle methodHandle, SamType samType, Method implementationMethod) {
            this.methodHandle = requireNonNull(methodHandle);
            this.samType = requireNonNull(samType);
            this.implementationMethod = requireNonNull(implementationMethod);
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> functionalInterface() {
            return samType.functionInterface();
        }

        /** {@inheritDoc} */
        @Override
        public Method implementingMethod() {
            return implementationMethod;
        }

        /** {@inheritDoc} */
        @Override
        public Method interfaceMethod() {
            return samType.saMethod();
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            return methodHandle;
        }
    }

    /** An operation that invokes or accesses a {@link Member}. */
    public static final class MemberOperationTarget extends PackedOperationTarget {

        private final MethodHandle methodHandle;

        /** The {@link Member target member}. */
        public final OperationMemberTarget<?> target;

        // MH -> mirror - no gen
        // MH -> Gen - With caching (writethrough to whereever the bean cache it)
        // MH -> LazyGen - With caching
        public MemberOperationTarget(OperationMemberTarget<?> member, MethodHandle methodHandle) {
            this.target = requireNonNull(member);
            this.methodHandle = requireNonNull(methodHandle);

        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            return methodHandle;
        }

        @Override
        public OperationTarget target() {
            return (OperationTarget) target;
        }
    }

    /** An operation that invokes a method handle. */
    public static final class MethodHandleOperationTarget extends PackedOperationTarget implements OperationTarget.OfMethodHandle {

        final MethodHandle methodHandle;

        /**
         * @param operator
         * @param site
         */
        public MethodHandleOperationTarget(MethodHandle methodHandle) {
            this.methodHandle = requireNonNull(methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle methodHandle() {
            return methodHandle;
        }

        /** {@inheritDoc} */
        @Override
        public MethodType methodType() {
            return methodHandle.type();
        }
    }
}
