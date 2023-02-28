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

import app.packed.container.Assembly;
import app.packed.container.Wirelet;
import app.packed.lifetime.LifetimeKind;
import app.packed.util.Nullable;
import internal.app.packed.container.AbstractContainerBuilder;
import internal.app.packed.container.ApplicationSetup;
import internal.app.packed.container.AssemblySetup;

/**
 * Used by {@link BootstrapApp} to build applications.
 */
final class BootstrapAppApplicationBuilder extends AbstractContainerBuilder {

    final BootstrapAppApplicationSetup ba;

    /** The build goal. */
    private final BuildGoal goal;

    BootstrapAppApplicationBuilder(BootstrapAppApplicationSetup ba, BuildGoal goal) {
        super(ba.template);
        this.ba = ba;
        this.goal = goal;
        super.applicationMirrorSupplier = ba.mirrorSupplier;
    }

    ApplicationSetup build(Assembly assembly, Wirelet[] wirelets) {
        // Process any wirelets that were specified
        processWirelets(wirelets);

        // Build the application
        AssemblySetup as = build(assembly);

        // return the application we just build.
        return as.container.application;
    }

    /** {@inheritDoc} */
    @Override
    public BuildGoal goal() {
        return goal;
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeKind lifetimeKind() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected @Nullable Wirelet prefix() {
        return ba.wirelet;
    }
}
