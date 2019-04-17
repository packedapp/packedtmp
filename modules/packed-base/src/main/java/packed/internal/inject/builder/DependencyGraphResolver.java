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

import app.packed.inject.DependencyDescriptor;
import packed.internal.box.BoxServices;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.ServiceNode;

/**
 *
 */
class DependencyGraphResolver {
    // Requirements -> cannot require any exposed services, or internally registered services...

    static void resolveAllDependencies(DependencyGraph graph) {
        graph.detectCyclesFor = new ArrayList<>();

        BoxServices services = graph.root.box.services();

        for (ServiceNode<?> nn : graph.root.privateNodeMap) {
            ServiceBuildNode<?> node = (ServiceBuildNode<?>) nn;
            node.freeze();// Should be frozen, maybe change to an assert

            if (node.needsResolving()) {
                graph.detectCyclesFor.add(node);
                List<InternalDependency> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    DependencyDescriptor dependency = dependencies.get(i);
                    ServiceNode<?> resolveTo = graph.root.privateNodeMap.getNode(dependency);
                    services.recordDependencyResolved(node, dependency, resolveTo, false);
                    node.resolvedDependencies[i] = resolveTo;
                }
            }
        }
        services.checkForMissingDependencies();
        // b.root.privateNodeMap.forEach(n -> ((ServiceBuildNode<?>) n).checkResolved());
    }

}
