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

import java.util.concurrent.ConcurrentHashMap;

import app.packed.bean.lifecycle.OnStart;
import app.packed.extension.ExtensionContext;

// Runtime
// Hmm managing, daemons in multiple lifetimes????
// Actually I think we need to install it in the managed lifetime
public final class DaemonRuntimeManager {

    final DaemonRuntimeConfiguration[] daemons;

    final ExtensionContext extensionContext;

    final ConcurrentHashMap<Thread, DaemonRunner> deamons = new ConcurrentHashMap<>();

    public DaemonRuntimeManager(ExtensionContext extensionContext, DaemonRuntimeConfiguration[] daemons) {
        this.extensionContext = extensionContext;
        this.daemons = daemons;
    }

    // Okay, we should schedule this with callbacks actually
    // OperationHandle.onInitialize(DaemonManager.init(index));
    // No choice we need to obey dependency order...
    @OnStart
    public void onStart() {
        System.out.println("On Start");

        for (DaemonRuntimeConfiguration d : daemons) {
            // Okay need to keep track of these
            d.threadFactory().newThread(new DaemonRunner(this, d.callMe())).start();
        }
    }

    @OnStart
    public void onStop() {
        System.out.println("Bye");
    }
}