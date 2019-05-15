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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.UUID;

import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;

/**
 *
 */
public final class ComponentBuildNode {

    /** The configuration of the container that this component has been installed into. */
    final DefaultContainerConfiguration containerConfiguration;

    /** The description of the component */
    @Nullable
    String description;

    Object instance;

    /** The name of the component */
    @Nullable
    String name;

    BuildtimeServiceNode<?> serviceNode;

    /** The configuration site of the component. */
    final InternalConfigurationSite site;

    ComponentBuildNode(InternalConfigurationSite site, DefaultContainerConfiguration containerConfiguration) {
        this.site = requireNonNull(site);
        this.containerConfiguration = requireNonNull(containerConfiguration);
    }

    public void onFreeze() {
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        containerConfiguration.components.put(name, this);
    }
}
