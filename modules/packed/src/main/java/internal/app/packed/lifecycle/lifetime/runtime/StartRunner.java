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
package internal.app.packed.lifecycle.lifetime.runtime;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.StructureViolationException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

import app.packed.bean.lifecycle.OnStartContext;
import app.packed.extension.ExtensionContext;
import internal.app.packed.lifecycle.BeanLifecycleOperationHandle.LifecycleOnStartHandle;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
final class StartRunner {

    final Collection<LifecycleOnStartHandle> operations;

    /** The runtime component node we are building. */
    final ExtensionContext pool;

    /** If the application is stateful, the applications runtime. */
    final RegionalManagedLifetime runtime;

    final Thread startingThread = Thread.currentThread();

    /** A structured task scope, used if forking. */
    StructuredTaskScope<Void, Void> ts;

    StartRunner(Collection<LifecycleOnStartHandle> methodHandles, ExtensionContext pool, RegionalManagedLifetime runtime) {
        this.operations = methodHandles;
        this.pool = pool;
        this.runtime = runtime;
    }

    private Void run(LifecycleOnStartHandle h) {
        MethodHandle mh = h.methodHandle;
        try {
            mh.invokeExact(pool, (OnStartContext) new PackedOnStartContext(this));
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return null;
    }

    void start() {
        long start = System.currentTimeMillis();

        for (LifecycleOnStartHandle h : operations) {
            if (h.fork) {
                ts().fork(() -> run(h));
            } else {
                run(h);
            }
        }
        StructuredTaskScope<Void, Void> sts = ts;
        if (sts != null) {
            try {
                System.out.println(Thread.currentThread() == startingThread);
                System.out.println(ts.toString());
                ts.join();
                System.out.println("Joined " + (System.currentTimeMillis() - start));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // System.out.println("Start finished");
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
                c -> c.withName("AppStart").withThreadFactory(Thread.ofVirtual().name("CoolAppStartin", 0).factory()));
    }

    record PackedOnStartContext(StartRunner runner) implements OnStartContext {

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
            runner.ts().fork((Callable) Executors.callable(runnable));
        }

//        @Override
//        public void forkNoAwait(Runnable runnable) {}
    }
}
