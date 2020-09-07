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

import java.util.ArrayList;

import app.packed.service.ServiceRegistry;
import packed.internal.container.ContainerAssembly;
import packed.internal.service.buildtime.ServiceExtensionNode;

/**
 *
 */
public class RegionAssembly {

    int index;

    final ComponentNodeConfiguration configuration; // do we need this??

    private final ArrayList<SourceAssembly> constantSources = new ArrayList<>();

    private final ArrayList<SourceAssembly> sources = new ArrayList<>();

    RegionAssembly(ComponentNodeConfiguration node) {
        this.configuration = requireNonNull(node);
    }

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        Region region = new Region(index);

        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constant sources in the array
        for (SourceAssembly sa : constantSources) {
            region.store(sa.singletonIndex, sa.instance());
        }

        for (SourceAssembly sa : sources) {
            System.out.println("SDSDS");
            sa.initSource(region);
        }

        ContainerAssembly container = configuration.container;

        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
        ServiceExtensionNode node = container.se;
        if (node != null) {
            region.store(registryIndex, node.instantiateEverything(region, pic.wirelets()));
        } else {
            region.store(registryIndex, ServiceRegistry.empty());
        }

        return region;
    }

    public SourceAssembly addSourced(ComponentNodeConfiguration cnc) {
        SourceAssembly sa = new SourceAssembly(cnc, cnc.driver);
        if (sa.hasInstance()) {
            constantSources.add(sa);
        }
        return sa;
    }

    public void close() {
        System.out.println("Closing region");
    }

    public int reserve() {
        return index++;
    }
}
