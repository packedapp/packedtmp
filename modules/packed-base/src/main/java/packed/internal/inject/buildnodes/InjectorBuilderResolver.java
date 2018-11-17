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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionException;
import app.packed.util.MethodDescriptor;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.Node;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/**
 *
 */
public class InjectorBuilderResolver {
    // TODO also check no injection of prototype beans into singleton, after we have resolved

    public static void resolveAllDependencies(InjectorBuilder b) {
        b.detectCyclesFor = new ArrayList<>();

        for (BuildNode<?> node : b.c.privateBuildNodeList) {
            node.freeze();

            if (node.needsResolving()) {
                b.detectCyclesFor.add(node);
                List<InternalDependency> dependencies = node.getDependencies();
                for (int i = 0; i < dependencies.size(); i++) {
                    Dependency dependency = dependencies.get(i);
                    Node<?> resolveTo = null;

                    // Special handling for mixins. They are allowed to be inner classes, without the outer class
                    // needing to be exposed as a service
                    // Also we need to check it first because, we should always override any exposed service with the same key
                    // if (i == 0 && node instanceof BuildNodeOldFactory) {
                    // BuildNodeOldFactory<?> factory = (BuildNodeOldFactory<?>) node;
                    // if (factory.componentInstanceId > 0) {// A mixin
                    // BuildNodeInstanceOrFactory<?> parent = factory.componentConfiguration.getNodes().get(0);
                    // if (factory.mirror.isInnerClassOf(parent.mirror.getType())) {
                    // resolveTo = parent;
                    // }
                    // }
                    // }

                    // See if we have a matching service in the node map.
                    if (resolveTo == null) {
                        resolveTo = b.c.privateBuildNodeMap.getNode(dependency);

                        // Did not find service of the specified type
                        if (resolveTo == null) {
                            // Long long error message
                            StringBuilder sb = new StringBuilder();
                            sb.append("Cannot resolve dependency for ");
                            if (dependencies.size() == 1) {
                                sb.append("single ");
                            }
                            sb.append("parameter on ");
                            if (dependency.getVariable() != null) {

                                InternalExecutableDescriptor e = (InternalExecutableDescriptor) ((InternalParameterDescriptor) dependency.getVariable().get())
                                        .getDeclaringExecutable();
                                sb.append(e.descriptorTypeName()).append(": ");
                                sb.append(e.getDeclaringClass().getCanonicalName());
                                if (e instanceof MethodDescriptor) {
                                    sb.append("#").append(((MethodDescriptor) e).getName());
                                }
                                sb.append("(");
                                if (dependencies.size() > 1) {
                                    StringJoiner sj = new StringJoiner(", ");
                                    for (int j = 0; j < dependencies.size(); j++) {
                                        if (j == i) {
                                            sj.add("-> " + dependency.getKey().toString() + " <-");
                                        } else {
                                            sj.add(dependencies.get(j).getKey().getTypeLiteral().getRawType().getSimpleName());
                                        }
                                    }
                                    sb.append(sj.toString());
                                } else {
                                    sb.append(dependency.getKey().toString());
                                    sb.append(" ");
                                    sb.append(dependency.getVariable().get().getName());
                                }
                                sb.append(")");
                            }
                            System.err.println(b.c.privateBuildNodeList.stream().map(e -> e.getKey()).collect(Collectors.toList()));
                            throw new InjectionException(sb.toString());
                        }

                    }
                    node.resolvedDependencies[i] = requireNonNull(resolveTo);
                }
            }

            // Cannot resolve dependency for constructor stubs.Letters.XY(** stubs.Letters.YX **, String, Foo)
        }

        b.c.privateBuildNodeList.forEach(n -> n.checkResolved());// An extra check for now
    }

}
