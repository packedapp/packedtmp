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
package packed.internal.inject.buildnodes;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Key;
import packed.internal.inject.CommonKeys;
import packed.internal.inject.Node;
import packed.internal.inject.buildnodes.DependencyGraphCycleDetector.DependencyCycle;

final class DependencyGraph {

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildNode<?>> detectCyclesFor;

    /** The root injector builder. */
    final InjectorBuilder root;

    /**
     * Creates a new dependency graph.
     * 
     * @param root
     *            the root injector builder
     */
    DependencyGraph(InjectorBuilder root) {
        this.root = requireNonNull(root);
    }

    /** Also used for descriptors. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    void analyze(InjectorBuilder builder) {
        builder.privateInjector = new InternalInjector(builder, builder.privateNodeMap);
        builder.privateNodeMap.put(new BuildNodeDefault<>(builder, builder.getConfigurationSite(), builder.privateInjector).as((Key) CommonKeys.INJECTOR_KEY));
        if (builder.bundle == null) {
            builder.publicInjector = builder.privateInjector;
        } else {
            builder.publicInjector = new InternalInjector(builder, builder.publicNodeMap);

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

        // All exposures
        if (builder.publicNodeList != null) {
            for (BuildNode<?> bn : builder.publicNodeList) {
                if (bn instanceof BuildNodeExposed) {
                    BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
                    Node<?> node = builder.privateNodeMap.getRecursive(bne.getPrivateKey());
                    bne.exposureOf = requireNonNull((Node) node, "Could not find private key " + bne.getPrivateKey());
                }
            }
        }

        // If we do not export services into a bundle. We should be able to resolver much quicker..
        DependencyGraphResolver.resolveAllDependencies(this);

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
        ArrayDeque<BuildNode<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildNode<?>> dependencies = new ArrayDeque<>();
        for (BuildNode<?> node : detectCyclesFor) {
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
        for (Node<?> node : root.privateNodeMap.nodes.values()) {
            if (node instanceof BuildNodeDefault) {
                BuildNodeDefault<?> s = (BuildNodeDefault<?>) node;
                if (s.getBindingMode() == BindingMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }

        // Okay we are finished, convert all nodes to runtime nodes.
        root.privateNodeMap.toRuntimeNodes();
        if (root.privateNodeMap != root.publicNodeMap) {
            root.publicNodeMap.toRuntimeNodes();
        }
    }

}
