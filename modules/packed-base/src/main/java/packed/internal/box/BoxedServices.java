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
package packed.internal.box;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionException;
import app.packed.inject.ServiceContract;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.ServiceNodeMap;
import packed.internal.inject.builder.ServiceBuildNode;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/**
 *
 */
public class BoxedServices {

    final Box box;

    public final ServiceNodeMap exportedNodes;

    private ArrayList<Entry<ServiceBuildNode<?>, Dependency>> missingDependencies;

    /** A node map with all nodes, populated with build nodes at configuration time, and runtime nodes at run time. */
    public final ServiceNodeMap nodes;

    final HashSet<Key<?>> requiredServicesOptionally = new HashSet<>();

    public final HashSet<Key<?>> requires = new HashSet<>();

    BoxedServices(Box box) {
        this.box = requireNonNull(box);
        if (box.source.privateServices()) {
            nodes = new ServiceNodeMap();
            exportedNodes = new ServiceNodeMap();
        } else {
            nodes = exportedNodes = new ServiceNodeMap();
        }
    }

    /**
     * Adds the specified key to the list of optional services.
     * 
     * @param key
     *            the key to add
     */
    public void addOptional(Key<?> key) {
        requiredServicesOptionally.add(requireNonNull(key, "key is null"));
    }

    /**
     * Adds the specified key to the list of required services.
     * 
     * @param key
     *            the key to add
     */
    public void addRequires(Key<?> key) {
        requires.add(requireNonNull(key, "key is null"));
    }

    public void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<ServiceBuildNode<?>, Dependency> e : missingDependencies) {
                if (!e.getValue().isOptional() && !e.getKey().autoRequires) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    List<InternalDependency> dependencies = e.getKey().dependencies;

                    if (dependencies.size() == 1) {
                        sb.append("single ");
                    }
                    Dependency dependency = e.getValue();
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
                                if (j == dependency.index().getAsInt()) {
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

    public void populateBuilder(ServiceContract.Builder builder) {
        // Why do we need that list
        // for (ServiceBuildNode<?> n : exportedNodes) {
        // if (n instanceof ServiceBuildNodeExposed) {
        // builder.addProvides(n.getKey());
        // }
        // }
        if (missingDependencies != null) {
            // for (Entry<ServiceBuildNode<?>, Dependency> e : missingDependencies) {
            // if (e.getValue().isOptional()) {
            // requiredServicesOptionally.add(e.getKey().key());
            // } else {
            // requiredServicesMandatory.add(e.getKey().key());
            // }
            // }
            // We remove all optional dependencies that are also mandatory.
            requiredServicesOptionally.removeAll(requires);

            if (requiredServicesOptionally != null) {
                requiredServicesOptionally.forEach(k -> builder.addOptional(k));
            }
            if (requires != null) {
                requires.forEach(k -> builder.addRequires(k));
            }
        }
    }

    /**
     * Record a dependency that could not be resolved
     * 
     * @param node
     * @param dependency
     */
    public void recordDependencyResolved(ServiceBuildNode<?> node, Dependency dependency, @Nullable ServiceNode<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<ServiceBuildNode<?>, Dependency>> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new SimpleImmutableEntry<>(node, dependency));

        if (node.autoRequires) {
            if (dependency.isOptional()) {
                requiredServicesOptionally.add(dependency.key());
            } else {
                requires.add(dependency.key());
            }
        }
    }
}
