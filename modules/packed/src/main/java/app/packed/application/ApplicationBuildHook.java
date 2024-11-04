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
public non-sealed abstract class ApplicationBuildHook extends BuildHook {

    // Maybe take ApplicationDescriptor, and fail within the method instead of returning boolean
    // Then people call also just print a warning.
    /**
     * By default application build hooks can only be used on the root assembly of an application. This method can be used
     * override this behaviour by return {@code false}. Which will allow the application build book to used on non-root
     * assemblies.
     * <p>
     * Will throw a {@link app.packed.build.BuildException} with "This application build hook" can only be used on an
     * application's root assembly"
     *
     * @return
     */
    public boolean failIfAppliedOnNonRootAssembly() {
        return true;
    }

    public void onNew(ApplicationConfiguration configuration) {}

    // Vil mene det er den sidste der bliver kaldt.
    // Den vil dog trigger fx BeanBuildHook.
    public void onClosing(ApplicationConfiguration configuration) {}
}

class MyA extends ApplicationBuildHook {
    static final ApplicationBuildLocal<String> AS = ApplicationBuildLocal.of();

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