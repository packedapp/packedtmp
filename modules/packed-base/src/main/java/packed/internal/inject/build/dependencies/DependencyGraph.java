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
package packed.internal.inject.build.dependencies;

import static java.util.Objects.requireNonNull;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InjectorContract;
import app.packed.inject.ServiceDependency;
import app.packed.util.Key;
import app.packed.util.MethodDescriptor;
import app.packed.util.Nullable;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BuildEntry;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.build.InjectorResolver;
import packed.internal.inject.build.dependencies.DependencyGraphCycleDetector.DependencyCycle;
import packed.internal.inject.build.export.ServiceExporter;
import packed.internal.inject.build.service.BSEComponent;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.PackedServiceDependency;
import packed.internal.inject.util.ServiceNodeMap;
import packed.internal.util.KeyBuilder;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

public final class DependencyGraph {

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildEntry<?>> detectCyclesFor;

    /** The root injector builder. */
    final PackedContainerConfiguration root;

    final InjectorBuilder ib;

    final InjectorResolver ir;

    /** A set of all explicitly registered required service keys. */
    final HashSet<Key<?>> required = new HashSet<>();

    /** A set of all explicitly registered optional service keys. */
    final HashSet<Key<?>> requiredOptionally = new HashSet<>();

    /** A map of all dependencies that could not be resolved */
    IdentityHashMap<BuildEntry<?>, List<ServiceDependency>> unresolvedDependencies;

    /** A list of all dependencies that have not been resolved */
    private ArrayList<Entry<BuildEntry<?>, ServiceDependency>> missingDependencies;

    /**
     * Creates a new dependency graph.
     * 
     * @param root
     *            the root injector builder
     */
    public DependencyGraph(PackedContainerConfiguration root, InjectorBuilder ib, InjectorResolver resolver) {
        this.root = requireNonNull(root);
        this.ib = requireNonNull(ib);
        this.ir = requireNonNull(resolver);
    }

    /** Also used for descriptors. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void analyze(ServiceExporter exporter) {
        ir.privateInjector = new DefaultInjector(root, ir.resolvedEntries);
        BSEComponent d = new BSEComponent<>(ib, root.configSite(), ir.privateInjector);
        d.as(KeyBuilder.INJECTOR_KEY);
        ir.resolvedEntries.put(d);

        if (root.buildContext().artifactType() == Injector.class) {
            ir.publicInjector = requireNonNull(ir.privateInjector);
        } else {
            ServiceNodeMap sm = exporter == null ? new ServiceNodeMap() : exporter.resolvedExports;
            ir.publicInjector = new DefaultInjector(root, sm);
        }

        // If we do not export services into a bundle. We should be able to resolver much quicker..
        resolveAllDependencies();
        dependencyCyclesDetect();
    }

    /**
     * Tries to find a dependency cycle.
     *
     * @throws InjectionException
     *             if a dependency cycle was detected
     */
    private void dependencyCyclesDetect() {
        DependencyCycle c = dependencyCyclesFind();
        if (c != null) {
            throw new InjectionException("Dependency cycle detected: " + c);
        }
    }

    private DependencyCycle dependencyCyclesFind() {
        if (detectCyclesFor == null) {
            throw new IllegalStateException("Must resolve nodes before detecting cycles");
        }
        ArrayDeque<BuildEntry<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildEntry<?>> dependencies = new ArrayDeque<>();
        for (BuildEntry<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyGraphCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    private void resolveAllDependencies() {
        detectCyclesFor = new ArrayList<>();

        for (ServiceEntry<?> nn : ir.resolvedEntries) {
            BuildEntry<?> node = (BuildEntry<?>) nn;
            if (node.needsResolving()) {
                detectCyclesFor.add(node);
                List<PackedServiceDependency> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    ServiceDependency dependency = dependencies.get(i);
                    ServiceEntry<?> resolveTo = ir.resolvedEntries.getNode(dependency);
                    recordResolvedDependency(node, dependency, resolveTo, false);
                    node.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        checkForMissingDependencies();
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
    public void recordResolvedDependency(BuildEntry<?> node, ServiceDependency dependency, @Nullable ServiceEntry<?> resolvedTo, boolean fromParent) {
        requireNonNull(node);
        requireNonNull(dependency);
        if (resolvedTo != null) {
            return;
        }
        ArrayList<Entry<BuildEntry<?>, ServiceDependency>> m = missingDependencies;
        if (m == null) {
            m = missingDependencies = new ArrayList<>();
        }
        m.add(new SimpleImmutableEntry<>(node, dependency));

        if (ib.dependencies == null || !ib.dependencies.manualRequirementsManagement) {
            if (dependency.isOptional()) {
                requiredOptionally.add(dependency.key());
            } else {
                required.add(dependency.key());
            }
        }
    }

    public void checkForMissingDependencies() {
        boolean manualRequirementsManagement = ib.dependencies != null && ib.dependencies.manualRequirementsManagement;
        if (missingDependencies != null) {
            // if (!box.source.unresolvedServicesAllowed()) {
            for (Entry<BuildEntry<?>, ServiceDependency> e : missingDependencies) {
                if (!e.getValue().isOptional() && manualRequirementsManagement) {
                    // Long long error message
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot resolve dependency for ");
                    List<PackedServiceDependency> dependencies = e.getKey().dependencies;

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

    public void recordMissingDependency(BuildEntry<?> node, ServiceDependency dependency, boolean fromParent) {

    }
}
