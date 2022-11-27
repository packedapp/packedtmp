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
import java.util.function.Supplier;

import app.packed.container.User;
import app.packed.operation.BindingKind;
import app.packed.operation.BindingMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;

/** The configuration of a single binding in an operation. */

// Vi vil godt gemme noget om 
// Hvorfor er denne binding blevet lavet... Vi saa det var en composite, Vi resolved den som en service, den var manuelt bounded

// CompositeBinding (Always op)
// OpBinding (Always constant)
// @Anno/ Class Hook BindingOp
//// Constant
//// Non
// Service/ExtensionService (Always opish)

// Constant vs Op vs InvocationArgument

public abstract class BindingSetup {

    /** A MethodHandle for invoking {@link OperationMirror#initialize(OperationSetup)}. */
    private static final MethodHandle MH_BINDING_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), BindingMirror.class, "initialize",
            void.class, BindingSetup.class);

    public final User boundBy;

    /** The index into {@link OperationSetup#bindings}. */
    public final int index;

    /** Supplies a mirror for the operation */
    public Supplier<? extends BindingMirror> mirrorSupplier;

    /** The operation this binding is a part of. */
    public final OperationSetup operation;

    /** Supplies a mirror for the operation */
    public final BindingOrigin target;

    public BindingSetup(OperationSetup operation, int index, User user, BindingOrigin origin) {
        this.operation = requireNonNull(operation);
        this.index = index;
        this.target = origin;
        this.boundBy = requireNonNull(user);
    }

    public abstract MethodHandle bindIntoOperation(MethodHandle methodHandle);

    public abstract BindingKind kind();

    /** {@return a new mirror.} */
    public BindingMirror mirror() {
        BindingMirror mirror = ClassUtil.mirrorHelper(BindingMirror.class, BindingMirror::new, mirrorSupplier);

        // Initialize BindingMirror by calling BindingMirror#initialize(BindingSetup)
        try {
            MH_BINDING_MIRROR_INITIALIZE.invokeExact(mirror, this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return mirror;
    }

    public static final class ArgumentBindingSetup extends BindingSetup {

        /**
         * @param operation
         * @param index
         * @param user
         * @param target
         */
        public ArgumentBindingSetup(OperationSetup operation, int index, User user, BindingOrigin target) {
            super(operation, index, user, target);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        public BindingKind kind() {
            return BindingKind.ARGUMENT;
        }
    }
    /**
     * A binding to a constant.
     * 
     * @see BindingKind#CONSTANT
     */
    public static final class ConstantBindingSetup extends BindingSetup {

        /** The constant. */
        public final Object constant;

        /** The type of the constant. */
        public final Class<?> constantType;

        /**
         * @param operation
         * @param index
         * @param target
         */
        public ConstantBindingSetup(OperationSetup operation, int index, User user, BindingOrigin target, Class<?> constantType, Object constant) {
            super(operation, index, user, target);
            this.constant = constant;
            this.constantType = constantType;
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            return MethodHandles.insertArguments(methodHandle, index, constant);
        }

        /** {@inheritDoc} */
        public BindingKind kind() {
            return BindingKind.CONSTANT;
        }
    }
    
    public static final class LifetimePoolBindingSetup extends BindingSetup {

        /**
         * @param operation
         * @param index
         * @param user
         * @param target
         */
        public LifetimePoolBindingSetup(OperationSetup operation, int index, User user, BindingOrigin target) {
            super(operation, index, user, target);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            throw new UnsupportedOperationException();
        }

        public BindingKind kind() {
            return BindingKind.LIFETIME_POOL;
        }
    }
    
    public static final class OperationBindingSetup extends BindingSetup {

        /** The operation that produces the value for the binding. */
        public final OperationSetup providingOperation;

        public OperationBindingSetup(OperationSetup operation, int index, User user, BindingOrigin target, OperationSetup providingOperation) {
            super(operation, index, user, target);
            this.providingOperation = requireNonNull(providingOperation);
        }

        /** {@inheritDoc} */
        @Override
        public MethodHandle bindIntoOperation(MethodHandle methodHandle) {
            MethodHandle mh = providingOperation.generateMethodHandle();
            return MethodHandles.collectArguments(methodHandle, index, mh);
        }
        
        public BindingKind kind() {
            return BindingKind.OPERATION;
        }
    }

}
