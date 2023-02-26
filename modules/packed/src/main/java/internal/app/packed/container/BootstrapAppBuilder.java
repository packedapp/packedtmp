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

import java.util.List;

import app.packed.application.BootstrapApp;
import app.packed.application.BuildGoal;
import app.packed.container.AbstractComposer.ComposerAssembly;
import app.packed.extension.container.ContainerTemplate;
import app.packed.lifetime.LifetimeKind;

/**
 *
 */
public final class BootstrapAppBuilder extends AbstractContainerBuilder {

    private static final ContainerTemplate TEMPLATE = new PackedContainerTemplate(PackedContainerKind.PREMORDIAL, BootstrapApp.class, List.of());

    /**
     * @param template
     */
    public BootstrapAppBuilder() {
        super(TEMPLATE);
    }

    /** {@inheritDoc} */
    @Override
    public BuildGoal goal() {
        return BuildGoal.LAUNCH_NOW;
    }

    public void build(ComposerAssembly<?> assembly) {
        AssemblySetup as = new AssemblySetup(this, assembly);
        as.build();
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeKind lifetimeKind() {
        return LifetimeKind.STATELESS;
    }
}
