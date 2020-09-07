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
public final class Region {

    static final MethodHandle MH_GET_SINGLETON_INSTANCE = LookupUtil.mhVirtualSelf(MethodHandles.lookup(), "getSingletonInstance", Object.class, int.class);

    final Object[] store; // May contain f.eks. CHM.. ?? Maybe hosts are also there...

    Region(int i) {
        store = new Object[i];
    }

    PackedGuest guest() {
        return (PackedGuest) store[0];
    }

    ServiceRegistry serviceRegistry(ComponentNode node) {
        return (ServiceRegistry) store[node.modifiers().isGuest() ? 1 : 0];
    }

    public Object getSingletonInstance(int index) {
        return store[index];
    }

    public void storeSingleton(int index, Object instance) {
        store[index] = instance;
    }

    public static MethodHandle readSingletonAs(int index, Class<?> type) {
        MethodHandle mh = MethodHandles.insertArguments(MH_GET_SINGLETON_INSTANCE, 1, index);
        mh = MethodHandleUtil.castReturnType(mh, type);
        return mh;
    }

}
//Taenker den er inline
//Skal jo godt nok vaere lille for Actors...

/// GUESTS (

// En guest kunne mere eller mindre vaere 10 objects