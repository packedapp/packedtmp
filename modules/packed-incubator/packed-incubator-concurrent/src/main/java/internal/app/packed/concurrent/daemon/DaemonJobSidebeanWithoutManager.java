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
package internal.app.packed.concurrent.daemon;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ThreadFactory;

import app.packed.bean.SidebeanBinding;
import internal.app.packed.concurrent.daemon.DaemonJobSidebean.DaemonOperationInvoker;

/**
 *
 */
public final class DaemonJobSidebeanWithoutManager {

    volatile Thread thread;

    volatile boolean isShutdown;

    final ThreadFactory factory;

    final DaemonOperationInvoker invoker;

    public DaemonJobSidebeanWithoutManager(@SidebeanBinding ThreadFactory factory,
            @SidebeanBinding DaemonOperationInvoker invoker) {
        this.factory = requireNonNull(factory);
        this.invoker = requireNonNull(invoker);
    }

}
