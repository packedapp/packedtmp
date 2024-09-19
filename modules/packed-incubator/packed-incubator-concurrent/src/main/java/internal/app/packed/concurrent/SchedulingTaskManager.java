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
package internal.app.packed.concurrent;

import app.packed.bean.ComputedConstant;
import app.packed.extension.ExtensionContext;
import app.packed.lifetime.OnStart;
import internal.app.packed.concurrent.VirtualThreadScheduledTaskManager.DaemonOperationRunner;

// Runtime
public class SchedulingTaskManager {

    final ExtensionContext cc;

    final ScheduledDaemon[] daemons;

    final ScheduledOperation[] mhs;
    final boolean shutdownOnExit;
    final ScheduledTaskManager vts;

    public SchedulingTaskManager(ExecutorConfiguration scheduler, @ComputedConstant ScheduledOperation[] mhs, @ComputedConstant ScheduledDaemon[] daemons,
            ExtensionContext cc) {
        if (scheduler == null) {
            this.vts = new VirtualThreadScheduledTaskManager(cc);
            shutdownOnExit = true;
        } else {
            this.vts = null;
            shutdownOnExit = scheduler.shutdownOnExit();
        }
        this.cc = cc;
        this.daemons = daemons;
        this.mhs = mhs;
    }

    @OnStart
    public void onStart() {
        System.out.println("On Start");

        for (ScheduledOperation p : mhs) {
            vts.schedule(p.callMe(), p.s().d());
        }
        for (ScheduledDaemon d : daemons) {
            if (d.useVirtual()) {
                Thread.startVirtualThread(new DaemonOperationRunner(cc, d.callMe()));
            } else {
                Thread.ofPlatform().daemon().start(new DaemonOperationRunner(cc, d.callMe()));
            }
        }
    }

    @OnStart
    public void onStop() {
        System.out.println("Bye");
    }

}