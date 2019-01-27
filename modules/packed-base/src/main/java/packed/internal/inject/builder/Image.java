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
package packed.internal.inject.builder;

import java.util.ArrayList;
import java.util.HashSet;

import app.packed.bundle.Bundle;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.config.site.InternalConfigurationSite;
import packed.internal.inject.ServiceNodeMap;

/**
 *
 */
public final class Image {

    /** All nodes that have been added to this builder, even those that are not exposed. */
    ServiceBuildNode<?> privateLatestNode;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    final ServiceNodeMap privateNodeMap;

    @Nullable
    final ArrayList<ServiceBuildNodeExposed<?>> publicNodeList;

    /** The runtime nodes that will be available in the injector. */
    final ServiceNodeMap publicNodeMap;

    HashSet<Key<?>> requiredServicesMandatory;

    HashSet<Key<?>> requiredServicesOptionally;

    /**
     * Creates a new builder.
     * 
     * @param configurationSite
     *            the configuration site
     */
    public Image(InternalConfigurationSite configurationSite) {
        publicNodeMap = privateNodeMap = new ServiceNodeMap();
        publicNodeList = null;
    }

    public Image(InternalConfigurationSite configurationSite, Bundle bundle) {
        publicNodeMap = new ServiceNodeMap();
        privateNodeMap = new ServiceNodeMap();
        publicNodeList = new ArrayList<>();
    }
}
