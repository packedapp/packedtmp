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
package internal.app.packed.container;

import java.util.HashMap;
import java.util.Map;

import app.packed.namespace.NamespaceMirror;
import app.packed.util.Nullable;
import internal.app.packed.bean.AuthorSetup;
import internal.app.packed.util.AbstractTreeNode;
import internal.app.packed.util.MagicInitializer;

/**
 *
 */
// Is an application a namespace for Components???
public final class NamespaceSetup {

    /** The default name of a namespace. */
    public static final String DEFAULT_NAME = "main";

    public static final MagicInitializer<NamespaceSetup> MI = MagicInitializer.of(NamespaceMirror.class);

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    /** The name of the namespace. */
    public String name = DEFAULT_NAME;

    /** The owner of the name space. */
    public final AuthorSetup owner;

    /** The extension and root container of the namespace. */
    public final ExtensionSetup root;

    /** The namespace template */
    public final PackedNamespaceTemplate<?> template;

    public NamespaceSetup(PackedNamespaceTemplate<?> template, ExtensionSetup root, AuthorSetup owner) {
        this.template = template;
        this.root = root;
        this.owner = owner;
    }

    public record NamespaceKey(Class<?> namespaceKind, String name) {}
    // Tror maaske vi har nogle strategies
    // AllApplication-> No nodes

    // Vi vil helst avoid node
    static class NamespaceNode extends AbstractTreeNode<NamespaceNode> {

        /**
         * @param treeParent
         */
        protected NamespaceNode(@Nullable NamespaceNode treeParent) {
            super(treeParent);
        }
    }
}
