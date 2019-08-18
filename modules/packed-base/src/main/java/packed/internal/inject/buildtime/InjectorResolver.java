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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import app.packed.inject.InjectionException;
import app.packed.inject.ServiceDependency;
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
class InjectorResolver {

    final ServiceNodeMap exports = new ServiceNodeMap();

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

    public void checkForMissingDependencies() {
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BSN<?>, ServiceDependency> e : missingDependencies) {
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

    public void recordMissingDependency(BSN<?> node, ServiceDependency dependency, boolean fromParent) {

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

        if (node.autoRequires) {
            if (dependency.isOptional()) {
                ib.requiredOptionally.add(dependency.key());
            } else {
                ib.required.add(dependency.key());
            }
        }
    }

}
