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
import app.packed.component.ComponentType;
import app.packed.container.ContainerSource;
import app.packed.container.Wirelet;

/**
 *
 */
// Behoever altsaa ikke vaere et interfaace...
// Det er
public interface HostConfigurationContext extends BaseComponentConfiguration {

    void deploy(ContainerSource source, ArtifactDriver<?> driver, Wirelet... wirelets);
    // deploy permanently...

    /** {@inheritDoc} */
    @Override
    HostConfigurationContext setDescription(String description);

    /** {@inheritDoc} */
    @Override
    HostConfigurationContext setName(String name);

    /** {@inheritDoc} */
    @Override
    default ComponentType type() {
        return ComponentType.HOST;
    }
}