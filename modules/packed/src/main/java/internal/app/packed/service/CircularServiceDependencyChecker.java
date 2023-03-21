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
package internal.app.packed.service;

import java.util.ArrayDeque;
import java.util.ArrayList;

import app.packed.service.CircularDependencyException;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;

/**
 * A service multi-composer is responsible for managing 1 or more {@link ServiceManager0 service composers} that are
 * directly connected and part of the same build.
 * <p>
 * This class server two main purposes:
 *
 * Finds dependency circles either within the same container or across containers that are not in a parent-child
 * relationship.
 *
 * Responsible for invoking the callback for every DependencyNode. We do this here, because we guarantee that all
 * dependants of a dependant are always invoked before the dependant itself.
 */
public final class CircularServiceDependencyChecker {

    private static void dependencyCyclesFind(ArrayDeque<ServiceSetup> stack, ArrayDeque<ServiceSetup> dependencies, ContainerSetup container) {
        for (ServiceSetup node : container.sm.entries.values()) {
            if (!node.hasBeenCheckForDependencyCycles) { // only process those nodes that have not been visited yet
                detectCycle(node, stack, dependencies);
            }
        }

        // Process nodes in children
        for (ContainerSetup e = container.treeFirstChild; e != null; e = e.treeNextSibling) {
            dependencyCyclesFind(stack, dependencies, e);
        }
    }

    public static void dependencyCyclesFind(ContainerSetup container) {
        ArrayDeque<ServiceSetup> stack = new ArrayDeque<>();
        ArrayDeque<ServiceSetup> dependencies = new ArrayDeque<>();
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
    private static void detectCycle(ServiceSetup entry, ArrayDeque<ServiceSetup> stack, ArrayDeque<ServiceSetup> dependencies) {
        ServiceProviderSetup ps = entry.provider();
        ArrayList<ServiceBindingSetup> bindings = entry.bindings;
        if (ps == null || bindings.isEmpty()) {
            return; // leaf
        }

        // We have both bindings and provide

        stack.push(entry);
        for (ServiceBindingSetup binding : bindings) {
            BeanSetup bean = binding.operation.bean;
            for (ServiceProviderSetup psDep : bean.serviceProviders) {
                ServiceSetup next = psDep.entry;
                if (next.hasBeenCheckForDependencyCycles) {
                    continue;
                }

                dependencies.push(next);
                if (stack.contains(next)) {
                    // clear links not part of the circle, for example, for A->B->C->B we want to remove A
                    while (stack.peekLast() != next) {
                        stack.pollLast();
                        dependencies.pollLast();
                    }

                    // Create a proper error me
                    String errorMsg = createErrorMessage(dependencies);
                    throw new CircularDependencyException(errorMsg);
                }
                detectCycle(next, stack, dependencies);
                dependencies.pop();
            }

        }
        stack.pop();

    }

    private static String createErrorMessage(ArrayDeque<ServiceSetup> dependencies) {
        int size = dependencies.size();
        StringBuilder sb = new StringBuilder("Circular dependencies between " + size + " services: ");
        if (size == 2) {
            sb.append(dependencies.pollLast().key);
            sb.append(" <-> ");
            sb.append(dependencies.pollLast().key);
        } else {
            ServiceSetup e = dependencies.pollLast();
            do {
                sb.append(e.key);
                if (!dependencies.isEmpty()) {
                    sb.append(" -> ");
                }
            } while ((e = dependencies.pollLast()) != null);
        }
        return sb.toString();
    }
}

// From cake

//StringBuilder sb = new StringBuilder("Cyclic dependency: ");
//InjectionGraphDependency s = dependencies.pollLast();
//InternalInjectionSite edge = s.site;
//sb.append(formatClass(edge.getActualType()));
//do {
//    edge = s.site;
//    sb.append(" -");
//    if (edge.getAnnotation() != null) { // show which annotation caused the link dependency
//        sb.append("[via @" + edge.getAnnotation().annotationType().getSimpleName() + "]");
//    }
//    sb.append("> ").append(formatClass(edge.getType()));
//} while ((s = dependencies.pollLast()) != null);
//return sb.toString();