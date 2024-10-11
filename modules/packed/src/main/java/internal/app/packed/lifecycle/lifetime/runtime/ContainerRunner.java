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

import app.packed.extension.ExtensionContext;
import app.packed.runtime.RunState;
import app.packed.util.Nullable;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.ContainerSetup;

/**
 *
 */
public class ContainerRunner {

    final ContainerSetup container;

    /** The runtime component node we are building. */
    private ExtensionContext pool;

    /** If the application is stateful, the applications runtime. */
    @Nullable
    public final RegionalManagedLifetime runtime;

    public ContainerRunner(ApplicationSetup application) {
        this.runtime = new RegionalManagedLifetime(this);
        this.container = application.container();
        // application.goal.isLaunchable() && application.driver.lifetimeKind() == OldLifetimeKind.MANAGED ? new
        // PackedManagedLifetime(this) : null;
    }

    /**
     * @return
     */
    public ExtensionContext pool() {
        return pool;
    }

    public void run(RunState state) {
        this.pool = container.lifetime.newRuntimePool();
        runtime.launch(state);
    }

    void shutdown() {
        new StopRunner(container.lifetime.stoppersPre, pool, runtime).start();
    }

    void start() {
        new StartRunner(container.lifetime.startersPre, pool, runtime).start();
    }
}
