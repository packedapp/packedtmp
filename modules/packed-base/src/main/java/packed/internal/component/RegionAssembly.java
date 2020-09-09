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

import app.packed.service.ServiceRegistry;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.resolvable.Injectable;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.InjectionManager;
import packed.internal.service.buildtime.service.ProvideBuildEntry;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class RegionAssembly {

    final ComponentNodeConfiguration configuration; // do we need this??

    int nextIndex;

    public final Resolver resolver = new Resolver(this);

    RegionAssembly(ComponentNodeConfiguration node) {
        this.configuration = requireNonNull(node);
    }

    public void assemblyClosed() {
        resolver.resolveAll();

        System.out.println("Closing region");
    }

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        Region region = new Region(nextIndex);

        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constants in the region array
        for (SourceAssembly sa : resolver.sourceConstants) {
            region.store(sa.regionIndex, sa.constant());
        }

        for (Injectable ii : resolver.constantServices) {
            int index;
            BuildEntry<?> entry = ii.entry();
            if (entry instanceof ProvideBuildEntry<?>) {
                ProvideBuildEntry<?> e = (ProvideBuildEntry<?>) entry;
                index = e.regionIndex;
            } else {
                index = ii.source.regionIndex;
            }
            requireNonNull(ii);
            Object instance;
            MethodHandle mh = ii.buildMethodHandle();
            try {
                instance = mh.invoke(region);
            } catch (Throwable e1) {
                throw ThrowableUtil.orUndeclared(e1);
            }
            region.store(index, instance);
        }

        // Last all singletons that have not already been used as services
        for (SourceAssembly i : resolver.sourceInjectables) {
            if (!region.isSet(i.regionIndex)) {
                Object instance;
                try {
                    instance = i.injectable.buildMethodHandle().invoke(region);
                } catch (Throwable e1) {
                    throw ThrowableUtil.orUndeclared(e1);
                }
                region.store(i.regionIndex, instance);
            }
        }

        ContainerAssembly container = configuration.container;

        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
        InjectionManager node = container.im;
        if (node != null) {
            region.store(registryIndex, node.instantiateEverything(region, pic.wirelets()));
        } else {
            region.store(registryIndex, ServiceRegistry.empty());
        }

        return region;
    }

    public int reserve() {
        return nextIndex++;
    }
}
