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
import java.util.Map;

import app.packed.service.ServiceRegistry;
import packed.internal.container.ContainerAssembly;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.service.runtime.PackedInjector;

/**
 *
 */
public class RegionAssembly {

    int index;

    final ComponentNodeConfiguration root; // do we need this??

    final ArrayList<SourceAssembly> sources = new ArrayList<>();

    RegionAssembly(ComponentNodeConfiguration node) {
        this.root = requireNonNull(node);
    }

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        ContainerAssembly container = this.root.container;

        Region reg = new Region(index);
        for (SourceAssembly sa : sources) {
            sa.initSource(reg);
        }
        if (root.modifiers().isGuest()) {
            reg.store[0] = new PackedGuest(null);
        }
        ServiceRegistry registry = null;
        ServiceExtensionNode node = container.se;
        if (node != null) {
            registry = node.instantiateEverything(reg, pic.wirelets());
        } else {
            registry = new PackedInjector(root.configSite(), Map.of());
        }
        int off = root.modifiers().isGuest() ? 1 : 0;
        reg.store[off] = registry;

        return reg;
    }

    public int reserve() {
        return index++;
    }
}
