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
import app.packed.operation.Op0;
import app.packed.operation.Op1;
import app.packed.operation.Op2;
import app.packed.operation.OperationType;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.operation.binding.NestedBindingSetup;

/**
 * An op that captures 1 or more type variables.
 * 
 * @see Op0
 * @see Op1
 * @see Op2
 */
final class PackedCapturingOp<R> extends PackedOp<R> {

    PackedCapturingOp(OperationType type, MethodHandle methodHandle) {
        super(type, methodHandle);
    }

    /** {@inheritDoc} */
    @Override
    public OperationSetup newOperationSetup(BeanSetup bean, OperationType type, ExtensionSetup operator, @Nullable NestedBindingSetup nestedBinding) {
        return new OperationSetup(bean, type, operator, null, nestedBinding);
    }
}