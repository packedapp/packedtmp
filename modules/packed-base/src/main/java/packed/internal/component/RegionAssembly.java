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
import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.service.ServiceRegistry;
import packed.internal.component.PackedWireableComponentDriver.SingletonComponentDriver;
import packed.internal.container.ContainerAssembly;
import packed.internal.inject.resolvable.ResolvableFactory;
import packed.internal.service.buildtime.ServiceExtensionNode;
import packed.internal.util.ThrowableUtil;

/**
 *
 */
public class RegionAssembly {

    private final ArrayList<SourceAssembly> allSources = new ArrayList<>();

    final ComponentNodeConfiguration configuration; // do we need this??

    private final ArrayList<SourceAssembly> constantSources = new ArrayList<>();

    int index;

    public final ArrayDeque<ResolvableFactory> mustInstantiate = new ArrayDeque<>();

    public final Resolver resolver = new Resolver(this);

    public ServiceExtensionNode services;

    RegionAssembly(ComponentNodeConfiguration node) {
        this.configuration = requireNonNull(node);
    }

    public SourceAssembly addSourced(ComponentNodeConfiguration cnc) {
        SourceAssembly sa = new SourceAssembly(cnc, (SingletonComponentDriver<?>) cnc.driver);
        if (sa.hasInstance()) {
            constantSources.add(sa);
        } else {
            allSources.add(sa);
        }
        return sa;
    }

    public void assemblyClosed() {
        for (SourceAssembly sa : allSources) {
            if (sa.resolvable != null && sa.service == null) {
                ContainerAssembly container = configuration.container;
                ServiceExtensionNode node = container.se;
                System.out.println("Do we have the service extension= " + (node != null));
                sa.resolvable.newMH(this, node.provider());
                mustInstantiate.addLast(sa.resolvable);
            }
        }
        // Write all non-services singletons...

        System.out.println("Closing region");
    }

    Region newRegion(PackedInitializationContext pic, ComponentNode root) {
        Region region = new Region(index);

        if (root.modifiers().isGuest()) {
            region.store(0, new PackedGuest(null));
        }

        // We start by storing all constant sources in the array
        for (SourceAssembly sa : constantSources) {
            region.store(sa.regionIndex, sa.instance());
        }

        for (ResolvableFactory e : mustInstantiate) {
            if (e.regionIndex > -1) {
                MethodHandle mh = e.reducedMha;
                // System.out.println("INST " + mh.type().returnType());
                Object instance;
                try {
                    instance = mh.invoke(region);
                } catch (Throwable e1) {
                    throw ThrowableUtil.orUndeclared(e1);
                }
                region.store(e.regionIndex, instance);
            }
        }

        ContainerAssembly container = configuration.container;

        int registryIndex = root.modifiers().isGuest() ? 1 : 0;
        ServiceExtensionNode node = container.se;
        if (node != null) {
            region.store(registryIndex, node.instantiateEverything(region, pic.wirelets()));
        } else {
            region.store(registryIndex, ServiceRegistry.empty());
        }

        return region;
    }

    public int reserve() {
        return index++;
    }
}
