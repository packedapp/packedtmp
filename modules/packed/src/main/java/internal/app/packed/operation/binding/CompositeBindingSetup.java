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

import app.packed.container.User;
import app.packed.operation.BindingMirror;
import app.packed.operation.bindings.CompositeBindingMirror;
import internal.app.packed.operation.OperationSetup;

/**
 * A composite binding.
 */
public final class CompositeBindingSetup extends NestedBindingSetup {

    /**
     * @param operation
     * @param index
     */
    public CompositeBindingSetup(OperationSetup operation, int index) {
        super(operation, index);
    }
    

    /** {@inheritDoc} */
    @Override
    public User boundBy() {
        // A composite binding is always bound by the bean itself
        return operation.bean.realm.realm();
    }


    /** {@inheritDoc} */
    @Override
    public BindingMirror mirror0() {
        return new CompositeBindingMirror();
    }
}
