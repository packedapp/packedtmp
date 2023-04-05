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
package internal.app.packed.container;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import app.packed.application.BuildException;
import app.packed.container.Assembly;

/**
 * Represents an application that can be build lazily or in another thread.
 */

// Vi har brug for at representere an application der enten er bygget.
// Eller ikke faerdig bygget

// ParentType = LiveApplication | ApplicationSetup
// ChildType = ApplicationSetup | FutureApplicationSetup

// Kan kun tilfoejes saa laenge en assembly er aaben
public final class FutureApplicationSetup {

    /** The result of a successful application build. */
    private volatile ApplicationSetup application;

    final NonBootstrapContainerBuilder parent;

    /** A task for building the application. */
    private final FutureTask<ApplicationSetup> buildTask;

    // IDK vi skal nok have en specielt builder
    public FutureApplicationSetup(NonBootstrapContainerBuilder parent, Assembly assembly) {
        this.parent = parent;

        Callable<ApplicationSetup> c = () -> {
            ContainerSetup s = parent.buildNow(assembly);
            return application = s.application;
        };
        this.buildTask = new FutureTask<ApplicationSetup>(c);
    }

    public Future.State state() {
        return buildTask.state();
    }

    /**
     * Lazily builds the application.
     *
     * @return the application
     * @throws BuildException
     *             if the application failed to build
     */
    public ApplicationSetup lazyBuild() {
        ApplicationSetup a = application;
        if (a == null) {
            buildTask.run();
            try {
                a = buildTask.get();
            } catch (InterruptedException e) {
                throw new BuildException("Application build was interrupted", e);
            } catch (ExecutionException e) {
                throw new BuildException("Application failed to build lazily", e.getCause());
            }
        }
        return a;
    }

    // 4 states
    // Configured
    // Building
    // Failed-Building/Cancelled
    // Completed
}
