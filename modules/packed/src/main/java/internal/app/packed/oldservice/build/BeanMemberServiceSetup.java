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
package internal.app.packed.oldservice.build;

import app.packed.framework.Nullable;
import app.packed.service.Key;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.lifetime.pool.Accessor.DynamicAccessor;
import internal.app.packed.oldservice.InternalServiceExtension;
import internal.app.packed.oldservice.runtime.PrototypeRuntimeService;
import internal.app.packed.oldservice.runtime.RuntimeService;
import internal.app.packed.oldservice.runtime.ServiceInstantiationContext;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
public final class BeanMemberServiceSetup extends ServiceSetup {

    /** If constant, the region index to store it in */
    @Nullable
    public final DynamicAccessor accessor;

    final OperationSetup operation;

    public BeanMemberServiceSetup(InternalServiceExtension im, BeanSetup beanSetup, Key<?> key, boolean isConst,
            OperationSetup operation) {
        super(key);
        this.operation = operation;
        // TODO fix Object
        this.accessor = isConst ? beanSetup.container.lifetime.pool.reserve(Object.class) : null;
    }


    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return accessor != null;
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeService newRuntimeNode(ServiceInstantiationContext context) {
        if (isConstant()) {
            return RuntimeService.constant(key(), accessor.read(context.pool));
        } else {
            return new PrototypeRuntimeService(this, context.pool, operation.buildInvoker());
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "@Provide " ;
    }
}
