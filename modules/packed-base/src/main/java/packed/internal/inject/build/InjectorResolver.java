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
package packed.internal.inject.build;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.inject.InstantiationMode;
import app.packed.util.Key;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.dependencies.DependencyGraph;
import packed.internal.inject.build.service.BSEComponent;
import packed.internal.inject.build.service.ProvideAllFromInjector;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.ServiceNodeMap;

/**
 *
 */

// Nodes [explicit, @Provides, importInjector]
// No node with the same key can be registered

// Exports [explicit, via annotations]
///// Cannot export a service more than oce

// Explicit Requirements [explicit, via contract]
//// Can export many times, but only registered once
//// Mandatory requirements will always override optional requirements

//// Hvordan virker transitive exports???? Det er jo ikke noget vi kan finde ud af med det samme...

/// -------- Graf
// Der skal vaere noder imellem, hvor man kan filtrere

public final class InjectorResolver {

    final InjectorBuilder ib;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap resolvedEntries = new ServiceNodeMap();

    public DefaultInjector privateInjector;

    public DefaultInjector publicInjector;

    public InjectorResolver(InjectorBuilder ib) {
        this.ib = requireNonNull(ib);
    }

    DependencyGraph dg;

    public void build(ArtifactBuildContext buildContext) {
        boolean hasDuplicates = processNodesAndCheckForDublicates(buildContext);

        // Go through all exports, and make sure they can all be fulfilled
        if (ib.exporter != null) {
            ib.exporter.resolve(this, buildContext);
        }

        if (hasDuplicates) {
            return;
        }
        // It does not make sense to try and resolve
        dg = new DependencyGraph(ib.pcc, ib, this);
        dg.analyze(ib.exporter);

        // Instantiate
        if (buildContext.isInstantiating()) {
            for (ServiceEntry<?> node : resolvedEntries) {
                if (node instanceof BSEComponent) {
                    BSEComponent<?> s = (BSEComponent<?>) node;
                    if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                        s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                    }
                }
            }

            // Okay we are finished, convert all nodes to runtime nodes.
            resolvedEntries.toRuntimeNodes();

            if (ib.exporter != null) {
                if (resolvedEntries != ib.exporter.resolvedExports) {
                    ib.exporter.resolvedExports.toRuntimeNodes();
                }
            }
        }
    }

    private boolean processNodesAndCheckForDublicates(ArtifactBuildContext buildContext) {
        HashMap<Key<?>, BuildEntry<?>> uniqueNodes = new HashMap<>();
        LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateNodes = new LinkedHashMap<>(); // preserve order for error message

        processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, ib.provider.entries);
        for (ProvideAllFromInjector ii : ib.provider.provideAll) {
            processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, ii.entries.values());
        }

        // Add error messages if any nodes with the same key have been added multiple times
        if (!duplicateNodes.isEmpty()) {
            ErrorMessages.addDuplicateNodes(buildContext, duplicateNodes);
        }
        resolvedEntries.addAll(uniqueNodes.values());
        return !duplicateNodes.isEmpty();
    }

    private void processNodesAndCheckForDublicates0(HashMap<Key<?>, BuildEntry<?>> uniqueNodes,
            LinkedHashMap<Key<?>, LinkedHashSet<BuildEntry<?>>> duplicateNodes, Iterable<? extends BuildEntry<?>> nodes) {
        for (BuildEntry<?> node : nodes) {
            Key<?> key = node.key();
            if (key != null) {
                BuildEntry<?> existing = uniqueNodes.putIfAbsent(key, node);
                if (existing != null) {
                    HashSet<BuildEntry<?>> hs = duplicateNodes.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set, but add existing first
                    hs.add(node);
                }
            }
        }
    }

}
