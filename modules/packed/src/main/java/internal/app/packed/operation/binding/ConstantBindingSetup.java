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

import app.packed.base.Nullable;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.BeanOperationSetup;

/**
 * A binding to a constant.
 */
public final class ConstantBindingSetup extends BindingSetup {

    public ConstantBindingSetup(BeanOperationSetup operation, int index, Object constant) {
        super(operation, index);
        this.constant = constant;
    }

    // Eller er det en extension bean??? Det er hvem der styrer vaerdien
    public ExtensionSetup managedBy;
    
    /** The constant that was bound. */
    @Nullable
    public final Object constant;
}