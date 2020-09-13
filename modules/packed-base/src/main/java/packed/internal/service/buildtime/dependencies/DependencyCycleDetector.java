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
package packed.internal.service.buildtime.dependencies;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.service.CyclicDependencyGraphException;
import packed.internal.component.RegionAssembly;
import packed.internal.inject.DependencyProvider;
import packed.internal.inject.Injectable;

/** A utility class that can find cycles in a dependency graph. */

// New algorithm

// resolve + create id for each node

// https://algs4.cs.princeton.edu/42digraph/TarjanSCC.java.html
// https://www.youtube.com/watch?v=TyWtx7q2D7Y
final class DependencyCycleDetector {

    /**
     * Tries to find a dependency cycle.
     *
     * @throws CyclicDependencyGraphException
     *             if a dependency cycle was detected
     */
    static void dependencyCyclesDetect(RegionAssembly resolver, ArrayList<Injectable> detectCyclesFor) {
        DependencyCycle c = dependencyCyclesFind(resolver, detectCyclesFor);
        if (c != null) {
            throw new CyclicDependencyGraphException("Dependency cycle detected: " + c);
        }
    }

    private static DependencyCycle dependencyCyclesFind(RegionAssembly resolver, ArrayList<Injectable> detectCyclesFor) {
        if (detectCyclesFor == null) {
            throw new IllegalStateException("Must resolve nodes before detecting cycles");
        }
        ArrayDeque<Injectable> stack = new ArrayDeque<>();
        ArrayDeque<Injectable> dependencies = new ArrayDeque<>();

        for (Injectable node : detectCyclesFor) {
            // System.out.println("Detect for " + node.directMethodHandle + " " + node.detectForCycles);
            if (node.detectForCycles) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyCycleDetector.detectCycle(resolver, node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        return null;
    }

    /**
     * Recursively invoked for each node.
     *
     * @param stack
     *            the stack of all visited dependencies so far
     * @param dependencies
     *            the stack of locally visited dependencies so far
     * @param node
     *            the node to visit
     * @return stuff
     * @throws CyclicDependencyGraphException
     *             if there is a cycle in the graph
     */
    private static DependencyCycle detectCycle(RegionAssembly resolver, Injectable node, ArrayDeque<Injectable> stack, ArrayDeque<Injectable> dependencies) {
        stack.push(node);

        for (int i = 0; i < node.resolved.length; i++) {
            DependencyProvider dependency = node.resolved[i];
            if (dependency != null) {
                Injectable injectable = dependency.injectable();
                if (injectable != null) {
                    if (injectable.detectForCycles) {
                        dependencies.push(injectable);
                        // See if the component is already on the stack -> A cycle has been detected
                        if (stack.contains(injectable)) {
                            // clear links not part of the circle, for example, for A->B->C->B we want to remove A
                            while (stack.peekLast() != injectable) {
                                stack.pollLast();
                                dependencies.pollLast();
                            }
                            return new DependencyCycle(dependencies);
                        }
                        DependencyCycle cycle = detectCycle(resolver, injectable, stack, dependencies);
                        if (cycle != null) {
                            return cycle;
                        }
                        dependencies.pop();
                    }
                }
            }
        }

        stack.pop(); // assert stack.pop() == node

        resolver.constantServices.add(node);
//        BuildEntry<?> entry = node.entry();
//        System.out.println();
//        if (entry != null) {
//            System.out.println("Adding entry " + entry.key());
//            
//        } else {
//            System.out.println("No service for " + node.directMethodHandle);
//        }

        node.detectForCycles = false;
        return null;
    }

    /** A class indicating a dependency cycle. */
    public static class DependencyCycle {

        final ArrayDeque<Injectable> dependencies;

        DependencyCycle(ArrayDeque<Injectable> dependencies) {
            this.dependencies = requireNonNull(dependencies);
        }

        @Override
        public String toString() {
            // ArrayList<Injectable> list = new ArrayList<>(dependencies);
            // This method does not yet support Provides methods

            // Try checking this out and running some examples, it should have better error messages.
            // https://github.com/cakeframework/cake-container/blob/23d7f3a083a0fc08efbe45dad0016d5195450a0c/modules/org.cakeframework.base/src/main/java/cake/internal/inject/ErrorMessages.java

            StringBuilder sb = new StringBuilder();

            // Should be BuildNodeFactory instead, but now mirror is gone...Maybe put it back again
            // BuildNodeFactory<?> s = (BuildNodeFactory<?>) list.get(0);
            // Collections.reverse(list);

            // Uncomments the 3
            // sb.append(format(s.factory.mirror.getType()));
//            for (Injectable n : list) {
//                sb.append(" -");
//                // s = (BuildNodeOldFactory<?>) n;
//                // sb.append("> ").append(format(s.factory.mirror.getType()));
//            }

            return sb.toString();
        }
    }
}
