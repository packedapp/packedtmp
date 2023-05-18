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
package app.packed.namespace;

import app.packed.container.Author;
import app.packed.container.ContainerMirror;
import app.packed.container.ContainerTreeMirror;
import app.packed.extension.Extension;
import internal.app.packed.container.Mirror;
import internal.app.packed.container.NamespaceSetup;

/**
 * A mirror of a namespace.
 */
// Kan maaske have en EventRouter? DeliveredEvent = <Domain, Event>
// Namespace:Cli:main
// CliExtension.CliCommand:...

// ServiceNamespace::/:main
public class NamespaceMirror<E extends Extension<E>> implements Mirror {

    /** The domain configuration. */
    private final NamespaceSetup namespace = NamespaceSetup.MI.initialize();

    // IDK, define is also a bad word
    /** {@return the extension class that defines the namespace.} */
    Class<? extends Extension<?>> namespaceExtension() {
        return namespace.root.extensionType;
    }

    /**
     * {@return the local name of this domain for the specified container.}
     * <p>
     * Domain instance may be available with different names in different containers.
     *
     * @param container
     *            the container to return a local name for
     * @throws IllegalArgumentException
     *             if this domain instance is not available in the specified container
     */
    public final String namespaceLocalName(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }

    /** {@return the name of this domain.} */
    public final String namespaceName() {
        return namespace.name;
    }

    /** {@return the owner of this domain instance.} */
    public final Author namespaceOwner() {
        return namespace.owner.author();
    }

    /** {@return the root container of the domain.} */
    public final ContainerMirror namespaceRoot() {
        return namespace.root.container.mirror();
    }

    /** {@return a tree containing every container where this domain instance is present.} */
    public final ContainerTreeMirror namespaceScope() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return other instanceof NamespaceMirror<?> m && getClass() == m.getClass() && namespace == m.namespace;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return namespace.hashCode();
    }
}
// nameSpaceIDClass -> Nah for a database is it the table name? A class if hibernate?
// Also, for example, for CLI both arguments and commands are unique. So 2 "keys"
