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

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import app.packed.assembly.Assembly;
import app.packed.build.BuildException;
import app.packed.build.BuildProcess;
import app.packed.component.ComponentConfiguration;

/**
 *
 */
// By default it is configuration everywhere..
// Maybe have a freeze()/protect() operation/

public non-sealed class ApplicationConfiguration extends ComponentConfiguration implements ApplicationBuildLocal.Accessor {

    List<Class<? extends Assembly>> allowedAssemblies = List.of();
    // matcher

    /** The application's handle. */
    private final ApplicationHandle<?, ?> handle;

    public ApplicationConfiguration(ApplicationHandle<?, ?> handle) {
        this.handle = requireNonNull(handle);
    }

    protected final void checkUpdatable() {
        checkIsConfigurable();
        Optional<Class<? extends Assembly>> current = BuildProcess.current().currentAssembly();

        if (current.isEmpty()) {
            return;
        }
        Class<? extends Assembly> cl = current.get();
        if (allowedAssemblies.isEmpty()) {
            return;
        }
        for (Class<? extends Assembly> c : allowedAssemblies) {
            if (c.isAssignableFrom(cl)) {
                return;
            }
        }
        throw new BuildException("This operation can only be called from assemblies of type " + allowedAssemblies + ", current assembly = " + cl);
    }

    // isConfigurable?? Models

    // Root assembly defines this, and is sharable between all assemblies

    // Per assembly, requires that we can create new application configurations.
    // when needed

    /** {@inheritDoc} */
    @Override
    public ApplicationConfiguration componentTag(String... tags) {
        handle.componentTag(tags);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    protected final ApplicationHandle<?, ?> handle() {
        return handle;
    }

    /**
     * If no name is set, uses the name of the root container
     *
     * @param name
     */
    public void named(String name) {
        checkUpdatable();
        System.out.println("Setting name");
    }

    @SafeVarargs
    public final void restrictUpdatesTo(Class<? extends Assembly>... assemblies) {
        checkUpdatable();
        this.allowedAssemblies = List.of(assemblies);
    }

    public final void restrictUpdatesToThisAssembly() {
        allowedAssemblies = List.of(BuildProcess.current().currentAssembly().get());
    }
}
