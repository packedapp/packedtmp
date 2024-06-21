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
package app.packed.application;

import app.packed.assembly.Assembly;
import app.packed.build.hook.BuildHook;
import app.packed.container.ContainerMirror;

/**
 *
 */
// Jeg ved ikke

public non-sealed abstract class ApplicationBuildHook extends BuildHook {

    // What happens if people applies an ApplicationBuildHook to a non-root assembly
    // Maybe take ApplicationDescriptor, and fail within the method instead of returning boolean
    public boolean failIfAppliedOnNonRootAssembly() {
        return true;
    }

    public void onNew(ApplicationConfiguration configuration) {}
}

class MyA extends ApplicationBuildHook {
    static final ApplicationLocal<String> AS = ApplicationLocal.of();

    /** {@inheritDoc} */
    @Override
    public void onNew(ApplicationConfiguration configuration) {
        AS.get(configuration);
    }

    public void onNew(ContainerMirror configuration) {
        AS.get(configuration);
    }

    public void onNew(Assembly assembly) {
        AS.get(assembly);
    }
}