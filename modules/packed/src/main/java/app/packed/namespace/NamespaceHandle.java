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

import java.util.function.BiFunction;
import java.util.stream.Stream;

import app.packed.component.ComponentHandle;
import app.packed.component.ComponentKind;
import app.packed.component.ComponentPath;
import app.packed.extension.Extension;
import app.packed.operation.OperationHandle;
import app.packed.util.TreeView;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.namespace.PackedNamespaceInstaller;

/**
 *
 * Instances of this class should never be exposed to non-trusted code.
 */
public abstract non-sealed class NamespaceHandle<E extends Extension<E>, C extends NamespaceConfiguration<E>> implements ComponentHandle {

    /** The domain configuration. */
    final NamespaceSetup namespace;

    protected NamespaceHandle(NamespaceTemplate.Installer installer) {
        this.namespace = ((PackedNamespaceInstaller) installer).namespace;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentKind componentKind() {
        return ComponentKind.NAMESPACE;
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return namespace.componentPath();
    }

    /** {@return a tree view of all the extensions in the namespace} */
    public final TreeView<E> extensions() {
        // A treeview of all
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return namespace.root.container.isConfigurable();
    }

    public final boolean isInApplicationLifetime(Extension<?> extension) {
        return true;
    }

    public final String name() {
        return namespace.name();
    }

    public NamespaceMirror<E> namespaceMirror() {
        // We could make this abstract. But nice when you develop new namespaces and don't have a mirror yet
        return new NamespaceMirror<E>(this);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final C newNamespaceConfiguration(E e) {
        BiFunction bi = namespace.newConfiguration;
        return (C) bi.apply(this, e);
    }

    protected void onAssemblyClose(E rootExtension, boolean isNamespaceRoot) {}

    /** {@return a stream of all of the operations declared in the namespace.} */
    public final Stream<OperationHandle<?>> operations() {
        return namespace.operations.stream().map(e -> e.handle());
    }

    /**
     * Returns a stream of all of the operations declared by the bean with the specified mirror type.
     *
     * @param <T>
     * @param operationType
     *            the type of operations to include
     * @return a collection of all of the operations declared by the bean of the specified type.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public final <T extends NamespaceOperationConfiguration> Stream<OperationHandle<T>> operations(Class<T> operationType) {
        requireNonNull(operationType, "operationType is null");
        return (Stream) operations().filter(f -> operationType.isAssignableFrom(f.configuration().getClass()));
    }
    /**
     * Returns a navigator for all extensions in the namespace.
     *
     * @return a navigator for all extensions in the namespace
     */
    // Ogsaa containere hvor den ikke noedvendig er brugt890[]?

    /** {@return the root extension of this domain.} */
    @SuppressWarnings("unchecked")
    public final E rootExtension() {
        return (E) namespace.root.instance();
    }
}
