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

import java.util.HashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import app.packed.component.ComponentHandle;
import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint.ExtensionPointHandle;
import app.packed.operation.OperationHandle;
import app.packed.util.TreeView;
import internal.app.packed.namespace.NamespaceSetup;
import internal.app.packed.namespace.PackedNamespaceInstaller;
import internal.app.packed.util.accesshelper.AccessHelper;
import internal.app.packed.util.accesshelper.NamespaceAccessHandler;

/**
 *
 * Instances of this class should never be exposed to non-trusted code.
 */
public abstract non-sealed class NamespaceHandle<E extends Extension<E>, C extends NamespaceConfiguration<E>> extends ComponentHandle {

    /** A cache of all namespace configurations, currently do not support namespaces uses by extensions. */
    private HashMap<E, C> configurations = new HashMap<>();

    /** The lazy generated namespace mirror. */
    private final Supplier<NamespaceMirror<E>> mirror = StableValue.supplier(() -> newNamespaceMirror());

    /** The namespace configuration. */
    final NamespaceSetup namespace;

    protected NamespaceHandle(NamespaceInstaller<?> installer) {
        this.namespace = requireNonNull(((PackedNamespaceInstaller<?>) installer).toSetup());
    }

    /** {@return the root extension of this domain.} */
    @SuppressWarnings("unchecked")
    public final E applicationRootExtension() {
        return (E) namespace.root.root().instance();
    }

    public final ComponentRealm componentOwner() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final ComponentPath componentPath() {
        return namespace.componentPath();
    }

    /** {@inheritDoc} */
    @Override
    public final void componentTag(String... tags) {
        checkIsOpen();
        namespace.container().application.componentTags.addComponentTags(namespace, tags);
    }

    /**
     * Returns a configuration for this namespace for the specific extension.
     *
     * @param e
     *            the extension to return a
     * @return
     */
    public final C configuration(E e) {
        return configurations.computeIfAbsent(e, k -> newNamespaceConfiguration(k, ComponentRealm.userland()));
    }

    public final C configuration(E e, ExtensionPointHandle handle) {
        return configurations.computeIfAbsent(e, k -> newNamespaceConfiguration(k, handle.author()));
    }

    /** {@return a tree view of all the extensions in the namespace} */
    public final TreeView<E> extensions() {
        // A treeview of all
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isConfigurable() {
        return namespace.root.container.handle().isConfigurable();
    }

    public final boolean isInApplicationLifetime(Extension<?> extension) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isOpen() {
        return namespace.root.container.handle().isOpen();
    }

    /** {@inheritDoc} */
    @Override
    public final NamespaceMirror<E> mirror() {
        return mirror.get();
    }

    /** {@inheritDoc} */
    protected abstract C newNamespaceConfiguration(E extension, ComponentRealm actor);

    /**
     * <p>
     * Most namespace implementations will want to define a special namespace mirror. But it is fine to leave it alone when
     * implementing the namespace.
     *
     * @return the new mirror
     */
    protected NamespaceMirror<E> newNamespaceMirror() {
        return new NamespaceMirror<E>(this);
    }

    protected void onClose() {}

    // I think we have one per assembly
    protected void onConfigured(E rootExtension, boolean isNamespaceRoot) {}

    /** {@return a stream of all of the operations that have been declared in this namespace.} */
    public final Stream<OperationHandle<?>> operations() {
        return namespace.operations.stream().map(e -> e.handle());
    }

    /**
     * {@return a stream of all of the operations that have been declared in this namespace of the specified type.}
     *
     * @param <T>
     * @param handleType
     *            the type of operation handles to include in the stream
     */
    @SuppressWarnings("unchecked")
    public final <H extends OperationHandle<?>> Stream<H> operations(Class<H> handleType) {
        requireNonNull(handleType, "handleType is null");
        return (Stream<H>) operations().filter(f -> handleType.isAssignableFrom(f.getClass()));
    }

    /** {@return the root extension of this domain.} */
    @SuppressWarnings("unchecked")
    public final E rootExtension() {
        return (E) namespace.root.instance();
    }

    static {
        AccessHelper.initHandler(NamespaceAccessHandler.class, new NamespaceAccessHandler() {

            @Override
            public void invokeNamespaceOnNamespaceClose(NamespaceHandle<?, ?> handle) {
                handle.onClose();
            }

            @Override
            public NamespaceSetup getNamespaceHandleNamespace(NamespaceHandle<?, ?> handle) {
                return handle.namespace;
            }
        });
    }
}
