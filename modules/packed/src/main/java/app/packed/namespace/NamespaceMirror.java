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

import java.util.stream.Stream;

import app.packed.component.Authority;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import internal.app.packed.container.NamespaceSetup;

/**
 * A mirror of a namespace.
 */
// Kan maaske have en EventRouter? DeliveredEvent = <Domain, Event>
// Namespace:Cli:main
// CliExtension.CliCommand:...
// ServiceNamespace::/:main

// Is abstract for now. Similar to ExtensionMirror

// I think a namespace can have restrictions Map<Permission, Set<Container>>

public abstract class NamespaceMirror<E extends Extension<E>> implements ComponentMirror {

    /*
     * A namespace does not lists the types of resource keys. As namespaces might have multiple resource types. For example,
     * CLI has both arguments and commands that are unique. And Hibernate could technically have both Table Names and
     * EntityClasses <p> A namespace does not have an owner. For example, if two extensions uses an extension who of them
     * are the owner <p>
     **/

    /** The namespace configuration. */
    private final NamespaceSetup namespace;

    protected NamespaceMirror() {
        this.namespace = NamespaceSetup.MI.initialize();

    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath componentPath() {
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

    // IDK, define is also a bad word
    // Should be similar named as bean.operator(), operation.operator
    // Maybe we do allow user namespaces...
    /** {@return the extension class that owns the namespace.} */
    Class<? extends Extension<?>> namespaceExtension() {
        return namespace.root.extensionType;
    }

    /**
     * {@return the local name of this namespace for the specified container.}
     * <p>
     * Domain instance may be available with different names in different containers.
     *
     * @param container
     *            the container to return a local name for
     * @throws IllegalArgumentException
     *             if this namespace instance is not available in the specified container
     */
    public final String namespaceLocalName(ContainerMirror container) {
        throw new UnsupportedOperationException();
    }

    /** {@return the name of this name.} */
    public final String namespaceName() {
        return namespace.name;
    }

    /** {@return the owner of this namespace instance.} */
    // I don't think there is an owner.
    // What if a database is used by two extensions only?
    // Maybe the DatabaseExtension itself??
    public final Authority namespaceOwner() {
        throw new UnsupportedOperationException();
    }

    /** {@return the root container of the namespace.} */
    public final ContainerMirror namespaceRoot() {
        return namespace.root.container.mirror();
    }

    /** {@return a tree containing every container where this namespace instance is present.} */
    // And this is where I think we have 2 things.
    // One where it is available, and one where it is active. And by used I mean???
    // Hmm, A reference to one of its elements?? Hmm. Maybe where it is used it not very well defined
    public final ContainerMirror.OfTree namespaceScope() {
        throw new UnsupportedOperationException();
    }


    // All containers that are active, are containers that use the namespace in some way
    // PartialTreeView
    public final Stream<ContainerMirror> namespaceActiveContainers() {
        throw new UnsupportedOperationException();
    }
}

class ZamaspaceMirrorArchive<E extends Extension<E>> extends NamespaceMirror<E> {

}