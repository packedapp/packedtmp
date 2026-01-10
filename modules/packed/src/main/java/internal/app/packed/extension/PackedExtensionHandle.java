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
package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;

import app.packed.component.ComponentPath;
import app.packed.component.ComponentRealm;
import app.packed.extension.BaseExtension;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.InternalExtensionException;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.util.TreeView;
import internal.app.packed.ValueBased;
import internal.app.packed.namespace.NamespaceSetup.NamespaceKey;
import internal.app.packed.namespace.PackedNamespaceInstaller;
import internal.app.packed.namespace.PackedNamespaceTemplate;
import internal.app.packed.util.PackedTreeView;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.accesshelper.ExtensionAccessHandler;
import internal.app.packed.util.types.TypeVariableExtractor;

/** Implementation of {@link ExtensionHandle} */
@ValueBased
public record PackedExtensionHandle<E extends Extension<E>>(ExtensionSetup extension) implements ExtensionHandle<E> {

    @Override
    public void runOnCodegen(Runnable action) {
        checkIsConfigurable();
        extension.container.application.addCodegenAction(action);
    }

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     *
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    @Override
    public void checkIsConfigurable() {
        if (!extension.isConfigurable()) {
            throw new IllegalStateException(extension.extensionType + " is no longer configurable");
        }
    }

    /** Maps an ExtensionPoint class to the type parameter (E). */
    public final static ClassValue<Class<? extends Extension<?>>> TYPE_VARIABLE_EXTRACTOR = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(ExtensionPoint.class);

        /** {@inheritDoc} */
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionClassModel.extractE(EXTRACTOR, type);
        }
    };

    /** {@inheritDoc} */
    @Override
    public <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass) {
        requireNonNull(extensionPointClass, "extensionPointClass is null");

        // Extract the extension class (<E>) from ExtensionPoint<E>
        Class<? extends Extension<?>> otherExtensionClass = TYPE_VARIABLE_EXTRACTOR.get(extensionPointClass);

        // BaseExtension can do anything it wants
        if (extension.extensionType != BaseExtension.class) {
            // Check that the extension of requested extension point's is a direct dependency of this extension
            if (!extension.model.dependsOn(otherExtensionClass)) {
                // An extension cannot use its own extension point
                if (otherExtensionClass == extension.extensionType) {
                    throw new InternalExtensionException(otherExtensionClass.getSimpleName() + " cannot use its own extension point " + extensionPointClass);
                }
                throw new InternalExtensionException(getClass().getSimpleName() + " must declare " + StringFormatter.format(otherExtensionClass)
                        + " as a dependency in order to use " + extensionPointClass);
            }
        }

        ExtensionSetup otherExtension = extension.container.useExtension(otherExtensionClass, extension);

        PackedExtensionPointHandle c = new PackedExtensionPointHandle(otherExtension, extension);
        // Create a new extension point
        ExtensionPoint<?> newExtensionPoint = ExtensionAccessHandler.instance().invoke_Extension_NewExtensionPoint(otherExtension.instance(), c);

        if (newExtensionPoint == null) {
            throw new NullPointerException(
                    "Extension " + otherExtension.model.fullName() + " returned null from " + otherExtension.model.name() + ".newExtensionPoint()");
        }

        // Make sure it is a proper type of the requested extension point
        if (!extensionPointClass.isInstance(newExtensionPoint)) {
            throw new InternalExtensionException(otherExtension.extensionType.getSimpleName() + ".newExtensionPoint() was expected to return an instance of "
                    + extensionPointClass + ", but returned an instance of " + newExtensionPoint.getClass());
        }

        // Initializes the extension point

        return extensionPointClass.cast(newExtensionPoint);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <H extends NamespaceHandle<E, ?>> H namespaceLazy(NamespaceTemplate<H> template, ComponentRealm realm) {
        NamespaceKey nk = new NamespaceKey(template.handleClass(), realm);

        Map<NamespaceKey, NamespaceHandle<?, ?>> m = extension.container.application.namespaces;

        PackedNamespaceTemplate<H> t = (PackedNamespaceTemplate<H>) template;
        // cannot use computeIfAbsent, as we want to store the handle before the install method returns
        NamespaceHandle<?, ?> namespaceHandle = m.get(nk);
        if (namespaceHandle == null) {
            PackedNamespaceInstaller<H> installer = new PackedNamespaceInstaller<>((PackedNamespaceTemplate<H>) template, extension, extension, nk);

            installer.install();

            namespaceHandle = t.newHandle().apply(installer);
        }

        return (H) namespaceHandle;
    }

    /**
     * {@return an instance of this extension that is used in the application's root container. Will return this if this
     * extension is the root extension}
     */
    @Override
    @SuppressWarnings("unchecked")
    public E applicationRoot() {
        return (E) extension.root().instance();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public TreeView.Node<E> applicationNode() {
        ExtensionSetup es = extension.root();
        PackedTreeView<ExtensionSetup, E> tree = new PackedTreeView<>(es, null, e -> (E) e.instance());
        return tree.toNode(extension);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<E> parent() {
        ExtensionSetup parent = extension.treeParent;
        return parent == null ? Optional.empty() : Optional.of((E) parent.instance());
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExtensionUsed(Class<? extends Extension<?>> extensionType) {
        return extension.container.isExtensionUsed(extensionType);
    }

    /** {@inheritDoc} */
    @Override
    public ComponentPath containerPath() {
        return extension.container.componentPath();
    }
}
