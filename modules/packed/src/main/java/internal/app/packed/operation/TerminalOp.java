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

import app.packed.util.OperationType;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup.EmbeddedIntoOperation;
import sandbox.extension.operation.OperationTemplate;

/** A terminal op. */
public abstract sealed class TerminalOp<R> extends PackedOp<R> {

    /**
     * @param type
     * @param operation
     */
    private TerminalOp(OperationType type, MethodHandle operation) {
        super(type, operation);
    }

    /** An op that invokes a method on a functional interface. */
    public static final class FunctionInvocationOp<R> extends TerminalOp<R> {

        /** The method extending the single abstract method. */
        private final Method implementationMethod;

        /** The single abstract method type of the function. */
        private final SamType samType;

        public FunctionInvocationOp(OperationType type, MethodHandle methodHandle, SamType samType, Method implementationMethod) {
            super(type, methodHandle);
            this.samType = requireNonNull(samType);
            this.implementationMethod = requireNonNull(implementationMethod);
        }

        /** {@inheritDoc} */
        @Override
        public OperationSetup newOperationSetup(BeanSetup bean, ExtensionSetup operator, OperationTemplate template,
                @Nullable EmbeddedIntoOperation nestedParent) {
            template = template.returnType(type.returnRawType());
            OperationSetup os = new OperationSetup.FunctionOperationSetup(operator, bean, type, template, nestedParent, mhOperation, samType,
                    implementationMethod);
            return os;
        }
    }

    /** An op that wraps a MethodHandle. */
    static final class MethodHandleInvoke<R> extends TerminalOp<R> {

        MethodHandleInvoke(MethodHandle methodHandle) {
            super(OperationType.fromMethodType(methodHandle.type()), methodHandle);
        }

        /** {@inheritDoc} */
        @Override
        public OperationSetup newOperationSetup(BeanSetup bean, ExtensionSetup operator, OperationTemplate template,
                @Nullable EmbeddedIntoOperation nestedParent) {
            return new OperationSetup.MethodHandleOperationSetup(operator, bean, type, template, nestedParent, mhOperation);
        }
    }
}
