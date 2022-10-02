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
package internal.app.packed.operation.oldbindings;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;

import app.packed.application.BuildException;
import app.packed.base.Nullable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.pool.LifetimePoolSetup;
import internal.app.packed.service.ContainerInjectionManager;

/**
 * A service multi-composer is responsible for managing 1 or more {@link ContainerInjectionManager service composers}
 * that are directly connected and part of the same build.
 * <p>
 * This class server two main purposes:
 * 
 * Finds dependency circles either within the same container or across containers that are not in a parent-child
 * relationship.
 * 
 * Responsible for invoking the {@link DependencyNode#onAllDependenciesResolved(LifetimePoolSetup)} callback for every
 * {@link DependencyNode}. We do this here, because we guarantee that all dependants of a dependant are always invoked
 * before the dependant itself.
 */
// New algorithm
// resolve + create id for each node
// https://algs4.cs.princeton.edu/42digraph/TarjanSCC.java.html
// https://www.youtube.com/watch?v=TyWtx7q2D7Y

// TODO vi behoever jo faktisk kun checke beans fra samme realm...
// En ConfigBean kan jo ikke bruge en UserBean
// Ved faktisk ikke om det goer nogen forskel

//TODO WE NEED TO CHECK INTRA Assembly REFERENCES
// BitMap???
public final class ApplicationInjectionManager {

    /**
     * Tries to find a dependency cycle.
     *
     * @throws BuildException
     *             if a dependency cycle was detected
     */
    // detect cycles for -> detect cycle or needs to be instantited at initialization time
    public void finish(LifetimePoolSetup region, ContainerSetup container) {
        DependencyCycle c = dependencyCyclesFind(region, container);
        if (c != null) {
            throw new BuildException("Dependency cycle detected: " + c);
        }
    }

    private DependencyCycle dependencyCyclesFind(LifetimePoolSetup region, ContainerSetup container) {
        ArrayDeque<DependencyNode> stack = new ArrayDeque<>();
        ArrayDeque<DependencyNode> dependencies = new ArrayDeque<>();

        return dependencyCyclesFind(stack, dependencies, region, container);
    }

    private DependencyCycle dependencyCyclesFind(ArrayDeque<DependencyNode> stack, ArrayDeque<DependencyNode> dependencies, LifetimePoolSetup region,
            ContainerSetup container) {
        for (DependencyNode node : container.injectionManager.consumers) {
            if (node.needsPostProcessing) { // only process those nodes that have not been visited yet
                DependencyCycle dc = detectCycle(region, node, stack, dependencies);
                if (dc != null) {
                    return dc;
                }
            }
        }

        if (container.containerChildren != null) {
            for (ContainerSetup c : container.containerChildren) {
                dependencyCyclesFind(stack, dependencies, region, c);
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
     * @throws BuildException
     *             if there is a cycle in the graph
     */
    @Nullable
    private DependencyCycle detectCycle(LifetimePoolSetup region, DependencyNode node, ArrayDeque<DependencyNode> stack,
            ArrayDeque<DependencyNode> dependencies) {
        DependencyProducer[] deps = node.producers;
        if (deps.length > 0) {
            stack.push(node);
            for (int i = 0; i < deps.length; i++) {
                DependencyProducer dependency = deps[i];
                if (dependency != null) {
                    DependencyNode next = dependency.dependencyConsumer();
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
        // Hvis vi analysere skal vi ikke lave det her...
        node.onAllDependenciesResolved(region);
        return null;
    }

    /** A class indicating a dependency cycle. */
    public static class DependencyCycle {

        final ArrayDeque<DependencyNode> dependencies;

        DependencyCycle(ArrayDeque<DependencyNode> dependencies) {
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