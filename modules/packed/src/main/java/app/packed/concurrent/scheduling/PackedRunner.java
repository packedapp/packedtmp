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
package app.packed.concurrent.scheduling;

import java.lang.invoke.MethodHandle;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
class PackedRunner implements Runnable {

    final AtomicLong count = new AtomicLong();
    final ReentrantLock l = new ReentrantLock();

    final Condition c;

    final Duration d;

    boolean isShutdown;

    final MethodHandle mh;

    final PackedVirtualThreadScheduler vts;

    PackedRunner(PackedVirtualThreadScheduler vts, MethodHandle mh, Duration d) {
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
        SchedulingContext sv = new PackedSchedulingContext(this);
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
                    mh.invokeExact(vts.pec, sv);
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
}
