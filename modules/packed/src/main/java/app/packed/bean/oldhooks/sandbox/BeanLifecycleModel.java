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
package app.packed.bean.oldhooks.sandbox;

import app.packed.bean.oldhooks.sandbox.BeanDriverSandbox.LifecycleInvoker;
import app.packed.lifecycle.RunState;

/**
 *
 */
/// Container, Container_lazy
/// Members_as_request (Methods only I think)
/// Instantiere + Koere til en end-state  (Service-Prototype)
/// Instantiere + Koere til en end-state og aflevere instancen
/// Med instance Koere til en end-state (vi har allerede instancen so no return)

public interface BeanLifecycleModel<T> {

    static final BeanLifecycleModel<Void> CONTAINER = null;

    static final BeanLifecycleModel<Void> CONTAINER_LAZY = null;

    // Never any instances directly
    //// All non-lifecycle methods will create a new bean
    static final BeanLifecycleModel<Void> REQUEST_METHODS = null;

    /// Will initialize and return the bean
    static final BeanLifecycleModel<LifecycleInvoker> INITIALIZE_ONLY = returningInstance(RunState.RUNNING);

    // two invokers, first will up to start
    // second will stop
    static final BeanLifecycleModel<LifecycleInvoker[]> ASYNC_START_STOP = null;
    
    static BeanLifecycleModel<LifecycleInvoker> returningInstance(RunState endState) {
        // En invoker der koere alle lifecycle indtil et vist stadie hvorefter den returnere.
        // instancen, og ellers ikke holder noget fast omkring instancen
        throw new UnsupportedOperationException();
    }
}
