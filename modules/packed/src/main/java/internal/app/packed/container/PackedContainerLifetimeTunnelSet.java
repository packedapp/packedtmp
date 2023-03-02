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
package internal.app.packed.container;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import app.packed.util.Key;

/**
 *
 */
public final class PackedContainerLifetimeTunnelSet {

    public final List<PackedContainerLifetimeTunnel> tunnels;

    PackedContainerLifetimeTunnelSet(List<PackedContainerLifetimeTunnel> tunnels) {
        this.tunnels = List.copyOf(tunnels);
    }

    public PackedContainerLifetimeTunnelSet add(PackedContainerLifetimeTunnel tunnel) {
        ArrayList<PackedContainerLifetimeTunnel> l = new ArrayList<>(tunnels);
        l.add(tunnel);
        return new PackedContainerLifetimeTunnelSet(l);
    }

    /**
     * @return
     */
    public Set<Key<?>> keys() {
        throw new UnsupportedOperationException();
    }
}
