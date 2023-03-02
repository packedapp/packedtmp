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
import java.util.concurrent.FutureTask;

import app.packed.container.Assembly;

/**
 * Represents the child of an application.
 */

// ParentType = LiveApplication | ApplicationSetup
// ChildType = ApplicationSetup | FutureApplicationSetup

// Kan kun tilfoejes saa laenge en assembly er aaben
public final class ApplicationChild {

    final LeafContainerBuilder parent;

    final FutureTask<ApplicationSetup> task;

    final Assembly assembly;

    // IDK vi skal nok have en specielt builder
    ApplicationChild(LeafContainerBuilder parent, Assembly assembly) {
        this.parent = parent;
        this.assembly = assembly;
        Callable<ApplicationSetup> c = () -> {
            ContainerSetup s = parent.buildFromAssembly(assembly);
            return s.application;
        };
        this.task = new FutureTask<ApplicationSetup>(c);
    }

    // 4 states
    // Configured
    // Building
    // Failed-Building/Cancelled
    // Completed
}
