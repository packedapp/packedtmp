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
package packed.internal.inject.buildnodes;

import packed.internal.inject.InternalInjectorConfiguration;
import packed.internal.util.configurationsite.InternalConfigurationSite;

/**
 * A build node for a component instance.
 */
public class BuildNodeComponentFactory<T> extends BuildNodeInstance<T> {

    /**
     * @param injectorConfiguration
     * @param configurationSite
     * @param instance
     */
    public BuildNodeComponentFactory(InternalInjectorConfiguration injectorConfiguration, InternalConfigurationSite configurationSite, T instance) {
        super(injectorConfiguration, configurationSite, instance);
    }

}

// public BuildNodeInstance(InternalInjectorConfiguration bundle, T instance, RegistrationPoint frame,

/// ** The component configuration, if created via an install functions. */
// private final InternalComponentConfiguration<?> componentConfiguration;// not <T> because of mixins
//
/// ** The component instance id. or -1 if not a component instance. */
// private final int componentInstanceId;
// InternalComponentConfiguration<?> componentConfiguration, int componentInstanceId) {
// super(bundle, List.of(), frame);
// this.instance = instance;
// this.componentConfiguration = requireNonNull(componentConfiguration);
// this.componentInstanceId = componentInstanceId;
// }

// public InternalComponentConfiguration<?> getComponentConfiguration() {
// return componentConfiguration;
// }
//
// public int getComponentInstanceId() {
// return componentInstanceId;
// }
