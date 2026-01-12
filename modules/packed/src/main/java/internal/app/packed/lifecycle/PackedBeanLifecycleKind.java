/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import app.packed.lifecycle.RunState;

/**
 * The various internal bean lifecycle
 */
public enum PackedBeanLifecycleKind {

    /** Creates a bean instance. */
    FACTORY(RunState.INITIALIZING, DependantOrder.RUN_BEFORE_DEPENDANTS),

    /** Runs injection operations. */
    INJECT(RunState.INITIALIZING, DependantOrder.RUN_BEFORE_DEPENDANTS),

    /** Runs initialization natural order. */
    INITIALIZE_PRE_ORDER(RunState.INITIALIZING, DependantOrder.RUN_BEFORE_DEPENDANTS),

    /** Runs initialization reverse. */
    INITIALIZE_POST_ORDER(RunState.INITIALIZING, DependantOrder.RUN_AFTER_DEPENDANTS),

    START_PRE_ORDER(RunState.STARTING, DependantOrder.RUN_BEFORE_DEPENDANTS), START_POST_ORDER(RunState.STARTING, DependantOrder.RUN_AFTER_DEPENDANTS),

    STOP_PRE_ORDER(RunState.STOPPING, DependantOrder.RUN_BEFORE_DEPENDANTS), STOP_POST_ORDER(RunState.STOPPING, DependantOrder.RUN_AFTER_DEPENDANTS);

    public final RunState runState;

    public final DependantOrder ordering;

    PackedBeanLifecycleKind(RunState runState, DependantOrder ordering) {
        this.runState = runState;
        this.ordering = ordering;
    }

    /**
     *
     * @see OnInitialize
     * @see OnStart
     * @see OnStop
     */
    // BeanLifecycleOrder
    // DependencyOrder <---
    // In app.packed.lifetime/lifecycle?

    // PreOrder, PostOrder | OperationDependencyORder->DependencyOrder (Or just Ordering)

    // remove this, and just have boolean naturalOrder
    public enum DependantOrder {

        /** The operation will be executed before any other operation on beans that have this bean as a dependency. */
        RUN_BEFORE_DEPENDANTS,

        /** The operation will be executed after any dependencies. */
        RUN_AFTER_DEPENDANTS;
    }
}