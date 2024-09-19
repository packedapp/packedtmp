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
package internal.app.packed.bean;

import app.packed.operation.OperationDependencyOrder;
import app.packed.runtime.RunState;

public enum BeanLifecycleOrder {
    INITIALIZE_POST_ORDER(RunState.INITIALIZING), INITIALIZE_PRE_ORDER(RunState.INITIALIZING), INJECT(RunState.INITIALIZING),
    START_POST_ORDER(RunState.STARTING), START_PRE_ORDER(RunState.STARTING), STOP_POST_ORDER(RunState.STOPPING), STOP_PRE_ORDER(RunState.STOPPING);

    public final RunState runState;

    BeanLifecycleOrder(RunState runState) {
        this.runState = runState;
    }

    public static BeanLifecycleOrder fromInitialize(OperationDependencyOrder ordering) {
        return ordering == OperationDependencyOrder.BEFORE_DEPENDENCIES ? INITIALIZE_PRE_ORDER : BeanLifecycleOrder.INITIALIZE_POST_ORDER;
    }

    public static BeanLifecycleOrder fromStarting(OperationDependencyOrder ordering) {
        return ordering == OperationDependencyOrder.BEFORE_DEPENDENCIES ? START_PRE_ORDER : BeanLifecycleOrder.START_POST_ORDER;
    }

    public static BeanLifecycleOrder fromStopping(OperationDependencyOrder ordering) {
        return ordering == OperationDependencyOrder.BEFORE_DEPENDENCIES ? STOP_PRE_ORDER : BeanLifecycleOrder.STOP_POST_ORDER;
    }
}