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
package internal.app.packed.namespace;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.namespace.NamespaceHandle;
import app.packed.util.Nullable;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.AuthoritySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.AbstractTreeNode;

/**
 *
 */
// Is an application a namespace for Components??? My brain just fried
@SuppressWarnings("rawtypes")
public final class NamespaceSetup extends ComponentSetup {

    /** The default name of a namespace. */
    public static final String DEFAULT_NAME = "main";

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    /** The name of the namespace. */
    public String name = DEFAULT_NAME;

    /** The owner of the name space. */
    public final AuthoritySetup owner;

    /** The extension and root container of the namespace. */
    public final ExtensionSetup root;

    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The namespace template */
    public final PackedNamespaceTemplate template;

    NamespaceHandle handle;

    public final BiFunction<?, ?, ?> newConfiguration;

    public NamespaceSetup(PackedNamespaceTemplate template, ExtensionSetup root, AuthoritySetup owner, BiFunction<?, ?, ?> newConfiguration) {
        this.template = template;
        this.root = root;
        this.owner = owner;
        this.newConfiguration = newConfiguration;
    }

    public String name() {
        return name;
    }

    private NamespaceHandle handle() {
        return requireNonNull(handle);
    }
//    /** {@inheritDoc} */
//    @Override
//    public void named(String name) {
//        this.name = name;
//    }

    // Tror maaske vi har nogle strategies
    // AllApplication-> No nodes

    // Vi vil helst avoid node
    // Taenker vi bliver noedt til at gemme NameSpaceConfiguration...
    // Saa slipper ikek for den
    static class NamespaceNode extends AbstractTreeNode<NamespaceNode> {

        /**
         * @param treeParent
         */
        protected NamespaceNode(@Nullable NamespaceNode treeParent) {
            super(treeParent);
        }
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentMirror mirror() {
        return handle().namespaceMirror();
    }

    public record NamespaceKey(Class<? extends NamespaceHandle<?, ?>> handleClass, String name) {}
}
