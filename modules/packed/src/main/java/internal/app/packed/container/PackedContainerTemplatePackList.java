/*
 * Copyright (c) 2026 Kasper Nielsen.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.packed.binding.Key;

/**
 *
 */
// Giver kun meningen hvis man frequent reconfigure...
//Hvad man ikke goer...
public final class PackedContainerTemplatePackList {

    public final List<PackedContainerLink> packs;

    PackedContainerTemplatePackList(List<PackedContainerLink> tunnels) {
        this.packs = List.copyOf(tunnels);
    }

    public PackedContainerTemplatePackList add(PackedContainerLink tunnel) {
        ArrayList<PackedContainerLink> l = new ArrayList<>(packs);
        l.add(tunnel);
        return new PackedContainerTemplatePackList(l);
    }

    /**
     * @return
     */
    public Set<Key<?>> keys() {
        throw new UnsupportedOperationException();
    }
}
