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

import app.packed.operation.OperationMirror;
import app.packed.operation.OperationType;
import internal.app.packed.container.ExtensionSetup;

/**
 *
 */
// Som jeg kan se kan vi ikke faa cirkler her...
// Vi aldrig exposer et result som kan bruges af andre
// Vi tillader kun args, contexts, services

// BindingHelperOperation
public final class FusedOperationSetup extends OperationSetup {

    public final BeanOperationSetup bos;

    /** The extension that is fusing the operation. */
    public ExtensionSetup fuser;

    /**
     * @param bean
     * @param count
     */
    protected FusedOperationSetup(BeanOperationSetup bos, OperationType type) {
        super(bos.bean, type);
        this.bos = bos;
    }

    /** {@inheritDoc} */
    @Override
    public OperationMirror mirror() {
        // FusedOperationMirror <- a fused operation are simple
        // helper operation that extensions uses to provide values
        // to bindings
        
        throw new UnsupportedOperationException();
    }
}
