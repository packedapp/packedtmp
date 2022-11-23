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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;

/** A terminal op. */
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
    public final OperationSetup newOperationSetup(BeanSetup bean, ExtensionSetup operator) {
        return new OperationSetup(operator, newOperationSite(bean));
    }

    /** {@return a new operation site for the op.} */
    abstract OperationSite newOperationSite(BeanSetup bean);

    /** An op that wraps a MethodHandle. */
    static final class MethodHandleInvoke<R> extends TerminalOp<R> {

        MethodHandleInvoke(MethodHandle methodHandle) {
            super(OperationType.ofMethodType(methodHandle.type()), methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        OperationSite newOperationSite(BeanSetup bean) {
            return new OperationSite.MethodHandleOperationSite(bean, type, mhOperation);
        }
    }

    /** An op that captures 1 or more type variables. */
    static final class PackedCapturingOp<R> extends TerminalOp<R> {

        private final Class<?> functionalType;

        PackedCapturingOp(OperationType type, MethodHandle methodHandle, Class<?> functionalType) {
            super(type, methodHandle);
            this.functionalType = requireNonNull(functionalType);
        }

        /** {@inheritDoc} */
        @Override
        OperationSite newOperationSite(BeanSetup bean) {
            return new OperationSite.FunctionOperationSite(bean, type, mhOperation, functionalType);
        }
    }
}
