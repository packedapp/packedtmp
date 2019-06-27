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
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

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

/** This class records all service related information for a single box. */

// Kan vi have en InjectorBuilder uden en InjectorExtension???
// Giver ikke saa meget mening....Configurationen skal jo ske gennem InjectorExtension
// Men ja, InjectorConfigurator behoever vel ikke en extension????
public final class InjectorBuilder {

    public boolean autoRequires = true;

    /** A map of all nodes that are exported out from the box. */
    public final ServiceNodeMap exports;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<Entry<BuildtimeServiceNode<?>, ServiceDependency>> missingDependencies;

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BuildtimeServiceNode<?>, List<ServiceDependency>> unresolvedDependencies;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap nodes;

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> optional = new HashSet<>();

    /** A set of all explicitly registered required service keys. */
    public final HashSet<Key<?>> required = new HashSet<>();

    public final ArrayList<BuildtimeServiceNodeExported<?>> exportedNodes = new ArrayList<>();

    /** A list of bundle bindings, as we need to post process the exports. */
    public ArrayList<BindInjectorFromBundle> injectorBundleBindings = new ArrayList<>();

    public DefaultInjector privateInjector;

    public DefaultInjector publicInjector;

    public ArrayList<BuildtimeServiceNode<?>> nodes2 = new ArrayList<>();

    public InjectorBuilder() {
        boolean exportNodes = true;
        // System.out.println(exportNodes);
        if (exportNodes) {
            nodes = new ServiceNodeMap();
            exports = new ServiceNodeMap();
        } else {
            // dont know, maybe just not put things in exported...
            nodes = exports = new ServiceNodeMap();
        }
    }

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public void addOptional(Key<?> key) {
        requireNonNull(key, "key is null");
        optional.add(key);
    }

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public void addRequired(Key<?> key) {
        requireNonNull(key, "key is null");
        required.add(key);
    }

    public void buildContract(InjectorContract.Builder builder) {
        // Why do we need that list
        // for (ServiceBuildNode<?> n : exportedNodes) {
        // if (n instanceof ServiceBuildNodeExposed) {
        // builder.addProvides(n.getKey());
        // }
        // }
        if (optional != null) {
            optional.forEach(k -> {
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

    public void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BuildtimeServiceNode<?>, ServiceDependency> e : missingDependencies) {
                if (!e.getValue().isOptional() && !e.getKey().autoRequires) {
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
                                    sj.add(dependencies.get(j).key().typeLiteral().getRawType().getSimpleName());
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

    public void recordMissingDependency(BuildtimeServiceNode<?> node, ServiceDependency dependency, boolean fromParent) {

    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param node
     * @param dependency
     */
    public void recordResolvedDependency(BuildtimeServiceNode<?> node, ServiceDependency dependency, @Nullable ServiceNode<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<BuildtimeServiceNode<?>, ServiceDependency>> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new SimpleImmutableEntry<>(node, dependency));

        if (node.autoRequires) {
            if (dependency.isOptional()) {
                optional.add(dependency.key());
            } else {
                required.add(dependency.key());
            }
        }
    }
}
