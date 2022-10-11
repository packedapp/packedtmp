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

/**
 *
 */
public final class CompositeOperationSetup extends OperationSetup {

    final BeanOperationSetup bos;

    // Are composite can be embedded in another composite..
    // Should we keep track of this?
    /**
     * @param bean
     * @param count
     */
    protected CompositeOperationSetup(BeanOperationSetup bos, OperationType type) {
        super(bos.bean, type);
        this.bos = bos;
    }

    /** {@inheritDoc} */
    @Override
    public OperationMirror mirror() {
        // No I think we need a CompositeOperationMirror
        // Maybe which has a parent (Then we make sure it is not reusable)
        return bos.mirror();
    }
}
