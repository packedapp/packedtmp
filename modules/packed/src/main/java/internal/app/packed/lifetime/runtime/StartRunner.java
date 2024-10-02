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
package internal.app.packed.lifetime.runtime;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;

import app.packed.extension.ExtensionContext;
import app.packed.lifecycle.OnStartContext;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public final class StartRunner {

    Collection<MethodHandle> methodHandles;

    /** The runtime component node we are building. */
    ExtensionContext pool;

    StructuredTaskScope<Void> ts = new StructuredTaskScope<>();

    /** If the application is stateful, the applications runtime. */
    PackedManagedLifetime runtime;

    StartRunner(Collection<MethodHandle> methodHandles, ExtensionContext pool, PackedManagedLifetime runtime) {
        this.methodHandles = methodHandles;
        this.pool = pool;
        this.runtime = runtime;
    }

    public void start() {
        // Run all Startup methods
        // We run in a OnStartContext
        for (MethodHandle mh : methodHandles) {
            try {
                mh.invokeExact(pool, (OnStartContext) new OnStartContext() {

                    @Override
                    public void forkNoAwait(Runnable runnable) {}

                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    @Override
                    public void fork(Runnable runnable) {
                        ts.fork((Callable) Executors.callable(runnable));
                    }

                    @Override
                    public void fail(Throwable cause) {}

                    @SuppressWarnings({ "unchecked", "rawtypes" })
                    @Override
                    public void fork(Callable<?> callable) {
                        ts.fork((Callable) callable);
                    }
                });
            } catch (Throwable e) {
                runtime.start();
                throw ThrowableUtil.orUndeclared(e);
            }
        }

        try {
            ts.join();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
