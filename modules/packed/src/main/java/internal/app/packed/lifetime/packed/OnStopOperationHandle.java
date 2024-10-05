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

import java.lang.invoke.MethodHandle;

import app.packed.lifecycle.OnStop;
import app.packed.operation.OperationDependencyOrder;
import app.packed.operation.OperationInstaller;
import internal.app.packed.bean.BeanLifecycleOrder;

/**
 *
 */
public final class OnStopOperationHandle extends LifecycleOperationHandle {


    public boolean fork;
    public MethodHandle methodHandle;

    /**
     * @param installer
     */
    public OnStopOperationHandle(OperationInstaller installer, OnStop annotation) {
        super(installer, annotation.order(),
                annotation.order() == OperationDependencyOrder.BEFORE_DEPENDENCIES ? BeanLifecycleOrder.STOP_PRE_ORDER : BeanLifecycleOrder.STOP_POST_ORDER);

        this.fork = annotation.fork();
    }

}
