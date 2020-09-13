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
import packed.internal.service.buildtime.BuildtimeService;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.service.AtProvideBuildEntry;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public final class RegionAssembly {

    public final ArrayList<Injectable> allInjectables = new ArrayList<>();

    final ComponentNodeConfiguration compConf; // do we need this??

    public final ArrayList<Injectable> constantServices = new ArrayList<>();

    int nextIndex;

    /** Components that contains constants that should be stored in a region. */
    final ArrayList<SourceAssembly> runtimeInstances = new ArrayList<>();

    /** Everything that needs to resolved. */
    public final ArrayList<SourceAssembly> sourceInjectables = new ArrayList<>();

    RegionAssembly(ComponentNodeConfiguration compConf) {
        this.compConf = requireNonNull(compConf);
    }

    public void assemblyClosed() {
        InjectionManager se = compConf.container.im;

        se.buildTree(this);
    }

    // Vi bliver noedt til at kalde ned recursivt saa vi kan finde raekkefolgen af service inst

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        Region region = new Region(nextIndex);

        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constants in the region array
        for (SourceAssembly sa : runtimeInstances) {
            region.store(sa.regionIndex, sa.instance);
        }

        for (Injectable ii : constantServices) {
            // System.out.println(ii.directMethodHandle);
            int index;
            BuildtimeService<?> entry = ii.entry();
            if (entry instanceof AtProvideBuildEntry<?>) {
                AtProvideBuildEntry<?> e = (AtProvideBuildEntry<?>) entry;
                index = e.regionIndex;
            } else {
                index = ii.source.regionIndex;
            }
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
            if (i.regionIndex > -1) {
                if (!region.isSet(i.regionIndex)) {
                    Object instance;
                    try {
                        instance = i.injectable.buildMethodHandle().invoke(region);
                    } catch (Throwable e1) {
                        throw ThrowableUtil.orUndeclared(e1);
                    }
                    requireNonNull(instance);
                    region.store(i.regionIndex, instance);
                }
            }
        }

        ContainerAssembly container = compConf.container;

        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
        InjectionManager node = container.im;
        // Move this to lazy create via PIC
        // And no need to store this is the region
        if (node != null) {
            region.store(registryIndex, node.newServiceRegistry(region, pic.wirelets()));
        } else {
            region.store(registryIndex, ServiceRegistry.empty());
        }

        return region;
    }

    public int reserve() {
        return nextIndex++;
    }
}
