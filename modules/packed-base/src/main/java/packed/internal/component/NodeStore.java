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

import java.util.HashMap;
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

    final HashMap<ThingsToStore, Object>[] instances; // May contain f.eks. CHM.. ?? Maybe hosts are also there...
    // If non-root instances[0] always is the parent...

    @SuppressWarnings("unchecked")
    NodeStore(int i) {
        instances = new HashMap[i + 1];
        for (int j = 0; j < instances.length; j++) {
            instances[j] = new HashMap<>();
        }
    }

    public PackedContainer getContainer(ComponentNode node) {
        throw new UnsupportedOperationException();
    }

    public PackedGuest getGuest(ComponentNode node) {
        return (PackedGuest) instances[node.storeOffset].get(ThingsToStore.GUEST);
    }

    public ServiceRegistry getServiceRegistry(ComponentNode node) {
        return (ServiceRegistry) instances[node.storeOffset].get(ThingsToStore.SERVICEREGISTRY);
    }

    public Object getSingletonInstance(int index) {
        return instances[index].get(ThingsToStore.SINGLETON_INSTANCE);
    }

    public Object getSingletonInstance(ComponentNode node) {
        return instances[node.storeOffset].get(ThingsToStore.SINGLETON_INSTANCE);
    }

    public Object getSingletonInstance(ComponentNodeConfiguration node) {
        return instances[node.storeOffset].get(ThingsToStore.SINGLETON_INSTANCE);
    }

    public void storeServiceRegistry(ComponentNode node, ServiceRegistry registry) {
        instances[node.storeOffset].put(ThingsToStore.SERVICEREGISTRY, registry);
    }

    public void storeGuest(ComponentNode node, PackedGuest guest) {
        instances[node.storeOffset].put(ThingsToStore.GUEST, guest);
    }

    public void storeSingleton(ComponentNodeConfiguration node, Object instance) {
        instances[node.storeOffset].put(ThingsToStore.SINGLETON_INSTANCE, instance);
    }

    static final class Assembly {

        int index;

        final ComponentNodeConfiguration root;

        Assembly(ComponentNodeConfiguration node) {
            this.root = requireNonNull(node);
        }

        int reserve(ComponentNodeConfiguration c) {
            int i = 1;
            int current = index;
            index += i;
            return current;
        }

        NodeStore newStore() {
            return new NodeStore(index);
        }
    }

    enum ThingsToStore {
        GUEST, SERVICEREGISTRY, SINGLETON_INSTANCE;
    }

}
//Taenker den er inline
//Skal jo godt nok vaere lille for Actors...

/// GUESTS (

// En guest kunne mere eller mindre vaere 10 objects