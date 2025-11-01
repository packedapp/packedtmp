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
package app.packed.concurrent.job;

import java.util.concurrent.TimeUnit;

import app.packed.bean.scanning.BeanTrigger.AutoInject;
import app.packed.context.Context;
import app.packed.extension.BaseExtension;
import internal.app.packed.concurrent.daemon.DaemonSideBean;

/**
 * A context object that can be injected into daemon methods.
 *
 */
// I think we need a bit understand about where we are in the shutdown process, early, vs late
@AutoInject(introspector = DaemonJobBeanIntrospector.class, requiresContext = DaemonJobContext.class)
public sealed interface DaemonJobContext extends Context<BaseExtension> permits DaemonSideBean {

    /**
     * @return
     *
     * The annotated method should exit when this method returns true.
     */
    boolean isShutdown();

    // Daemons should wait using this method
    void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException;

    void awaitShutdown() throws InterruptedException;
}


// should probably check isShutdown();

//void onShutdown(); <--- cleans up

