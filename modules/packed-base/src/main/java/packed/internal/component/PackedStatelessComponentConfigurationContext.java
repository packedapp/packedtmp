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

import static java.util.Objects.requireNonNull;

import app.packed.component.ComponentDescriptor;
import app.packed.component.StatelessConfiguration;
import app.packed.config.ConfigSite;

/** The default implementation of {@link StatelessConfiguration}. */
public final class PackedStatelessComponentConfigurationContext extends PackedComponentConfigurationContext {

    private final ComponentModel componentModel;

    public PackedStatelessComponentConfigurationContext(ConfigSite configSite, PackedComponentConfigurationContext parent, ComponentModel componentModel) {
        super(PackedComponentDriver.defaultComp(), ComponentDescriptor.STATELESS, configSite, parent);
        this.componentModel = requireNonNull(componentModel);
    }

    @Override
    protected String initializeNameDefaultName() {
        return componentModel.defaultPrefix();
    }

    public PackedStatelessComponentConfigurationContext runHooks(Object source) {
        componentModel.invokeOnHookOnInstall(source, this);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfigurationContext setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PackedStatelessComponentConfigurationContext setName(String name) {
        super.setName(name);
        return this;
    }

    public Class<?> definition() {
        return componentModel.type();
    }
}
