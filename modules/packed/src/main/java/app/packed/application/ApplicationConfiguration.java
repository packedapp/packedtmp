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

import java.util.function.Consumer;

import app.packed.assembly.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentPath;
import internal.app.packed.container.ApplicationSetup;

/**
 *
 */
// By default it is configuration everywhere..
// Maybe have a freeze()/protect() operation/

public non-sealed class ApplicationConfiguration implements ComponentConfiguration, ApplicationLocalAccessor {

    ApplicationSetup application;

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return application.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public ComponentConfiguration componentTag(String... tags) {
        return null;
    }

    // isConfigurable?? Models

    // Root assembly defines this, and is sharable between all assemblies

    // Per assembly, requires that we can create new application configurations.
    // when needed

    /** {@inheritDoc} */
    @Override
    public boolean isConfigurable() {
        throw new UnsupportedOperationException();
    }

    /**
     * If no name is set, uses the name of the root container
     *
     * @param name
     */
    public void named(String name) {

    }


    public void allowAll() {}

    // matcher

    // replace with AssemblyModel (or AssemblyDescriptor)
    public void allowAll(Consumer<? super Class<? extends Assembly>> c) {}
}
