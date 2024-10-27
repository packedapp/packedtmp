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
package app.packed.bean.lifecycle;

import java.util.concurrent.TimeUnit;

import app.packed.bean.scanning.BeanTrigger.OnExtensionServiceBeanTrigger;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import app.packed.runtime.StopInfo;

/**
 *
 */
// Bliver lavet per operation
// BeanStopContext
@OnExtensionServiceBeanTrigger(extension = BaseExtension.class, requiresContext = StopContext.class)
public interface StopContext extends Context<BaseExtension> {

    /**
     * {@return whether or not the whole application is stopping or only the li
     */
    boolean isApplicationStopping();

    /** {@return Information about why the containing lifetime was stopped.} */
    StopInfo info();
    // isApplicationStopping();


    default void await(AwaitingTimeoutFunction f) {

    }

    @FunctionalInterface
    interface AwaitingTimeoutFunction {
        boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    }
}
