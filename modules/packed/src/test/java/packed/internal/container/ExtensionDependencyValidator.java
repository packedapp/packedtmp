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
package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import app.packed.container.Extension;
import app.packed.container.InternalExtensionException;
import packed.internal.util.StringFormatter;

/**
 * This class is used validating that there are no cycles between extensions. We do not allow circle in the extension
 * dependency graph as this would lead to a nondeterministic configuration order of extensions.
 * <p>
 * We do not cache exceptions
 */

// I think the new one is simpler...??
final class ExtensionDependencyValidator {

    private static final Map<Class<? extends Extension<?>>, List<Class<? extends Extension<?>>>> VALIDATED = Collections.synchronizedMap(new WeakHashMap<>());

    private static Node collect(Class<? extends Extension<?>> extensionClass, IdentityHashMap<Class<? extends Extension<?>>, Node> nodes) {
        Node n = nodes.get(extensionClass);
        if (n == null) {
            nodes.put(extensionClass, n = new Node(extensionClass));
            for (int i = 0; i < n.nodes.length; i++) {
                n.nodes[i] = collect(n.dependencies.get(i), nodes);
            }
        }
        return n;
    }

    static List<Class<? extends Extension<?>>> dependenciesOf(Class<? extends Extension<?>> extensionClass) {
        List<Class<? extends Extension<?>>> result = VALIDATED.get(extensionClass);
        if (result != null) {
            return result;
        }
        return dependenciesOf0(extensionClass);
    }

    static List<Class<? extends Extension<?>>> dependenciesOf0(Class<? extends Extension<?>> extensionClass) {
        // Create nodes and resolve edges
        IdentityHashMap<Class<? extends Extension<?>>, Node> nodes = new IdentityHashMap<>();
        collect(extensionClass, nodes);

        // Run Tarjan's strongly connected components
        // TarjanSCC tscc = new TarjanSCC();
        for (Node n : nodes.values()) {
            if (n.index == 0) {
                // We disable it for now
                // tscc.scc(n);
            }
        }

        // No circles was found, add all extensions to validated.
        for (Node n : nodes.values()) {
            // We add an unmodifiable linked set too m
            VALIDATED.putIfAbsent(n.extensionClass, n.dependencies);
        }

        return VALIDATED.get(extensionClass);
    }

    private static class Node {
        private final List<Class<? extends Extension<?>>> dependencies;
        final Class<? extends Extension<?>> extensionClass;
        private int index;

        private int lowLink;
        private final Node[] nodes;
        private boolean onStack;

        Node(Class<? extends Extension<?>> extensionClass) {
            this.extensionClass = requireNonNull(extensionClass);
            this.dependencies = ExtensionUtil.fromUseExtension(extensionClass);
            for (Class<? extends Extension<?>> c : dependencies) {
                if (c == extensionClass) {
                    throw new InternalExtensionException("Extension " + StringFormatter.format(extensionClass) + " cannot depend on itself via " + c);
                }
            }
            this.nodes = new Node[dependencies.size()];
        }

        @Override
        public String toString() {
            return extensionClass.getCanonicalName();
        }
    }

    static class TarjanSCC {

        private int index = 1;

        private final ArrayDeque<Node> stack = new ArrayDeque<>();

        void scc(Node n) {
            n.index = n.lowLink = index++;
            stack.push(n);
            n.onStack = true;
            for (Node nn : n.nodes) {
                if (nn != null) {
                    if (nn.index == 0) {
                        scc(nn);
                        n.lowLink = Math.min(n.lowLink, nn.lowLink);
                    } else if (nn.onStack) {
                        n.lowLink = Math.min(n.lowLink, nn.index);
                    }
                }
            }

            // System.out.println();
            // System.out.println("Finishing " + n);
            // System.out.println(stack.stream().map(e -> e.extensionClass.getCanonicalName()).collect(Collectors.joining(", ")));

            if (n.index == n.lowLink) {
                if (stack.size() > 1 && stack.peek() != n) {
                    // System.out.println("Stack size " + stack.size() + " for node " + n);
                    ArrayList<Class<? extends Extension<?>>> scc = new ArrayList<>();
                    Node n1 = stack.pop();
                    // System.out.println("Popping " + n1);
                    while (n1 != n) {
                        scc.add(n1.extensionClass);
                        n1 = stack.pop();
                        // System.out.println("Popping " + n1);
                    }
                    scc.add(n1.extensionClass);
                    if (scc.size() > 1) {
                        throw new InternalExtensionException("There is a dependency circle between multiple extensions" + scc);
                    }
                } else {
                    stack.pop();
                }
            }
        }
    }
}
