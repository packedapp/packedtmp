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
package packed.internal.service.build.dependencies;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.service.CyclicDependencyGraphException;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.service.ComponentFactoryBuildEntry;

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
    static void dependencyCyclesDetect(ArrayList<BuildEntry<?>> detectCyclesFor) {
        DependencyCycle c = dependencyCyclesFind(detectCyclesFor);
        if (c != null) {
            throw new CyclicDependencyGraphException("Dependency cycle detected: " + c);
        }
    }

    private static DependencyCycle dependencyCyclesFind(ArrayList<BuildEntry<?>> detectCyclesFor) {
        if (detectCyclesFor == null) {
            throw new IllegalStateException("Must resolve nodes before detecting cycles");
        }
        ArrayDeque<BuildEntry<?>> stack = new ArrayDeque<>();
        ArrayDeque<BuildEntry<?>> dependencies = new ArrayDeque<>();
        for (BuildEntry<?> node : detectCyclesFor) {
            if (!node.detectCycleVisited) { // only process those nodes that have not been visited yet
                DependencyCycle dc = DependencyCycleDetector.detectCycle(node, stack, dependencies);
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
    private static DependencyCycle detectCycle(BuildEntry<?> node, ArrayDeque<BuildEntry<?>> stack, ArrayDeque<BuildEntry<?>> dependencies) {
        stack.push(node);
        for (int i = 0; i < node.resolvedDependencies.length; i++) {
            BuildEntry<?> dependency = node.resolvedDependencies[i];
            if (dependency == null) {
                // System.out.println(node.dependencies);
                // new Exception().printStackTrace();
            }
            if (dependency != null) {
                BuildEntry<?> to = dependency;
                // If the dependency is a @Provides method, we need to use the declaring node

                if (to.hasUnresolvedDependencies() && to instanceof ComponentFactoryBuildEntry) {
                    ComponentFactoryBuildEntry<?> ic = (ComponentFactoryBuildEntry<?>) to;
                    if (!ic.detectCycleVisited) {
                        dependencies.push(to);
                        // See if the component is already on the stack -> A cycle has been detected
                        if (stack.contains(to)) {
                            // clear links not part of the circle, for example, for A->B->C->B we want to remove A
                            while (stack.peekLast() != to) {
                                stack.pollLast();
                                dependencies.pollLast();
                            }
                            return new DependencyCycle(dependencies);
                        }
                        DependencyCycle cycle = detectCycle(ic, stack, dependencies);
                        if (cycle != null) {
                            return cycle;
                        }
                        dependencies.pop();
                    }
                }
            }
        }
        stack.pop(); // assert stack.pop() == node
        node.detectCycleVisited = true;
        return null;
    }

    /** A class indicating a dependency cycle. */
    public static class DependencyCycle {

        final ArrayDeque<BuildEntry<?>> dependencies;

        DependencyCycle(ArrayDeque<BuildEntry<?>> dependencies) {
            this.dependencies = requireNonNull(dependencies);
        }

        @Override
        public String toString() {
            ArrayList<BuildEntry<?>> list = new ArrayList<>(dependencies);
            // This method does not yet support Provides methods

            // Try checking this out and running some examples, it should have better error messages.
            // https://github.com/cakeframework/cake-container/blob/23d7f3a083a0fc08efbe45dad0016d5195450a0c/modules/org.cakeframework.base/src/main/java/cake/internal/inject/ErrorMessages.java

            StringBuilder sb = new StringBuilder();

            // Should be BuildNodeFactory instead, but now mirror is gone...Maybe put it back again
            // BuildNodeFactory<?> s = (BuildNodeFactory<?>) list.get(0);
            // Collections.reverse(list);

            // Uncomments the 3
            // sb.append(format(s.factory.mirror.getType()));
            for (BuildEntry<?> n : list) {
                System.out.println(n);
                sb.append(" -");
                // s = (BuildNodeOldFactory<?>) n;
                // sb.append("> ").append(format(s.factory.mirror.getType()));
            }

            return sb.toString();
        }
    }
}
