/*
® * Copyright (c) 2008 Kasper Nielsen.
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
package internal.app.packed.lifecycle.lifetime.runtime;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.concurrent.StructureViolationException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

import app.packed.bean.lifecycle.StopContext;
import app.packed.extension.ExtensionContext;
import app.packed.runtime.StopInfo;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOperationStopHandle;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
// What do

final class StopRunner {

    final Thread startingThread = Thread.currentThread();

    final Collection<LifecycleOperationStopHandle> operations;

    /** The runtime component node we are building. */
    final ExtensionContext pool;

    /** If the application is stateful, the applications runtime. */
    final RegionalManagedLifetime runtime;

    /** A structured task scope, used if forking. */
    StructuredTaskScope<Void, Void> ts;

    @SuppressWarnings("unchecked")
    StopRunner(Collection<?> methodHandles, ExtensionContext pool, RegionalManagedLifetime runtime) {
        this.operations = (Collection<LifecycleOperationStopHandle>) methodHandles;
        this.pool = pool;
        this.runtime = runtime;
    }

    private Void run(LifecycleOperationStopHandle h) {
        MethodHandle mh = h.methodHandle;
        try {
            mh.invokeExact(pool, (StopContext) new StopContext() {

                @Override
                public boolean isApplicationStopping() {
                    return false;
                }

                @Override
                public StopInfo info() {
                    return null;
                }

            });
        } catch (Throwable e) {
            runtime.start();
            throw ThrowableUtil.orUndeclared(e);
        }
        return null;
    }

    void start() {
        long start = System.currentTimeMillis();
    //    System.out.println("Starting app from " + Thread.currentThread());
        // Run all Startup methods
        // We run in a OnStartContext
        for (LifecycleOperationStopHandle h : operations) {
            if (h.fork) {
                ts().fork(() -> run(h));
            } else {
                run(h);
            }
        }
        StructuredTaskScope<Void, Void> sts = ts;
        if (sts != null) {
            try {
                ts.join();
                System.out.println("Joined " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
  //      System.out.println("Start finished");
    }

    StructuredTaskScope<Void, Void> ts() {
        StructuredTaskScope<Void, Void> sts = ts;
        if (sts != null) {
            return sts;
        }
        // Only the starting thread should create the task scope
        if (Thread.currentThread() != startingThread) {
            throw new StructureViolationException();
        }
        return ts = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow(),
                c -> c.withName("AppStopping").withThreadFactory(Thread.ofVirtual().name("CoolAppStartin", 0).factory()));
    }

    record PackedOnStopContext(StopRunner runner, LifecycleOperationStopHandle handle) implements StopContext {

        /** {@inheritDoc} */
        @Override
        public boolean isApplicationStopping() {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public StopInfo info() {
            return null;
        }

    }
}
