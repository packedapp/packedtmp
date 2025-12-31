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
import java.util.concurrent.TimeUnit;

import app.packed.bean.lifecycle.Start;
import app.packed.bean.lifecycle.Stop;
import app.packed.bean.sidebean.SidebeanBinding;
import app.packed.concurrent.DaemonJobContext;

/**
 *
 */
public final class DaemonJobSidebean implements DaemonJobContext {

    private volatile Thread thread;

    private volatile boolean isShutdown;

    private final ThreadFactory factory;

    private final DaemonInvoker invoker;

    public DaemonJobSidebean(@SidebeanBinding ThreadFactory factory, @SidebeanBinding DaemonInvoker invoker) {
        this.factory = requireNonNull(factory);
        this.invoker = requireNonNull(invoker);
    }

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
    protected void onStart() {
        System.out.println("Starting daemon");
        thread = factory.newThread(new Runnable() {
            @Override
            public void run() {
                while (!isShutdown) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    invoker.invoke(DaemonJobSidebean.this);
                }
            }
        });

        thread.start();
    }

    @Stop
    protected void onStop() {
        System.out.println("Stopping daemon");

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
