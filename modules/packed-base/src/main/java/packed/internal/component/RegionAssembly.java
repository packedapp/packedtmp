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
import java.util.ArrayList;

import app.packed.service.ServiceRegistry;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.Injectable;
import packed.internal.service.InjectionManager;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
// Vi gemmer alt det her i en region...
// Fordi raekkefoelgen af initialisering gaar paa tvaers af containere
// Idet de kan dependende paa hinanden
public final class RegionAssembly {

    /** Components that contains constants that should be stored in a region. */
    final ArrayList<SourceAssembly> runtimeInstances = new ArrayList<>();

    final ComponentNodeConfiguration compConf; // do we need this??

    int nextIndex;

    // List of services that must be instantiated and stored in the region
    // They are ordered in the order they should be initialized
    // For now written by DependencyCycleDetector via BFS
    public final ArrayList<Injectable> constantServices = new ArrayList<>();

    /** Everything that needs to be injected and store, but which is not a service. */
    final ArrayList<SourceAssembly> sourceInjectables = new ArrayList<>();

    /*---***************************/

    // Taenker den her er paa injection manager
    public final ArrayList<Injectable> allInjectables = new ArrayList<>();

    RegionAssembly(ComponentNodeConfiguration compConf) {
        this.compConf = requireNonNull(compConf);
    }

    public void assemblyClosed() {
        InjectionManager se = compConf.injectionManager();
        se.buildTree(this);
    }

    // Vi bliver noedt til at kalde ned recursivt saa vi kan finde raekkefolgen af service inst

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        Region region = new Region(nextIndex);

        // I don't now if we create the guest here??? We do for now though
        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constants in the region array
        for (SourceAssembly sa : runtimeInstances) {
            region.store(sa.regionIndex, sa.instance);
        }

        // All services that must be instantiated and stored
        for (Injectable ii : constantServices) {
            int index = ii.entry().regionIndex();

            // Should never have been added if index==-1
            if (index > -1) {
                requireNonNull(ii);
                Object instance;
                MethodHandle mh = ii.buildMethodHandle();
                try {
                    instance = mh.invoke(region);
                } catch (Throwable e1) {
                    throw ThrowableUtil.orUndeclared(e1);
                }
                requireNonNull(instance);
                region.store(index, instance);
            }
        }

        // Last all singletons that have not already been used as services
        for (SourceAssembly i : sourceInjectables) {
            if (i.regionIndex > -1 && !region.isSet(i.regionIndex)) {
                Object instance;
                MethodHandle mh = i.injectable.buildMethodHandle();
                try {
                    instance = mh.invoke(region);
                } catch (Throwable e1) {
                    throw ThrowableUtil.orUndeclared(e1);
                }
                requireNonNull(instance);
                region.store(i.regionIndex, instance);
            }
        }

        ContainerAssembly container = compConf.memberOfContainer;

        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
        InjectionManager node = container.im;
        // Move this to lazy create via PIC
        // And no need to store this is the region
        if (node != null) {
            region.store(registryIndex, node.newServiceRegistry(root, region, pic.wirelets()));
        } else {
            region.store(registryIndex, ServiceRegistry.empty());
        }

        return region;
    }

    public int reserve() {
        return nextIndex++;
    }
}
