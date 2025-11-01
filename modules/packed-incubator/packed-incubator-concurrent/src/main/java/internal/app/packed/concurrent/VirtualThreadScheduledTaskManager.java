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

import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import app.packed.concurrent.oldscheduling.SchedulingContext;
import internal.app.packed.extension.ExtensionContext;

/**
 *
 */
public class VirtualThreadScheduledTaskManager implements ScheduledTaskManager {

    final ExecutorService es = Executors.newVirtualThreadPerTaskExecutor();

    final ExtensionContext pec;

    final Set<ScheduledOperationRunner> runners = ConcurrentHashMap.newKeySet();

    VirtualThreadScheduledTaskManager(ExtensionContext pec) {
        this.pec = pec;
    }

    @Override
    public void schedule(MethodHandle mh, Duration d) {
        ScheduledOperationRunner pr = new ScheduledOperationRunner(this, mh, d);
        es.submit(pr);
    }

    /** {@inheritDoc} */
    @Override
    public void shutdown() {
        es.shutdown();
    }


    /**
    *
    */
    private static class ScheduledOperationRunner implements Runnable {

        final Condition c;

        final AtomicLong count = new AtomicLong();

        final Duration d;

        boolean isShutdown;

        final ReentrantLock l = new ReentrantLock();

        final MethodHandle mh;

        final VirtualThreadScheduledTaskManager vts;

        private ScheduledOperationRunner(VirtualThreadScheduledTaskManager vts, MethodHandle mh, Duration d) {
            c = l.newCondition();
            this.vts = vts;
            this.mh = mh;
            this.d = d;
        }

        /**
         * @param b
         */
        public void cancelSoft() {
            // if currentThread we are in runnable.run
            l.lock();
            try {
                isShutdown = true;
                c.signal();
            } finally {
                l.unlock();
            }
        }

        /** {@inheritDoc} */
        @Override
        public void run() {
            SchedulingContext context = new PackedSchedulingContext(this);
            vts.runners.add(this);
            try {
                for (;;) {
                    l.lock();
                    try {
                        long remaining = d.toNanos(); // must not return 0
                        while (remaining > 0) {
                            if (isShutdown) {
                                return;
                            }
                            remaining = c.awaitNanos(remaining);
                        }
                    } catch (InterruptedException e) {
                        return;
                    } finally {
                        l.unlock();
                    }

                    count.incrementAndGet();
                    try {
                        mh.invokeExact(vts.pec, context);
                    } catch (Throwable e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } finally {
                vts.runners.remove(this);
                // todo check shutdown
            }
        }

        /**
        *
        */
        record PackedSchedulingContext(ScheduledOperationRunner runner) implements SchedulingContext {

            /** {@inheritDoc} */
            @Override
            public void pause() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public void resume() {
                throw new UnsupportedOperationException();
            }

            /** {@inheritDoc} */
            @Override
            public void cancel() {
                runner.cancelSoft();
            }

            /** {@inheritDoc} */
            @Override
            public long invocationCount() {
                return runner.count.get();
            }
        }
    }

}
