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

import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// Hvis vi tager en Op (Hvad vi vil goer... Saa kan vi jo ogsaa vaere methods)
// ExtensionOp
public final class FusedBindingSetup extends NestedBindingSetup {

    // Eller er det en extension bean??? Det er hvem der styrer vaerdien
    public ExtensionSetup managedBy;

    public final OperationSetup operation;

    /**
     * @param beanOperation
     * @param index
     */
    public FusedBindingSetup(OperationSetup original, int index) {
        super(original, index);
        operation = null;
    }
}
