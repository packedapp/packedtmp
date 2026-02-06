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
package app.packed.concurrent;

import java.util.concurrent.TimeUnit;

import app.packed.bean.BeanTrigger.AutoService;
import app.packed.binding.Key;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.bean.introspection.IntrospectorOnAutoService;
import internal.app.packed.concurrent.daemon.DaemonJobSidehandle;
import internal.app.packed.extension.base.BaseExtensionBeanIntrospector;

/**
 * A context object that can be injected into daemon methods.
 *
 */
// I think we need a bit understand about where we are in the shutdown process, early, vs late
@AutoService(introspector = DaemonJobContextBeanIntrospector.class, requiresContext = DaemonJobContext.class)
public sealed interface DaemonJobContext extends Context<BaseExtension> permits DaemonJobSidehandle {

    /**
     * @return The annotated method should exit when this method returns true.
     */
    boolean isShutdown();

    /**
     * Blocks until the job has been stopped (for example, the application has been shutdown) , or the timeout occurs, or the current thread is interrupted, whichever happens
     * first.
     *
     * @param timeout
     *            the maximum time to wait
     * @param unit
     *            the time unit of the timeout argument
     * @return {@code true} if this bean has terminated and {@code false} if the timeout elapsed before termination
     * @throws InterruptedException
     *             if interrupted while waiting
     */
    // Maybe rename to sleep instead???
    boolean awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException;
}

final class DaemonJobContextBeanIntrospector extends BaseExtensionBeanIntrospector {

    /** {@inheritDoc} */
    @Override
    public void onExtensionService(Key<?> key, IntrospectorOnAutoService service) {
        service.binder().bindContext(DaemonJobContext.class);
    }
}
