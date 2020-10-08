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
package packed.internal.inject;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;

import app.packed.base.Nullable;
import app.packed.component.BuildException;
import packed.internal.component.RegionBuild;
import packed.internal.container.ContainerBuild;
import packed.internal.inject.service.ServiceBuildManager;

/** A utility class that can find cycles in a dependency graph. */

// New algorithm

// resolve + create id for each node

// https://algs4.cs.princeton.edu/42digraph/TarjanSCC.java.html
// https://www.youtube.com/watch?v=TyWtx7q2D7Y
public final class ServiceIsland {

    /**
     * Tries to find a dependency cycle.
     *
     * @throws BuildException
     *             if a dependency cycle was detected
     */

    // detect cycles for -> detect cycle or needs to be instantited at initialization time
    public static void finish(RegionBuild region, ContainerBuild im) {

        DependencyCycle c = dependencyCyclesFind(region, im);

        if (c != null) {
            throw new BuildException("Dependency cycle detected: " + c);
        }
    }

    private static DependencyCycle dependencyCyclesFind(RegionBuild region, ContainerBuild im) {
        ArrayDeque<Dependant> stack = new ArrayDeque<>();
        ArrayDeque<Dependant> dependencies = new ArrayDeque<>();

        return dependencyCyclesFind(stack, dependencies, region, im);
    }

    private static DependencyCycle dependencyCyclesFind(ArrayDeque<Dependant> stack, ArrayDeque<Dependant> dependencies, RegionBuild region,
            ContainerBuild im) {
        for (Dependant node : im.dependants) {
            if (node.needsPostProcessing) { // only process those nodes that have not been visited yet
                DependencyCycle dc = detectCycle(region, node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }
        if (im.getServiceManager() != null) {
            for (ServiceBuildManager m : im.getServiceManager().children) {
                dependencyCyclesFind(stack, dependencies, region, m.im);
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
     * @param injectable
     *            the node to visit
     * @return stuff
     * @throws BuildException
     *             if there is a cycle in the graph
     */
    @Nullable
    private static DependencyCycle detectCycle(RegionBuild region, Dependant injectable, ArrayDeque<Dependant> stack, ArrayDeque<Dependant> dependencies) {
        DependencyProvider[] deps = injectable.providers;
        if (deps.length > 0) {
            stack.push(injectable);
            for (int i = 0; i < deps.length; i++) {
                DependencyProvider dependency = deps[i];

                if (dependency != null) {
                    Dependant next = dependency.dependant();
                    if (next != null) {
                        if (next.needsPostProcessing) {
                            dependencies.push(next);
                            // See if the component is already on the stack -> A cycle has been detected
                            if (stack.contains(next)) {
                                // clear links not part of the circle, for example, for A->B->C->B we want to remove A
                                while (stack.peekLast() != next) {
                                    stack.pollLast();
                                    dependencies.pollLast();
                                }
                                return new DependencyCycle(dependencies);
                            }
                            DependencyCycle cycle = detectCycle(region, next, stack, dependencies);
                            if (cycle != null) {
                                return cycle;
                            }
                            dependencies.pop();
                        }
                    }
                }
            }

            stack.pop();
        }
        injectable.onResolveSuccess(region);
        return null;
    }

    /** A class indicating a dependency cycle. */
    public static class DependencyCycle {

        final ArrayDeque<Dependant> dependencies;

        DependencyCycle(ArrayDeque<Dependant> dependencies) {
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
