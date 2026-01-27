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
package sandbox.lifetime;

import app.packed.lifecycle.RunState;
import app.packed.lifecycle.runtime.StopInfo.Trigger;

/**
 * A context that is available to all lifecycle transition operations.
 */
// BeanLifecycleTransitionPeriod

// Den er lidt droppet igen. Fordi deles daarligt mellem fx
// initialize og stop.
// Fx fork giver jo ikke mening for initialize

public interface LifecycleTransitionContext {

    RunState currentState();

    RunState desiredState();

    RunState nextState();

    /**
     * @param r
     *
     *
     * @throws UnsupportedOperationException
     *             if fork is not supported
     */
    void fork(Runnable r);

    boolean isNaturalOrder();

    Trigger startReason();

    /**
     * @return
     * @throws UnsupportedOperationException
     *             if the current state is different from Stopping or Terminated
     */
    Trigger stopReason();
    // Something about stoppage?
}
