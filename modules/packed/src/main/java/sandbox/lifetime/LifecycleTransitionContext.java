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
package sandbox.lifetime;

import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.lifetime.LifecycleOrder;
import app.packed.lifetime.RunState;
import sandbox.lifetime.stop.StopReason;

/**
 * A context that is available to all lifecycle transition operations.
 */

// BeanLifecycleTransitionPeriod
public interface LifecycleTransitionContext extends Context<BaseExtension> {

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

    LifecycleOrder order();

    StopReason startReason();

    /**
     * @return
     * @throws UnsupportedOperationException
     *             if the current state is different from Stopping or Terminated
     */
    StopReason stopReason();
    // Something about stoppage?
}
