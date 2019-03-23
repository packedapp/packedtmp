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

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import app.packed.inject.Dependency;
import app.packed.inject.InjectionException;
import app.packed.util.MethodDescriptor;
import packed.internal.inject.InternalDependency;
import packed.internal.inject.ServiceNode;
import packed.internal.util.descriptor.InternalExecutableDescriptor;
import packed.internal.util.descriptor.InternalParameterDescriptor;

/**
 *
 */
class DependencyGraphResolver {
    // Requirements -> cannot require any exposed services, or internally registered services...

    static void resolveAllDependencies(DependencyGraph b) {
        b.detectCyclesFor = new ArrayList<>();

        for (ServiceNode<?> nn : b.root.privateNodeMap) {
            ServiceBuildNode<?> node = (ServiceBuildNode<?>) nn;
            node.freeze();// Should be frozen, maybe change to an assert

            if (node.needsResolving()) {
                b.detectCyclesFor.add(node);
                List<InternalDependency> dependencies = node.dependencies;
                for (int i = 0; i < dependencies.size(); i++) {
                    Dependency dependency = dependencies.get(i);
                    ServiceNode<?> resolveTo = null;

                    // See if we have a matching service in the node map.
                    if (resolveTo == null) {
                        resolveTo = b.root.privateNodeMap.getNode(dependency);

                        // Did not find service of the specified type
                        if (resolveTo == null) {
                            if (node.autoRequires) {
                                if (dependency.isOptional()) {
                                    b.root.requiredServicesOptionally.add(dependency.key());
                                } else {
                                    b.root.requiredServicesMandatory.add(dependency.key());
                                }
                            } else {

                                // Long long error message
                                StringBuilder sb = new StringBuilder();
                                sb.append("Cannot resolve dependency for ");
                                if (dependencies.size() == 1) {
                                    sb.append("single ");
                                }
                                sb.append("parameter on ");
                                if (dependency.variable() != null) {

                                    InternalExecutableDescriptor e = (InternalExecutableDescriptor) ((InternalParameterDescriptor) dependency.variable().get())
                                            .declaringExecutable();
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
                                                sj.add("-> " + dependency.key().toString() + " <-");
                                            } else {
                                                sj.add(dependencies.get(j).key().typeLiteral().getRawType().getSimpleName());
                                            }
                                        }
                                        sb.append(sj.toString());
                                    } else {
                                        sb.append(dependency.key().toString());
                                        sb.append(" ");
                                        sb.append(dependency.variable().get().getName());
                                    }
                                    sb.append(")");
                                }
                                // b.root.requiredServicesMandatory.add(e.get)
                                System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
                                throw new InjectionException(sb.toString());
                            }
                        }

                    }
                    if (!node.autoRequires || resolveTo != null) {
                        node.resolvedDependencies[i] = requireNonNull(resolveTo);
                    }
                }
                // Cannot resolve dependency for constructor stubs.Letters.XY(** stubs.Letters.YX **, String, Foo)
            }
        }
        b.root.privateNodeMap.forEach(n -> ((ServiceBuildNode<?>) n).checkResolved());
    }

    static class UnresolvedDependency {
        ServiceBuildNode<?> node;
        Dependency dependency;

        public void fail() {
            StringBuilder sb = new StringBuilder();
            sb.append("Cannot resolve dependency for ");
            if (node.dependencies.size() == 1) {
                sb.append("single ");
            }
            sb.append("parameter on ");
            if (dependency.variable() != null) {

                InternalExecutableDescriptor e = (InternalExecutableDescriptor) ((InternalParameterDescriptor) dependency.variable().get())
                        .declaringExecutable();
                sb.append(e.descriptorTypeName()).append(": ");
                sb.append(e.getDeclaringClass().getCanonicalName());
                if (e instanceof MethodDescriptor) {
                    sb.append("#").append(((MethodDescriptor) e).getName());
                }
                sb.append("(");
                if (node.dependencies.size() > 1) {
                    StringJoiner sj = new StringJoiner(", ");
                    for (int j = 0; j < node.dependencies.size(); j++) {
                        if (dependency == node.dependencies.get(j) /* j == i */) {
                            sj.add("-> " + dependency.key().toString() + " <-");
                        } else {
                            sj.add(node.dependencies.get(j).key().typeLiteral().getRawType().getSimpleName());
                        }
                    }
                    sb.append(sj.toString());
                } else {
                    sb.append(dependency.key().toString());
                    sb.append(" ");
                    sb.append(dependency.variable().get().getName());
                }
                sb.append(")");
            }
            // b.root.requiredServicesMandatory
            // System.err.println(b.root.privateNodeMap.stream().map(e -> e.key()).collect(Collectors.toList()));
            throw new InjectionException(sb.toString());
        }
    }

}
