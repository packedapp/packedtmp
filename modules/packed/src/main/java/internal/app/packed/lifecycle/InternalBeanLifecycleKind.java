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
package internal.app.packed.lifecycle;

import app.packed.bean.lifecycle.LifecycleDependantOrder;
import app.packed.runtime.RunState;

/**
 * The various internal bean lifecycle
 */
public enum InternalBeanLifecycleKind {

    FACTORY(RunState.INITIALIZING, LifecycleDependantOrder.BEFORE_DEPENDANTS),
    INJECT(RunState.INITIALIZING, LifecycleDependantOrder.BEFORE_DEPENDANTS),
    INITIALIZE_PRE_ORDER(RunState.INITIALIZING, LifecycleDependantOrder.BEFORE_DEPENDANTS),
    INITIALIZE_POST_ORDER(RunState.INITIALIZING, LifecycleDependantOrder.AFTER_DEPENDANTS),

    START_PRE_ORDER(RunState.STARTING, LifecycleDependantOrder.BEFORE_DEPENDANTS),
    START_POST_ORDER(RunState.STARTING, LifecycleDependantOrder.AFTER_DEPENDANTS),

    STOP_PRE_ORDER(RunState.STOPPING, LifecycleDependantOrder.BEFORE_DEPENDANTS),
    STOP_POST_ORDER(RunState.STOPPING, LifecycleDependantOrder.AFTER_DEPENDANTS);

    public final RunState runState;

    public final LifecycleDependantOrder ordering;

    InternalBeanLifecycleKind(RunState runState, LifecycleDependantOrder ordering) {
        this.runState = runState;
        this.ordering=ordering;
    }
}