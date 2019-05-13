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

import java.lang.invoke.MethodHandles;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.DependencyDescriptor;
import app.packed.inject.InjectionException;
import app.packed.inject.InstantiationMode;
import packed.internal.box.BoxServices;
import packed.internal.classscan.ServiceClassDescriptor;
import packed.internal.inject.InternalDependencyDescriptor;
import packed.internal.inject.ServiceNode;
import packed.internal.inject.buildtime.DependencyGraphCycleDetector.DependencyCycle;
import packed.internal.inject.runtime.InternalInjector;
import packed.internal.util.KeyBuilder;

final class DependencyGraph {

    static final ServiceClassDescriptor INJ = ServiceClassDescriptor.from(MethodHandles.lookup(), InternalInjector.class);

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildtimeServiceNode<?>> detectCyclesFor;

    /** The root injector builder. */
    final ContainerBuilder root;

    /**
     * Creates a new dependency graph.
     * 
     * @param root
     *            the root injector builder
     */
    DependencyGraph(ContainerBuilder root) {
        this.root = requireNonNull(root);
    }

    /** Also used for descriptors. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void analyze(ContainerBuilder builder) {
        builder.privateInjector = new InternalInjector(builder, builder.box.services().nodes);
        BuildtimeServiceNodeDefault d = new BuildtimeServiceNodeDefault<>(builder, builder.configurationSite(), INJ, builder.privateInjector);
        d.as(KeyBuilder.INJECTOR_KEY);
        builder.box.services().nodes.put(d);
        if (builder.bundle == null) {
            builder.publicInjector = builder.privateInjector;
        } else {
            builder.publicInjector = new InternalInjector(builder, builder.box.services().exports);

            // Add public injector
            // bn = new BuildNodeInstance<>(c, InternalConfigurationSite.UNKNOWN, c.publicInjector);
            // bn.as(Injector.class);
            // c.public BuildNodeList.add(bn);

        }

        if (builder.injectorBundleBindings != null) {
            for (BindInjectorFromBundle bi : builder.injectorBundleBindings) {
                bi.processExport();
                new DependencyGraph(bi.newConfiguration).instantiate();
            }
        }

        // If we do not export services into a bundle. We should be able to resolver much quicker..
        resolveAllDependencies(this);
        dependencyCyclesDetect();
    }

    /**
     * Tries to find a dependency cycle.
     *
     * @throws InjectionException
     *             if a dependency cycle was detected
     */
    public void dependencyCyclesDetect() {
        DependencyCycle c = dependencyCyclesFind();
        if (c != null) {
            throw new InjectionException("Dependency cycle detected: " + c);
        }
    }

    DependencyCycle dependencyCyclesFind() {
        if (detectCyclesFor == null) {
            throw new IllegalStateException("Must resolve nodes before detecting cycles");
        }
        ArrayDeque<BuildtimeServiceNode<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildtimeServiceNode<?>> dependencies = new ArrayDeque<>();
        for (BuildtimeServiceNode<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyGraphCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    void instantiate() {
        analyze(root);

        // Instantiate all singletons
        // System.out.println(root.box.services().exports);

        for (ServiceNode<?> node : root.box.services().nodes) {
            if (node instanceof BuildtimeServiceNodeDefault) {
                BuildtimeServiceNodeDefault<?> s = (BuildtimeServiceNodeDefault<?>) node;
                if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        root.box.services().nodes.toRuntimeNodes();
        if (root.box.services().nodes != root.box.services().exports) {
            root.box.services().exports.toRuntimeNodes();
        }
    }

    // Requirements -> cannot require any exposed services, or internally registered services...

    static void resolveAllDependencies(DependencyGraph graph) {
        graph.detectCyclesFor = new ArrayList<>();

        BoxServices services = graph.root.box.services();

        for (ServiceNode<?> nn : services.nodes) {
            BuildtimeServiceNode<?> node = (BuildtimeServiceNode<?>) nn;

            if (node.needsResolving()) {
                graph.detectCyclesFor.add(node);
                List<InternalDependencyDescriptor> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    DependencyDescriptor dependency = dependencies.get(i);
                    ServiceNode<?> resolveTo = services.nodes.getNode(dependency);
                    services.recordResolvedDependency(node, dependency, resolveTo, false);
                    node.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        services.checkForMissingDependencies();
        // b.root.privateNodeMap.forEach(n -> ((ServiceBuildNode<?>) n).checkResolved());
    }
}

// All exposures
// if (builder.publicNodeList != null) {
// for (BuildNode<?> bn : builder.publicNodeList) {
// if (bn instanceof BuildNodeExposed) {
// BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
// Node<?> node = builder.privateNodeMap.getRecursive(bne. .getPrivateKey());
// bne.exposureOf = requireNonNull((Node) node, "Could not find private key " + bne.getPrivateKey());
// }
// }
// }
