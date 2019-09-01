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
package packed.internal.inject.compose;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BSE;
import packed.internal.inject.build.BSEExported;
import packed.internal.inject.build.ImportedInjector;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.inject.util.ServiceNodeMap;
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

public final class InjectorResolver {

    final ServiceNodeMap exportedNodes = new ServiceNodeMap();

    /** A map of multiple exports for the same key. */
    @Nullable
    Map<Key<?>, ArrayList<BSEExported<?>>> exportsDuplicates;

    final InjectorBuilder ib;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap internalNodes = new ServiceNodeMap();

    /** A list of all dependencies that have not been resolved */
    private ArrayList<Entry<BSE<?>, ServiceDependency>> missingDependencies;

    DefaultInjector privateInjector;

    public DefaultInjector publicInjector;

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BSE<?>, List<ServiceDependency>> unresolvedDependencies;

    public InjectorResolver(InjectorBuilder ib) {
        this.ib = requireNonNull(ib);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void build(ArtifactBuildContext buildContext) {
        boolean hasDuplicates = processNodesAndCheckForDublicates(buildContext);

        // Go through all exports, and make sure they can all be fulfilled
        HashMap<Key<?>, HashSet<BSE<?>>> unresolvedExports = new HashMap<>();
        for (BSEExported<?> node : ib.exportedEntries) {
            if (node.entryToExport == null) {
                ServiceEntry<?> sn = internalNodes.getRecursive(node.getKey());
                if (sn == null) {
                    unresolvedExports.computeIfAbsent(node.key(), m -> new HashSet<>()).add(node);
                }
                node.entryToExport = (ServiceEntry) sn;
                exportedNodes.put(node);
            }
        }

        if (!unresolvedExports.isEmpty()) {
            ErrorMessages.addUnresolvedExports(buildContext, unresolvedExports);
        }
        // It does not make sense to try and resolve
        if (!hasDuplicates) {
            DependencyGraph dg = new DependencyGraph(ib.pcc, ib, this);
            if (buildContext.isInstantiating()) {
                dg.instantiate();
            } else {
                dg.analyze();
            }
        }
    }

    private boolean processNodesAndCheckForDublicates(ArtifactBuildContext buildContext) {
        HashMap<Key<?>, BSE<?>> uniqueNodes = new HashMap<>();
        LinkedHashMap<Key<?>, LinkedHashSet<BSE<?>>> duplicateNodes = new LinkedHashMap<>(); // preserve order for error message

        processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, ib.entries);
        for (ImportedInjector ii : ib.imports) {
            processNodesAndCheckForDublicates0(uniqueNodes, duplicateNodes, ii.entries.values());
        }

        // Add error messages if any nodes with the same key have been added multiple times
        if (!duplicateNodes.isEmpty()) {
            ErrorMessages.addDuplicateNodes(buildContext, duplicateNodes);
        }
        internalNodes.addAll(uniqueNodes.values());
        return !duplicateNodes.isEmpty();
    }

    private void processNodesAndCheckForDublicates0(HashMap<Key<?>, BSE<?>> uniqueNodes, LinkedHashMap<Key<?>, LinkedHashSet<BSE<?>>> duplicateNodes,
            Iterable<? extends BSE<?>> nodes) {
        for (BSE<?> node : nodes) {
            Key<?> key = node.key();
            if (key != null) {
                BSE<?> existing = uniqueNodes.putIfAbsent(key, node);
                if (existing != null) {
                    HashSet<BSE<?>> hs = duplicateNodes.computeIfAbsent(key, m -> new LinkedHashSet<>());
                    hs.add(existing); // might be added multiple times, hence we use a Set
                    hs.add(node);
                }
            }
        }
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

    void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BSE<?>, ServiceDependency> e : missingDependencies) {
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

    public void recordMissingDependency(BSE<?> node, ServiceDependency dependency, boolean fromParent) {

    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param node
     * @param dependency
     */
    public void recordResolvedDependency(BSE<?> node, ServiceDependency dependency, @Nullable ServiceEntry<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<BSE<?>, ServiceDependency>> m = missingDependencies;
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
