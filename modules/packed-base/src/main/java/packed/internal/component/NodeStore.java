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

import java.util.concurrent.ConcurrentHashMap;

import app.packed.service.ServiceRegistry;

/**
 * All strongly connected components relate to the same pod.
 */
// Passive System -> 1 NodeStore
// Active System -> 1 NodeStore per guest
public final class NodeStore {

    RuntimeComponentModel[] descriptors;// packed descriptors...

    ConcurrentHashMap<Integer, NodeStore>[] hosts;

    final Object[] instances; // May contain f.eks. CHM.. ?? Maybe hosts are also there...
    // If non-root instances[0] always is the parent...

    NodeStore(int i) {
        instances = new Object[i];
    }

    enum ThingsToStore {

    }

    public Object getSingletonInstance(ComponentNode node) {
        throw new UnsupportedOperationException();
    }

    public PackedGuest getGuest(ComponentNode node) {
        throw new UnsupportedOperationException();
    }

    public PackedContainer getContainer(ComponentNode node) {
        throw new UnsupportedOperationException();
    }

    public ServiceRegistry getServiceRegistry(ComponentNode node) {
        return (ServiceRegistry) instances[node.storeOffset];
    }

    public void store(ComponentNode node, ServiceRegistry registry) {
        instances[node.storeOffset] = registry;
    }

    static final class Assembly {

        int index;

        final ComponentNodeConfiguration root;

        /** The pod used at runtime. */
        private NodeStore store;

        Assembly(ComponentNodeConfiguration node) {
            this.root = requireNonNull(node);
        }

        int reserve(ComponentNodeConfiguration c) {
            int i = 1;
            if (store != null) {
                throw new IllegalStateException();
            }
            int current = index;
            index += i;
            return current;
        }

        NodeStore store() {
            NodeStore s = store;
            if (s == null) {
                s = store = new NodeStore(index);
            }
            return s;
        }
    }

}
//Taenker den er inline
//Skal jo godt nok vaere lille for Actors...

/// GUESTS (

// En guest kunne mere eller mindre vaere 10 objects