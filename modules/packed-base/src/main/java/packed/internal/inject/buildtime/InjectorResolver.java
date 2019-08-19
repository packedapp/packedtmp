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
package packed.internal.inject.buildtime;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import app.packed.artifact.ArtifactBuildContext;
import app.packed.inject.InjectionException;
import app.packed.inject.InjectorContract;
import app.packed.inject.ServiceDependency;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.runtime.DefaultInjector;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

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

final class InjectorResolver {

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    final ServiceNodeMap exports = new ServiceNodeMap();

    /** A map of multiple exports for the same key. */
    @Nullable
    Map<Key<?>, ArrayList<BSNExported<?>>> exportsDuplicates;

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BSN<?>, List<ServiceDependency>> unresolvedDependencies;

    DefaultInjector privateInjector;

    DefaultInjector publicInjector;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    final ServiceNodeMap nodes = new ServiceNodeMap();

    final InjectorBuilder ib;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<Entry<BSN<?>, ServiceDependency>> missingDependencies;

    InjectorResolver(InjectorBuilder ib) {
        this.ib = requireNonNull(ib);
    }

    void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BSN<?>, ServiceDependency> e : missingDependencies) {
                if (!e.getValue().isOptional() && ib.manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    List<InternalDependencyDescriptor> dependencies = e.getKey().dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    ServiceDependency dependency = e.getValue();
                    sb.append("parameter on ");
                    if (dependency.variable() != null) {

                        InternalExecutableDescriptor ed = (InternalExecutableDescriptor) ((InternalParameterDescriptor) dependency.variable().get())
                                .declaringExecutable();
                        sb.append(ed.descriptorTypeName()).append(": ");
                        sb.append(ed.getDeclaringClass().getCanonicalName());
                        if (ed instanceof MethodDescriptor) {
                            sb.append("#").append(((MethodDescriptor) ed).getName());
                        }
                        sb.append("(");
                        if (dependencies.size() > 1) {
                            StringJoiner sj = new StringJoiner(", ");
                            for (int j = 0; j < dependencies.size(); j++) {
                                if (j == dependency.parameterIndex().getAsInt()) {
                                    sj.add("-> " + dependency.key().toString() + " <-");
                                } else {
                                    sj.add(dependencies.get(j).key().typeLiteral().rawType().getSimpleName());
                                }
                            }
                            sb.append(sj.toString());
                        } else {
                            sb.append(dependency.key().toString());
                            sb.append(" ");
                            sb.append(dependency.variable().get().getName());
                        }
                        sb.append(")");
                    }
                    // b.root.requiredServicesMandatory.add(e.get)
                    // System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
                    throw new InjectionException(sb.toString());
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    void build(ArtifactBuildContext buildContext) {

        // First process all our build nodes, making sure we have no overlap
        HashMap<Key<?>, BSN<?>> uniqueNodes = new HashMap<>();
        HashMap<Key<?>, HashSet<BSN<?>>> dublicateNodes = new HashMap<>();
        for (BSN<?> node : ib.nodes) {
            requireNonNull(node.key);
            BSN<?> existing = uniqueNodes.putIfAbsent(node.key, node);
            if (existing != null) {
                HashSet<BSN<?>> hs = dublicateNodes.computeIfAbsent(node.key, m -> new HashSet<>());
                hs.add(existing); // might be added multiple times, hence we use a Set
                hs.add(node);
            }
        }
        if (!dublicateNodes.isEmpty()) {
            // Here we want to add messages...
            // Maybe just
            throw new IllegalStateException("OOPS");
        }
        nodes.addAll(uniqueNodes.values());

        // Go through all exported nodes, and make sure they can all be fullfilled

        HashMap<Key<?>, HashSet<BSN<?>>> unresolvedExports = new HashMap<>();
        for (BSNExported<?> node : ib.exportedNodes) {
            if (node.exportOf == null) {
                ServiceNode<?> sn = nodes.getRecursive(node.getKey());
                if (sn == null) {
                    unresolvedExports.computeIfAbsent(node.key, m -> new HashSet<>()).add(node);
                }
                node.exportOf = (ServiceNode) sn;
                exports.put(node);
            }
        }
        if (!unresolvedExports.isEmpty()) {
            throw new IllegalStateException("Could not find nodes to export for: " + unresolvedExports.keySet());
        }

        DependencyGraph dg = new DependencyGraph(ib.container, ib);
        if (buildContext.isInstantiating()) {
            dg.instantiate();
        } else {
            dg.analyze();
        }
    }

    public void recordMissingDependency(BSN<?> node, ServiceDependency dependency, boolean fromParent) {

    }

    public void buildContract(InjectorContract.Builder builder) {
        if (requiredOptionally != null) {
            requiredOptionally.forEach(k -> {
                // We remove all optional dependencies that are also mandatory.
                if (required == null || !required.contains(k)) {
                    builder.addOptional(k);
                }
            });
        }
        if (required != null) {
            required.forEach(k -> builder.addRequires(k));
        }
    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param node
     * @param dependency
     */
    public void recordResolvedDependency(BSN<?> node, ServiceDependency dependency, @Nullable ServiceNode<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<BSN<?>, ServiceDependency>> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new SimpleImmutableEntry<>(node, dependency));

        if (!ib.manualRequirementsManagement) {
            if (dependency.isOptional()) {
                requiredOptionally.add(dependency.key());
            } else {
                required.add(dependency.key());
            }
        }
    }

}
