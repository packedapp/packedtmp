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
package internal.app.packed.operation;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;

/**
 * A terminal op.
 */
abstract non-sealed class TerminalOp<R> extends PackedOp<R> {

    /**
     * @param type
     * @param operation
     */
    TerminalOp(OperationType type, MethodHandle operation) {
        super(type, operation);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationSetup newOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator) {
        return new OperationSetup(bean, type, operator, newTarget());
    }

    /** {@return the target of the op.} */
    // Maaske require det i konstrukturen...
    // Vi skal jo altid lave det..
    abstract OperationTarget newTarget();

    /** An terminal op for a MethodHandle. */
    static final class MethodHandleInvoke<R> extends TerminalOp<R> {

        MethodHandleInvoke(MethodHandle methodHandle) {
            super(OperationType.ofMethodType(methodHandle.type()), methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        OperationTarget newTarget() {
            return new OperationTarget.MethodHandleOperationTarget(mhOperation);
        }
    }

    /** An op that captures 1 or more type variables. */
    static final class PackedCapturingOp<R> extends TerminalOp<R> {

        PackedCapturingOp(OperationType type, MethodHandle methodHandle) {
            super(type, methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        OperationTarget newTarget() {
            return new OperationTarget.FunctionOperationTarget(mhOperation, Function.class);
        }
    }
}
