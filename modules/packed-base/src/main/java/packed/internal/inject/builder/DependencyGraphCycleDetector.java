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
package packed.internal.inject.builder;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.inject.InjectionException;
import packed.internal.inject.Node;

/** A utility class that can find cycles in a dependency graph. */
final class DependencyGraphCycleDetector {

    /**
     * Recursively invoked for each node.
     *
     * @param stack
     *            the stack of all visited dependencies so far
     * @param dependencies
     *            the stack of locally visited dependencies so far
     * @param node
     *            the node to visit
     * @return
     * @throws InjectionException
     *             if there is a cycle in the graph
     */
    static DependencyCycle detectCycle(BuildNode<?> node, ArrayDeque<BuildNode<?>> stack, ArrayDeque<BuildNode<?>> dependencies) {
        stack.push(node);
        for (int i = 0; i < node.resolvedDependencies.length; i++) {
            Node<?> dependency = node.resolvedDependencies[i];
            if (dependency instanceof BuildNode) {
                BuildNode<?> to = (BuildNode<?>) dependency;
                // If the dependency is a @Provides method, we need to use the declaring node
                BuildNode<?> owner = to.declaringNode();
                if (owner != null) {
                    to = owner;
                }

                if (to.needsResolving() && to instanceof BuildNodeDefault) {
                    BuildNodeDefault<?> ic = (BuildNodeDefault<?>) to;
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

        final ArrayDeque<BuildNode<?>> dependencies;

        DependencyCycle(ArrayDeque<BuildNode<?>> dependencies) {
            this.dependencies = requireNonNull(dependencies);
        }

        @Override
        public String toString() {
            ArrayList<BuildNode<?>> list = new ArrayList<>(dependencies);
            // This method does not yet support Provides methods

            // Try checking this out and running some examples, it should have better error messages.
            // https://github.com/cakeframework/cake-container/blob/23d7f3a083a0fc08efbe45dad0016d5195450a0c/modules/org.cakeframework.base/src/main/java/cake/internal/inject/ErrorMessages.java

            StringBuilder sb = new StringBuilder();

            // Should be BuildNodeFactory instead, but now mirror is gone...Maybe put it back again
            // BuildNodeFactory<?> s = (BuildNodeFactory<?>) list.get(0);
            // Collections.reverse(list);

            // Uncomments the 3
            // sb.append(format(s.factory.mirror.getType()));
            for (BuildNode<?> n : list) {
                System.out.println(n);
                sb.append(" -");
                // s = (BuildNodeOldFactory<?>) n;
                // sb.append("> ").append(format(s.factory.mirror.getType()));
            }

            return sb.toString();
        }
    }
}
