/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.Extension;
import app.packed.namespace.NamespaceMirror;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.util.AbstractNamedTreeNode;
import internal.app.packed.util.accesshelper.NamespaceMirrorAccessHandler;

/**
 *
 */
public class NamespaceSetup extends AbstractNamedTreeNode<NamespaceSetup> {

    public final ContainerSetup rootContainer;

    /** The lazy generated namespace mirror. */
    private final Supplier<NamespaceMirror> mirror = StableValue.supplier(() -> NamespaceMirrorAccessHandler.instance().newNamespaceMirror(this));

    public final ComponentRealm owner;

    NamespaceSetup(ContainerSetup container, PackedNamespaceInstaller installer) {
        super(null);
        this.rootContainer = requireNonNull(container);
        this.owner = ComponentRealm.userland();
    }

    protected NamespaceSetup(ApplicationSetup application, Class<? extends Extension<?>> extensionClass) {
        super(application.container().namespace);
        this.rootContainer = requireNonNull(application.container());
        this.owner = ComponentRealm.extension(extensionClass);
    }

    /** {@inheritDoc} */
    public ComponentPath componentPath() {
        List<String> path = new ArrayList<>();
        NamespaceSetup currentNode = this;

        while (currentNode != null) {
            path.add(currentNode.name); // Add the current node's name
            currentNode = currentNode.treeParent; // Move to the parent
        }

        Collections.reverse(path);

        return ComponentKind.NAMESPACE.pathNew(rootContainer.application.componentPath(), path);
    }

    /**
     * @return
     */
    public NamespaceMirror mirror() {
        return mirror.get();
    }
}
