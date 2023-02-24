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
package internal.app.packed.application;

import app.packed.application.BuildGoal;
import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import internal.app.packed.container.AbstractContainerBuilder;
import internal.app.packed.container.AssemblySetup;

/**
 * Used by {@link BootstrapApp} to build applications.
 */
public final class RootApplicationBuilder extends AbstractContainerBuilder {

    private final BuildGoal goal;

    final AppSetup ba;

    public RootApplicationBuilder(AppSetup ba, BuildGoal goal) {
        this.ba = ba;
        this.goal = goal;
    }

    public ApplicationSetup build(Assembly assembly, Wirelet[] wirelets) {
        // Build the application
        AssemblySetup as = new AssemblySetup(ba, goal, null, assembly, wirelets);
        as.build();

        // return the application we just build.
        return as.application;
    }
}
