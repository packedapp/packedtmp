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
package app.packed.operation;

import static java.util.Objects.checkIndex;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Supplier;

import app.packed.bean.BeanIntrospector.OnBinding;
import internal.app.packed.bean.BeanAnalyzerOnBinding;
import internal.app.packed.operation.OperationSetup;

/** Implementation of {@link OperationHandle}. */
public final class PackedOperationHandle implements OperationHandle {

    /** The wrapped operation. */
    private final OperationSetup operation;

 PackedOperationHandle(OperationSetup operation) {
        this.operation = requireNonNull(operation);
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle buildInvoker() {
        // Hav en version der tager en ExtensionBeanConfiguration eller bring back ExtensionContext

        if (operation.isComputed) {
            throw new IllegalStateException("This method can only be called once");
        }

        operation.isComputed = true;
        // application.checkIsComputable
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public InvocationType invocationType() {
        return operation.invocationSite.invocationType;
    }

    /** {@inheritDoc} */
    public OperationHandle specializeMirror(Supplier<? extends OperationMirror> supplier) {
        if (operation.isComputed) {
            throw new IllegalStateException("Cannot set a mirror after an invoker has been computed");
        }
        operation.mirrorSupplier = requireNonNull(supplier, "supplier is null");
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public OperationType type() {
        return operation.type;
    }

    /** {@inheritDoc} */
    @Override
    public OnBinding parameter(int index) {
        checkIndex(index, operation.type.parameterCount());
        return new BeanAnalyzerOnBinding(operation, index, operation.invocationSite.invokingExtension, null, operation.type.parameter(index));
    }
}
