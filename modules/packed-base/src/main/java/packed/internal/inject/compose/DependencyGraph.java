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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.InstantiationMode;
import app.packed.inject.ServiceDependency;
import packed.internal.container.PackedContainerConfiguration;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BSE;
import packed.internal.inject.build.BSEComponent;
import packed.internal.inject.build.InjectorBuilder;
import packed.internal.inject.compose.DependencyGraphCycleDetector.DependencyCycle;
import packed.internal.inject.run.DefaultInjector;
import packed.internal.inject.util.InternalDependencyDescriptor;
import packed.internal.inject.util.ServiceNodeMap;
import packed.internal.util.KeyBuilder;

final class DependencyGraph {

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BSE<?>> detectCyclesFor;

    /** The root injector builder. */
    final PackedContainerConfiguration root;

    final InjectorBuilder ib;

    final InjectorResolver ir;

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
    public void analyze() {
        ir.privateInjector = new DefaultInjector(root, ir.internalNodes);
        BSEComponent d = new BSEComponent<>(ib, root.configSite(), ir.privateInjector);
        d.as(KeyBuilder.INJECTOR_KEY);
        ir.internalNodes.put(d);
        // TODO replace with something a.la.
        // dcc.source.isInjectorConfigurator
        if (root.buildContext().artifactType() == Injector.class) {
            ir.publicInjector = requireNonNull(ir.privateInjector);
        } else {
            ServiceNodeMap sm = ib.exporter == null ? new ServiceNodeMap() : ib.exporter.resolvedExports;
            ir.publicInjector = new DefaultInjector(root, sm);

            // Add public injector
            // bn = new BuildNodeInstance<>(c, configSite.UNKNOWN, c.publicInjector);
            // bn.as(Injector.class);
            // c.public BuildNodeList.add(bn);

        }

        // if (ib.injectorBundleBindings != null) {
        // for (BindInjectorFromBundle bi : ib.injectorBundleBindings) {
        // bi.processExport();
        // // new DependencyGraph(bi.newConfiguration).instantiate();
        // }
        // }

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
        ArrayDeque<BSE<?>> stack = new ArrayDeque<>();
        ArrayDeque<BSE<?>> dependencies = new ArrayDeque<>();
        for (BSE<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyGraphCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    public void instantiate() {
        analyze();

        // Instantiate all singletons
        // System.out.println(root.box.services().exports);

        for (ServiceEntry<?> node : ir.internalNodes) {
            if (node instanceof BSEComponent) {
                BSEComponent<?> s = (BSEComponent<?>) node;
                if (s.instantiationMode() == InstantiationMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        ir.internalNodes.toRuntimeNodes();

        if (ib.exporter != null) {
            if (ir.internalNodes != ib.exporter.resolvedExports) {
                ib.exporter.resolvedExports.toRuntimeNodes();
            }
        }
    }

    // Requirements -> cannot require any exposed services, or internally registered services...

    void resolveAllDependencies() {
        detectCyclesFor = new ArrayList<>();

        for (ServiceEntry<?> nn : ir.internalNodes) {
            BSE<?> node = (BSE<?>) nn;
            if (node.needsResolving()) {
                detectCyclesFor.add(node);
                List<InternalDependencyDescriptor> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    ServiceDependency dependency = dependencies.get(i);
                    ServiceEntry<?> resolveTo = ir.internalNodes.getNode(dependency);
                    ir.recordResolvedDependency(node, dependency, resolveTo, false);
                    node.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        ir.checkForMissingDependencies();
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
