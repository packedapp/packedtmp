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
package internal.app.packed.operation.op;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;

/** A op that binds 1 or more constants. */
final class BoundOp<R> extends PackedOp<R> {

    /** The arguments to insert. */
    final Object[] arguments;

    final PackedOp<R> delegate;

    /** The ExecutableFactor or FieldFactory to delegate to. */
    final int index;

    BoundOp(OperationType type, MethodHandle methodHandle, PackedOp<R> delegate, int index, Object[] arguments) {
        super(type, methodHandle);
        this.delegate = delegate;
        this.index = index;
        this.arguments = arguments;
    }

    /** {@inheritDoc} */
    @Override
    public OperationSetup newOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, @Nullable NestedBindingSetup nestedBinding) {
        OperationSetup os = delegate.newOperationSetup(bean, type, operator, nestedBinding);
        return os;
        // insert bindings
    }
}