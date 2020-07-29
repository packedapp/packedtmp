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
package packed.internal.component;

import app.packed.base.Nullable;
import app.packed.component.Bundle;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Wirelet;
import packed.internal.artifact.PackedInstantiationContext;
import packed.internal.container.PackedContainer;
import packed.internal.container.PackedContainerConfigurationContext;

/**
 *
 */
public abstract class PackedComponentDriver<C> implements ComponentDriver<C> {

    // boolean retainAtRuntime()

    public abstract PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic);

    public PackedComponentConfigurationContext newConfiguration(PackedComponentConfigurationContext parent, Bundle<?> bundle, Wirelet... wirelets) {
        return new PackedContainerConfigurationContext(this, parent, bundle, wirelets);
    }

    public static DefaultComponentDriver defaultComp() {
        return new DefaultComponentDriver();
    }

    public static ContainerComponentDriver container() {
        return new ContainerComponentDriver();
    }

    public static class DefaultComponentDriver extends PackedComponentDriver<ComponentConfiguration> {

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedComponent(parent, configuration, ic);
        }
    }

    public static class ContainerComponentDriver extends PackedComponentDriver<ContainerConfiguration> {

        /** {@inheritDoc} */
        @Override
        public PackedComponent create(@Nullable PackedComponent parent, PackedComponentConfigurationContext configuration, PackedInstantiationContext ic) {
            return new PackedContainer(parent, (PackedContainerConfigurationContext) configuration, ic);
        }
    }
}
