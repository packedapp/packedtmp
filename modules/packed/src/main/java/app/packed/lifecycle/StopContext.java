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
package app.packed.lifecycle;

import java.util.concurrent.TimeUnit;

import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.scanning.IntrospectorOnAutoService;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;

/**
 * A context that can be injected into methods annotated with {@link Stop}.
 */
@AutoService(introspector = StopContextBeanIntrospector.class, requiresContext = StopContext.class)
public interface StopContext extends Context<BaseExtension> {

    // Hmm??
    default void await(AwaitingTimeoutFunction f) {}

    /**
     * {@return whether or not the whole application is stopping or only the li
     */
    // I don't think this should be on stopInfo
    // Maybe return context
    // What about Application stopping -> DaemonJobContext stopping???
    // There should be some way to distinguish between these two situations
    boolean isApplicationStopping();

    /** {@return information about why the bean is being stopping} */
    StopInfo stopInfo();

    @FunctionalInterface
    interface AwaitingTimeoutFunction {
        boolean await(long timeout, TimeUnit unit) throws InterruptedException;
    }
}

final class StopContextBeanIntrospector extends BaseExtensionBeanIntrospector {

    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnAutoService service) {
        service.binder().bindContext(StopContext.class);
    }
}
