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

import java.util.Set;

import app.packed.bean.hooks.sandboxinvoke.InvokerConfiguration;
import app.packed.lifecycle.RunState;
import app.packed.state.State;

/**
 *
 */
public interface BeanDriverSandbox {
    
    
    // Same lifecycle as the container
    // Can provide services
    void lifecycleContainer();
    
    
    void lifecycleContainerLazy();
    void lifecycleContainerMemberRequest();
    LifecycleInvoker lifecycleRunUntil(RunState runState, boolean returnInstance);
    LifecycleInvoker[] lifecycleRunUntil(RunState... toState);
    
    // Vi mangler maaske 
    
    <T> T lifecycle(BeanLifecycleModel<T> mode);


    interface LifecycleInvoker extends InvokerConfiguration {
        Set<State> states();
    }
    
    @SuppressWarnings("unused")
    public static void main(BeanDriverSandbox bs) {
        LifecycleInvoker[] inv = bs.lifecycleRunUntil(RunState.RUNNING, RunState.TERMINATED);
        
        
        LifecycleInvoker asyncStart = inv[0];
        
        LifecycleInvoker asyncStop = inv[0];
        
        // asyncStop.asyncMode

    }
}
