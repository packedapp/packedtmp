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

import java.util.ArrayList;
import java.util.List;

import app.packed.inject.Dependency;
import packed.internal.box.Box;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.ServiceNode;

/**
 *
 */
class DependencyGraphResolver {
    // Requirements -> cannot require any exposed services, or internally registered services...

    static void resolveAllDependencies(DependencyGraph graph) {
        graph.detectCyclesFor = new ArrayList<>();
        Box box = graph.root.box;

        for (ServiceNode<?> nn : graph.root.privateNodeMap) {
            ServiceBuildNode<?> node = (ServiceBuildNode<?>) nn;
            node.freeze();// Should be frozen, maybe change to an assert

            if (node.needsResolving()) {
                graph.detectCyclesFor.add(node);
                List<InternalDependency> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    Dependency dependency = dependencies.get(i);
                    ServiceNode<?> resolveTo = graph.root.privateNodeMap.getNode(dependency);

                    if (resolveTo == null) {
                        box.services().recordMissingDependency(node, dependency);
                    } else {
                        node.resolvedDependencies[i] = resolveTo;
                    }
                }
                // Cannot resolve dependency for constructor stubs.Letters.XY(** stubs.Letters.YX **, String, Foo)
            }
        }
        box.services().checkForMissingDependencies();
        // b.root.privateNodeMap.forEach(n -> ((ServiceBuildNode<?>) n).checkResolved());
    }

}
