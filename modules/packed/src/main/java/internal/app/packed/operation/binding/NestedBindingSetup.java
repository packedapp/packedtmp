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

import java.lang.invoke.MethodHandle;

import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// Ved ikke om vi gider have det hiraki...
public abstract sealed class NestedBindingSetup extends BindingSetup permits CompositeBindingSetup, FusedBindingSetup {

    /**
     * @param operation
     * @param index
     */
    public NestedBindingSetup(OperationSetup operation, int index) {
        super(operation, index);
    }

    public OperationSetup nestedOperation;

    public final OperationSetup providedBy = null;
    

    /** {@inheritDoc} */
    @Override
    public MethodHandle read() {
        return nestedOperation.buildInvoker();
    }
}
