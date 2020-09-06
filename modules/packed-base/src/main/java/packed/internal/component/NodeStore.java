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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.service.ServiceRegistry;
import packed.internal.util.LookupUtil;
import packed.internal.util.MethodHandleUtil;

/**
 * All strongly connected components relate to the same pod.
 */
// Passive System -> 1 NodeStore
// Active System -> 1 NodeStore per guest
public final class NodeStore {

    static final MethodHandle MH_GET_SINGLETON_INSTANCE = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "getSingletonInstance", Object.class, int.class);

    final Object[] instances; // May contain f.eks. CHM.. ?? Maybe hosts are also there...

    NodeStore(int i) {
        instances = new Object[i];
        // System.out.println("CREATING NEW NODE STORE with room for " + instances.length);
    }

    public PackedGuest getGuest(ComponentNode node) {
        return (PackedGuest) instances[0];
    }

    public ServiceRegistry getServiceRegistry(ComponentNode node) {
        int off = node.modifiers().isGuest() ? 1 : 0;
        return (ServiceRegistry) instances[off];
    }

    public Object getSingletonInstance(int index) {
        return instances[index];
    }

    public void storeGuest(ComponentNode node, PackedGuest guest) {
        instances[0] = guest;
    }

    public void storeServiceRegistry(ComponentNode node, ServiceRegistry registry) {
        int off = node.modifiers().isGuest() ? 1 : 0;
        instances[off] = registry;
    }

    public void storeSingleton(int index, Object instance) {
        instances[index] = instance;
    }

    public static MethodHandle readSingletonAs(int index, Class<?> type) {
        MethodHandle mh = MethodHandles.insertArguments(MH_GET_SINGLETON_INSTANCE, 1, index);
        mh = MethodHandleUtil.castReturnType(mh, type);
        return mh;
    }

    public static final class Assembly {

        int index;

        final ComponentNodeConfiguration root;

        Assembly(ComponentNodeConfiguration node) {
            this.root = requireNonNull(node);
        }

        NodeStore newStore() {
            return new NodeStore(index);
        }

        public int reserve() {
//      new Exception().printStackTrace();
            return index++;
        }

    }

}
//Taenker den er inline
//Skal jo godt nok vaere lille for Actors...

/// GUESTS (

// En guest kunne mere eller mindre vaere 10 objects