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
package app.packed.concurrent;

import java.util.concurrent.TimeUnit;

import app.packed.context.Context;
import app.packed.extension.BeanTrigger.BindingClassBeanTrigger;

/**
 *
 */
@BindingClassBeanTrigger(extension = ThreadExtension.class, requiresContext = DaemonContext.class)
public interface DaemonContext extends Context<ThreadExtension> {

    boolean isShutdown();

    // Daemons should wait using this method
    void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException;

    // should probably check isShutdown();
    void await() throws InterruptedException;
}
