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
package internal.app.packed.lifecycle.runtime;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.StructureViolationException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

import app.packed.bean.lifecycle.StartContext;
import internal.app.packed.ValueBased;
import internal.app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.LifecycleOperationHandle.StartOperationHandle;
import internal.app.packed.lifecycle.SomeLifecycleOperationHandle;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
final class StartRunner {

    final Collection<SomeLifecycleOperationHandle<StartOperationHandle>> operations;

    /** The runtime component node we are building. */
    final ExtensionContext pool;

    /** If the application is stateful, the applications runtime. */
    final RegionalManagedLifetime runtime;

    final Thread startingThread = Thread.currentThread();

    /** A structured task scope, used if forking. */
    StructuredTaskScope<Void, Void> ts;

    StartRunner(Collection<SomeLifecycleOperationHandle<StartOperationHandle>> methodHandles, ExtensionContext pool, RegionalManagedLifetime runtime) {
        this.operations = methodHandles;
        this.pool = pool;
        this.runtime = runtime;
    }

    private void run(SomeLifecycleOperationHandle<StartOperationHandle> h) {
        try {
            h.methodHandle.invokeExact(pool, (StartContext) new PackedOnStartContext(this));
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }

    void start() {
        System.out.println("STARTING " + Thread.currentThread());
        long start = System.currentTimeMillis();

        for (SomeLifecycleOperationHandle<StartOperationHandle> h : operations) {
            if (h.handle.fork) {
                ts().fork(() -> run(h));
            } else {
                run(h);
            }
        }
        StructuredTaskScope<Void, Void> sts = ts;
        if (sts != null) {
            try {
                IO.println(Thread.currentThread() == startingThread);
                IO.println(ts.toString());
                System.out.println("TRYING TO JOIN " + Thread.currentThread());
                ts.join();
                IO.println("Joined " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // IO.println("Start finished");
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
        System.out.println("!!!!!!!!!!!! Creating new scope for thread " + Thread.currentThread());
        return ts = StructuredTaskScope.open(Joiner.awaitAllSuccessfulOrThrow(),
                c -> c.withName("AppStart").withThreadFactory(Thread.ofVirtual().name("CoolAppStartin", 0).factory()));
    }

    @ValueBased
    record PackedOnStartContext(StartRunner runner) implements StartContext {

//        @Override
//        public void fail(Throwable cause) {}

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void fork(Callable<?> callable) {
            runner.ts().fork((Callable) callable);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public void fork(Runnable runnable) {
            if (Thread.currentThread() != runner.startingThread) {
                throw new IllegalStateException("Operation is already forked");
            }
            runner.ts().fork((Callable) Executors.callable(runnable));
        }

//        @Override
//        public void forkNoAwait(Runnable runnable) {}
    }
}
