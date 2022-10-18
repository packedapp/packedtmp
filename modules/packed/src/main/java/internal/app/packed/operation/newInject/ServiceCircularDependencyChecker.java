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
package internal.app.packed.operation.newInject;

import java.util.ArrayDeque;

import app.packed.application.BuildException;
import app.packed.bean.CircularDependencyException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.lifetime.LifetimeObjectArenaSetup;
import internal.app.packed.service.InternalServiceExtension;
import internal.app.packed.service.inject.DependencyNode;

/**
 * A service multi-composer is responsible for managing 1 or more {@link InternalServiceExtension service composers}
 * that are directly connected and part of the same build.
 * <p>
 * This class server two main purposes:
 * 
 * Finds dependency circles either within the same container or across containers that are not in a parent-child
 * relationship.
 * 
 * Responsible for invoking the {@link DependencyNode#onAllDependenciesResolved(LifetimeObjectArenaSetup)} callback for
 * every {@link DependencyNode}. We do this here, because we guarantee that all dependants of a dependant are always
 * invoked before the dependant itself.
 */
public final class ServiceCircularDependencyChecker {

    private static void dependencyCyclesFind(ArrayDeque<ServiceEntry> stack, ArrayDeque<ServiceEntry> dependencies, ContainerSetup container) {
        for (ServiceEntry node : container.sm.entries.values()) {
            if (node.needsPostProcessing) { // only process those nodes that have not been visited yet
                detectCycle(node, stack, dependencies);
            }
        }

        // Process nodes in children
        for (var e = container.treeFirstChild; e != null; e = e.treeNextSiebling) {
            dependencyCyclesFind(stack, dependencies, e);
        }
    }

    public static void dependencyCyclesFind(ContainerSetup container) {
        ArrayDeque<ServiceEntry> stack = new ArrayDeque<>();
        ArrayDeque<ServiceEntry> dependencies = new ArrayDeque<>();
        dependencyCyclesFind(stack, dependencies, container);
    }

    /**
     * Recursively invoked for each node.
     *
     * @param stack
     *            the stack of all visited dependencies so far
     * @param dependencies
     *            the stack of locally visited dependencies so far
     * @param entry
     *            the node to visit
     * @return stuff
     * @throws BuildException
     *             if there is a cycle in the graph
     */
    private static void detectCycle(ServiceEntry entry, ArrayDeque<ServiceEntry> stack, ArrayDeque<ServiceEntry> dependencies) {
        ProvidedService ps = entry.provider;
        ServiceBindingSetup binding = entry.bindings;
        if (ps == null || binding == null) {
            return; // leaf
        }

        // We have both bindings and provide

        stack.push(entry);
        while (binding != null) {
            BeanSetup bean = binding.operation.bean;
            for (ProvidedService psDep : bean.providingOperations) {
                ServiceEntry next = psDep.entry;
                if (!next.needsPostProcessing) {
                    continue;
                }

                dependencies.push(next);
                if (stack.contains(next)) {
                    // clear links not part of the circle, for example, for A->B->C->B we want to remove A
                    while (stack.peekLast() != next) {
                        stack.pollLast();
                        dependencies.pollLast();
                    }
                    StringBuilder sb = new StringBuilder("Circular dependencies between " + stack.size() + " services: ");
                    if (stack.size() == 2) {
                        sb.append(stack.pollLast().key.toStringSimple());
                        sb.append(" <-> ");
                        sb.append(stack.pollLast().key.toStringSimple());
                    } else {
                        ServiceEntry e = stack.pollLast();
                        do {
                            sb.append(e.key);
                            if (!dependencies.isEmpty()) {
                                sb.append(" -> ");
                            }
                        } while ((e = dependencies.pollLast()) != null);
                    }
                    throw new CircularDependencyException(sb.toString());
                }
                detectCycle(next, stack, dependencies);
                dependencies.pop();
            }

            binding = binding.nextFriend;
        }
        stack.pop();
    }

}
