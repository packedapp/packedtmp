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
package internal.app.packed.lifetime.packed;

import app.packed.lifecycle.OnInitialize;
import app.packed.operation.OperationConfiguration;
import app.packed.operation.OperationDependencyOrder;
import app.packed.operation.OperationMirror;
import app.packed.operation.OperationTemplate.Installer;
import internal.app.packed.bean.BeanLifecycleOrder;

/**
 *
 */
public final class OnInitializeOperationHandle extends LifecycleOperationHandle {

    /**
     * @param installer
     */
    public OnInitializeOperationHandle(Installer installer, OnInitialize annotation) {
        super(installer, annotation.order(), annotation.order() == OperationDependencyOrder.BEFORE_DEPENDENCIES ? BeanLifecycleOrder.INITIALIZE_PRE_ORDER
                : BeanLifecycleOrder.INITIALIZE_POST_ORDER);
    }

    @Override
    protected OperationConfiguration newOperationConfiguration() {
        return super.newOperationConfiguration();
    }

    @Override
    protected OperationMirror newOperationMirror() {
        return super.newOperationMirror();
    }

}
