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
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceMirror;
import internal.app.packed.build.AuthoritySetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.extension.ExtensionSetup;
import internal.app.packed.handlers.NamespaceHandlers;
import internal.app.packed.operation.OperationSetup;

/**
 *
 */
// Is an application a namespace for Components??? My brain just fried
@SuppressWarnings("rawtypes")
public final class NamespaceSetup {

    /** The default name of a namespace. */
    public static final String DEFAULT_NAME = "main";

    NamespaceHandle handle;

    // Must search up until root to find local names
    final Map<ContainerSetup, String> localNames = new HashMap<>();

    /** The name of the namespace. */
    public String name = DEFAULT_NAME;

    /** All operations defined in this namespace. */
    public final ArrayList<OperationSetup> operations = new ArrayList<>();

    /** The owner of the name space. */
    public final AuthoritySetup owner;

    /** The extension and root container of the namespace. */
    public final ExtensionSetup root;

    /** The namespace template */
    public final PackedNamespaceTemplate template;

    public NamespaceSetup(PackedNamespaceTemplate template, ExtensionSetup root, AuthoritySetup owner) {
        this.template = template;
        this.root = root;
        this.owner = owner;
    }

    /** {@inheritDoc} */
    public ComponentPath componentPath() {
        throw new UnsupportedOperationException();
    }

    /** { @return the handle of the namespace} */
    public NamespaceHandle handle() {
        return requireNonNull(handle);
    }


    /** {@inheritDoc} */
    public NamespaceMirror<?> mirror() {
        return handle().mirror();
    }

    /** {@return the name of the namespace} */
    public String name() {
        return name;
    }

    public static NamespaceSetup crack(NamespaceHandle<?, ?> handle) {
        return NamespaceHandlers.getNamespaceHandleNamespace(handle);
    }

    public record NamespaceKey(Class<? extends NamespaceHandle<?, ?>> handleClass, String name) {}
}
