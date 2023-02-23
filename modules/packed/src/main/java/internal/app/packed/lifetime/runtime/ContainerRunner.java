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

import app.packed.extension.ContainerContext;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public class ContainerRunner {

    /** The runtime component node we are building. */
    private ContainerContext pool;

    /** If the application is stateful, the applications runtime. */
    @Nullable
    public final PackedManagedLifetime runtime;

    ContainerSetup container;

    ContainerRunner(PackedManagedLifetime runtime) {
        this.runtime = runtime;
    }

    public ContainerRunner(ApplicationSetup application) {
        this.runtime = new PackedManagedLifetime(this);
        // application.goal.isLaunchable() && application.driver.lifetimeKind() == OldLifetimeKind.MANAGED ? new
        // PackedManagedLifetime(this) : null;
    }

    /**
     * @return
     */
    public ContainerContext pool() {
        return pool;
    }

    void run(ContainerSetup container) {
        this.container = container;
        this.pool = container.lifetime.newRuntimePool();

        runtime.launch(container, this);
    }

    void initialize(ContainerSetup container) {
        // Run all initializers
        for (MethodHandle mh : container.lifetime.initialization.methodHandles) {
            try {
                mh.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    void start(ContainerSetup container) {
        // Run all initializers
        for (MethodHandle mh : container.lifetime.startup.methodHandles) {
            try {
                mh.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }

    void shutdown(ContainerSetup container) {
        // Run all initializers
        for (MethodHandle mh : container.lifetime.shutdown.methodHandles) {
            try {
                mh.invokeExact(pool);
            } catch (Throwable e) {
                throw ThrowableUtil.orUndeclared(e);
            }
        }
    }
}
