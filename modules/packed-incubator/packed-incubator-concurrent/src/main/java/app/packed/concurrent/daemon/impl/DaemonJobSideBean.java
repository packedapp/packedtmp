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
package app.packed.concurrent.daemon.impl;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import app.packed.bean.lifecycle.Start;
import app.packed.bean.sidebean.SidebeanService;
import app.packed.concurrent.daemon.DaemonJobContext;

/**
 *
 */
public final class DaemonJobSideBean implements DaemonJobContext {

    private volatile Thread thread;

    private volatile boolean isShutdown;

    /** {@inheritDoc} */
    @Override
    public void awaitShutdown() throws InterruptedException {
        IO.println("AWAIT");
    }

    /** {@inheritDoc} */
    @Override
    public void awaitShutdown(long timeout, TimeUnit unit) throws InterruptedException {
        IO.println("AWAIT");
    }

    /** {@inheritDoc} */
    @Override
    public boolean isShutdown() {
        return false;
    }

    ///////////////// Lifecycle
    @Start
    protected void onStart(@SidebeanService ThreadFactory factory, @SidebeanService DaemonOperationInvoker invoker) {
        thread = factory.newThread(new Runnable() {
            @Override
            public void run() {
                while (!isShutdown) {
                    invoker.invoke(DaemonJobSideBean.this);
                }
            }
        });

        thread.start();
    }

    protected void onStop() {
        isShutdown = true;
        Thread t = thread;
        if (t != null) {
            // Maybe add T to some application global awaiting queue...
        }
    }

    interface DaemonOperationInvoker {
        void invoke(DaemonJobContext context);
    }
}
