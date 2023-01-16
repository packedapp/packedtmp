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
import java.lang.reflect.Method;

import app.packed.operation.OperationTemplate;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;

/** A terminal op. */
abstract sealed class TerminalOp<R> extends PackedOp<R> {

    /**
     * @param type
     * @param operation
     */
    private TerminalOp(OperationType type, MethodHandle operation) {
        super(type, operation);
    }

    /** An op that captures 1 or more type variables. */
    static final class FunctionInvocationOp<R> extends TerminalOp<R> {

        /** The method extending the single abstract method. */
        private final Method implementationMethod;

        /** The single abstract method type of the function. */
        private final SamType samType;

        FunctionInvocationOp(OperationType type, MethodHandle methodHandle, SamType samType, Method implementationMethod) {
            super(type, methodHandle);
            this.samType = requireNonNull(samType);
            this.implementationMethod = requireNonNull(implementationMethod);
        }

        /** {@inheritDoc} */
        @Override
        public OperationSetup newOperationSetup(BeanSetup bean, ExtensionSetup operator, OperationTemplate template) {
            template = template.withReturnType(type.returnType());
            OperationSetup os = new OperationSetup.FunctionOperationSetup(operator, bean, type, template, mhOperation, samType, implementationMethod);
            return os;
        }
    }

    /** An op that wraps a MethodHandle. */
    static final class MethodHandleInvoke<R> extends TerminalOp<R> {

        MethodHandleInvoke(MethodHandle methodHandle) {
            super(OperationType.ofMethodType(methodHandle.type()), methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public OperationSetup newOperationSetup(BeanSetup bean, ExtensionSetup operator, OperationTemplate template) {
            return new OperationSetup.MethodHandleOperationSetup(operator, bean, type, template, mhOperation);
        }
    }
}
