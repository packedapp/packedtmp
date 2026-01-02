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

import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceMirror;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.component.ComponentSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;
import internal.app.packed.util.accesshelper.NamespaceAccessHandler;

/**
 *
 */
// Is an application a namespace for Components??? My brain just fried
@SuppressWarnings("rawtypes")
public final class NamespaceSetup implements ComponentSetup {

    /** The handle of the namespace must be read through {@link #handle()}. */
    private NamespaceHandle handle;

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    /** All operations defined in this namespace. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The owner of the name space. */
    public final AuthoritySetup owner;

    /** The extension and root container of the namespace. */
    public final ExtensionSetup root;

    /** The namespace template */
    public final PackedNamespaceTemplate template;

    private NamespaceSetup(PackedNamespaceInstaller installer) {
        this.template = installer.template;
        this.root = installer.root;
        this.owner = installer.owner;
    }

    public ContainerSetup container() {
        return root.container;
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
        throw new UnsupportedOperationException();
    }

    /** { @return the handle of the namespace} */
    @Override
    public NamespaceHandle handle() {
        return requireNonNull(handle);
    }

    /** {@inheritDoc} */
    @Override
    public NamespaceMirror<?> mirror() {
        return handle().mirror();
    }

    public static NamespaceSetup crack(NamespaceHandle<?, ?> handle) {
        return NamespaceAccessHandler.instance().getNamespaceHandleNamespace(handle);
    }

    /** {@inheritDoc} */
    static <H extends NamespaceHandle<?, ?>> H newNamespace(PackedNamespaceInstaller<H> installer) {
        NamespaceSetup namespace = installer.install(new NamespaceSetup(installer));

        @SuppressWarnings("unchecked")
        H handle = (H) installer.template.newHandle().apply(installer);
        namespace.handle = handle;

        installer.root.container.application.namespaces.put(installer.nk, handle);
        installer.handle = handle;
        installer.root.tree.namespacesToClose.add(namespace);
        return handle;
    }

    public record NamespaceKey(Class<? extends NamespaceHandle<?, ?>> handleClass, ComponentRealm realm) {}
}
