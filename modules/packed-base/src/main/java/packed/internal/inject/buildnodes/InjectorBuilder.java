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
import app.packed.inject.Key;
import packed.internal.inject.CommonKeys;
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

        // if (c.bundle != null) {
        // for (BuildNode<?> bn : c.publicExposedNodeList) {
        // if (bn instanceof BuildNodeExposed) {
        // BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
        // c.privateInjectorNodes.put(bne.toRuntimeNode());
        // }
        // }
        // } else {
        // // TODO fix
        // for (BuildNode<?> bn : c.privateNodeList) {
        // c.privateInjectorNodes.put(bn.toRuntimeNode());
        // }
        // for (Node<?> bn : c.privateNodeMap.toAll()) {
        // if (bn instanceof BuildNode) {
        // c.privateInjectorNodes.put(((BuildNode<?>) bn).toRuntimeNode());
        // }
        // }
        // }

        c.privateNodeMap.nodes.replaceAll((k, v) -> v.toRuntimeNode());
    }

    /** Also used for descriptors. */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void setup() {
        c.freezeBindings();
        c.privateInjector = new InternalInjector(c, c.privateNodeMap);
        c.privateNodeMap.put(new BuildNodeDefault<>(c, c.getConfigurationSite(), c.privateInjector).as((Key) CommonKeys.INJECTOR_KEY));

        if (c.bundle == null) {
            c.publicInjector = c.privateInjector;
        } else {
            c.publicInjector = new InternalInjector(c, c.publicInjectorNodes);

            // Add public injector
            // bn = new BuildNodeInstance<>(c, InternalConfigurationSite.UNKNOWN, c.publicInjector);
            // bn.as(Injector.class);
            // c.public BuildNodeList.add(bn);

        }

        for (BindInjector i : c.injectorBindings) {
            if (i instanceof BindInjectorFromBundle) {
                BindInjectorFromBundle bi = (BindInjectorFromBundle) i;
                new InjectorBuilder(bi.c).build();
            }
        }
        // Freeze

        HashMap<Key<?>, ArrayList<BuildNode<?>>> collisions = new HashMap<>();
        for (BuildNode<?> bv : c.privateNodeList) {
            if (bv.getKey() != null) {
                if (!c.privateNodeMap.putIfAbsent(bv)) {
                    collisions.computeIfAbsent(bv.getKey(), k -> new ArrayList<>()).add(bv);
                }
                c.privateNodeMap.put(bv);
            }
        }

        if (!collisions.isEmpty()) {
            System.err.println("OOOPS");
        }

        // All exposures
        for (BuildNode<?> bn : c.publicExposedNodeList) {
            if (bn instanceof BuildNodeExposed) {
                BuildNodeExposed<?> bne = (BuildNodeExposed) bn;
                Node<?> node = c.privateNodeMap.get(bne.getPrivateKey());
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
        for (Node<?> node : c.privateNodeMap.nodes.values()) {
            if (node instanceof BuildNodeDefault) {
                BuildNodeDefault<?> s = (BuildNodeDefault<?>) node;
                if (s.getBindingMode() == BindingMode.SINGLETON) {
                    s.getInstance(null);// getInstance() caches the new instance, newInstance does not
                }
            }
        }
    }

}
