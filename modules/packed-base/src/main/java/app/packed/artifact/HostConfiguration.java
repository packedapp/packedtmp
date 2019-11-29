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
package app.packed.artifact;

import app.packed.component.BaseComponentConfiguration;

/**
 *
 */
public interface HostConfiguration extends BaseComponentConfiguration {

    /** {@inheritDoc} */
    @Override
    HostConfiguration setDescription(String description);

    /** {@inheritDoc} */
    @Override
    HostConfiguration setName(String name);
}

// Ideen er at vi har
// ContainerConfiguration.addHost(Class<?> hostConfiguration);

class AppHostConfiguration {
    public AppHostConfiguration(HostConfiguration configuration) {

    }
}