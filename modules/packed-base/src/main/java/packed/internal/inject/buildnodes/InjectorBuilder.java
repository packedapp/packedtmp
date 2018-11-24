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
import java.util.HashMap;

import app.packed.inject.BindingMode;
import app.packed.inject.InjectionException;
import app.packed.inject.Injector;
import app.packed.inject.Key;
import packed.internal.inject.InternalInjector;
import packed.internal.inject.Node;
import packed.internal.inject.buildnodes.DependecyCycleDetector.DependencyCycle;

public final class InjectorBuilder {

    final InternalInjectorConfiguration c;

    /** A list of nodes to use when detecting dependency cycles. */
    ArrayList<BuildNode<?>> detectCyclesFor;

    public InjectorBuilder(InternalInjectorConfiguration c) {
        this.c = requireNonNull(c);
    }

    @SuppressWarnings({ "rawtypes" })
    public void build() {
        setup();
        instantiateAll(c.privateInjector);

        if (c.bundle != null) {
            for (BuildNode<?> bn : c.publicExposedNodeList) {
                if (bn instanceof BuildNodeExposed) {
                    BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
                    c.publicRuntimeNodes.put(bne.toRuntimeNode());
                }
            }
        } else {
            // TODO fix
            for (BuildNode<?> bn : c.privateBuildNodeList) {
                c.publicRuntimeNodes.put(bn.toRuntimeNode());
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setup() {
        // First start by creating the new injectors.
        c.privateInjector = new InternalInjector(c, c.privateRuntimeNodes); // Ignores any parent + Bundle for now.
        BuildNode<InternalInjector> inj = new BuildNodeDefault<>(c, c.getConfigurationSite(), c.privateInjector);
        inj.as(Injector.class);
        c.privateBuildNodeList.add(inj);

        if (c.bundle == null) {
            c.publicInjector = c.privateInjector;
        } else {
            c.publicInjector = new InternalInjector(c, c.publicRuntimeNodes);

            // Add public injector
            // bn = new BuildNodeInstance<>(c, InternalConfigurationSite.UNKNOWN, c.publicInjector);
            // bn.as(Injector.class);
            // c.public BuildNodeList.add(bn);

        }

        // Freeze

        // import all services...
        for (BindInjector i : c.privateImports) {
            i.importInto(c);
        }

        HashMap<Key<?>, ArrayList<BuildNode<?>>> collisions = new HashMap<>();
        for (BuildNode<?> bv : c.privateBuildNodeList) {
            if (bv.getKey() != null) {
                if (!c.privateBuildNodeMap.putIfAbsent(bv)) {
                    collisions.computeIfAbsent(bv.getKey(), k -> new ArrayList<>()).add(bv);
                }
                c.privateBuildNodeMap.put(bv);
            }
        }

        if (!collisions.isEmpty()) {
            System.err.println("OOOPS");
        }

        // All exposures
        for (BuildNode<?> bn : c.publicExposedNodeList) {
            if (bn instanceof BuildNodeExposed) {
                BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
                Node<?> node = c.privateBuildNodeMap.get(bne.getPrivateKey());
                bne.exposureOf = requireNonNull((Node) node, "Could not find private key " + bne.getPrivateKey());
            }
        }

        InjectorBuilderResolver.resolveAllDependencies(this);

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
                DependencyCycle dc = DependecyCycleDetector.detectCycle(node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    /**
     * Instantiates all nodes.
     *
     * @throws RuntimeException
     *             if a node could not be instantiated
     */
    public void instantiateAll(InternalInjector injector) {
        for (BuildNode<?> node : c.privateBuildNodeList) {
            if (node instanceof BuildNodeDefault) {
                BuildNodeDefault<?> s = (BuildNodeDefault<?>) node;
                if (s.getBindingMode() == BindingMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }
        for (BuildNode<?> n : c.privateBuildNodeList) {
            if (n.getKey() != null) {
                c.privateRuntimeNodes.put(n.toRuntimeNode());
            }
        }
    }

}
