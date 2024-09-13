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

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

import app.packed.bean.BeanMirror;
import app.packed.component.Authority;
import app.packed.component.ComponentMirror;
import app.packed.component.ComponentPath;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import internal.app.packed.namespace.NamespaceSetup;

/** A mirror of a namespace. */
public non-sealed class NamespaceMirror<E extends Extension<E>> implements ComponentMirror {

    /** The namespace configuration. */
    private final NamespaceSetup namespace;

    public NamespaceMirror(NamespaceHandle<E, ?> handle) {
        this.namespace = handle.namespace;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return other instanceof NamespaceMirror m && getClass() == m.getClass() && namespace == m.namespace;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return namespace.hashCode();
    }

    public final Stream<BeanMirror> namespaceActiveBeans() {
        throw new UnsupportedOperationException();
    }

    // All containers that are active, are containers that use the namespace in some way
    // PartialTreeView
    public final Stream<ContainerMirror> namespaceActiveContainers() {
        throw new UnsupportedOperationException();
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
    // I think Available is the right thing to return
    public final ContainerMirror.OfTree namespaceScope() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected final <T extends NamespaceOperationMirror> Stream<T> operations(Class<T> operationType) {
        requireNonNull(operationType, "operationType is null");
        return (Stream) namespace.operations.stream().map(e -> e.mirror()).filter(f -> operationType.isAssignableFrom(f.getClass()));
    }
}

///** {@return the root extension in the namespace} */
//@SuppressWarnings("unchecked")
//protected E extensionRoot() {
//  return (E) namespace.root.instance();
//}
//
//// IDK, define is also a bad word
//// Should be similar named as bean.operator(), operation.operator
//// Maybe we do allow user namespaces...
///** {@return the extension class that owns the namespace.} */
//Class<? extends Extension<?>> namespaceExtension() {
//  return namespace.root.extensionType;
//}