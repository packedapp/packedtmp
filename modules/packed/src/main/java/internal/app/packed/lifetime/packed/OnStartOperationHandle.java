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

import app.packed.lifecycle.OnStart;
import app.packed.operation.OperationDependencyOrder;
import app.packed.operation.OperationInstaller;
import internal.app.packed.bean.BeanLifecycleOrder;

/**
 *
 */
public final class OnStartOperationHandle extends LifecycleOperationHandle {

    public boolean stopOnFailure;
    public boolean interruptOnStopping;

    public boolean fork;

    public MethodHandle methodHandle;

    /**
     * @param installer
     */
    public OnStartOperationHandle(OperationInstaller installer, OnStart annotation) {
        super(installer, annotation.order(),
                annotation.order() == OperationDependencyOrder.BEFORE_DEPENDENCIES ? BeanLifecycleOrder.START_PRE_ORDER : BeanLifecycleOrder.START_POST_ORDER);
        this.stopOnFailure = annotation.stopOnFailure();
        this.interruptOnStopping = annotation.interruptOnStopping();
        this.fork = annotation.fork();
    }

}
