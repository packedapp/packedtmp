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
package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.function.Function;

import app.packed.extension.Extension;
import app.packed.extension.ExtensionHandle;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.InternalExtensionException;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import internal.app.packed.handlers.ExtensionHandlers;
import internal.app.packed.namespace.NamespaceSetup.NamespaceKey;
import internal.app.packed.namespace.PackedNamespaceInstaller;
import internal.app.packed.namespace.PackedNamespaceTemplate;
import internal.app.packed.util.StringFormatter;
import internal.app.packed.util.types.TypeVariableExtractor;

/**
 *
 */
public record PackedExtensionHandle<E extends Extension<E>>(ExtensionSetup extension) implements ExtensionHandle {

    /** Maps an ExtensionPoint class to the type parameter (E). */
    public final static ClassValue<Class<? extends Extension<?>>> TYPE_VARIABLE_EXTRACTOR = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor EXTRACTOR = TypeVariableExtractor.of(ExtensionPoint.class);

        /** {@inheritDoc} */
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            return ExtensionModel.extractE(EXTRACTOR, type);
        }
    };


    /** {@inheritDoc} */
    @Override
    public final <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass) {
        requireNonNull(extensionPointClass, "extensionPointClass is null");

        // Extract the extension class (<E>) from ExtensionPoint<E>
        Class<? extends Extension<?>> otherExtensionClass = TYPE_VARIABLE_EXTRACTOR.get(extensionPointClass);

        // Check that the extension of requested extension point's is a direct dependency of this extension
        if (!extension.model.dependsOn(otherExtensionClass)) {
            // An extension cannot use its own extension point
            if (otherExtensionClass == extension.extensionType) {
                throw new InternalExtensionException(otherExtensionClass.getSimpleName() + " cannot use its own extension point " + extensionPointClass);
            }
            throw new InternalExtensionException(getClass().getSimpleName() + " must declare " + StringFormatter.format(otherExtensionClass)
                    + " as a dependency in order to use " + extensionPointClass);
        }

        ExtensionSetup otherExtension = extension.container.useExtension(otherExtensionClass, extension);

        PackedExtensionUseSite c = new PackedExtensionUseSite(otherExtension, extension);
        // Create a new extension point
        ExtensionPoint<?> newExtensionPoint = ExtensionHandlers.newExtensionPoint(otherExtension.instance(), c);

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


    @SuppressWarnings("unchecked")
    public final <T extends NamespaceHandle<E, ?>> T namespaceLazy(NamespaceTemplate template, String name, Function<NamespaceTemplate.Installer, T> factory) {
        NamespaceKey nk = new NamespaceKey(template.handleClass(), name);
        requireNonNull(factory);

        Map<NamespaceKey, NamespaceHandle<?, ?>> m = extension.container.application.namespaces;

        // cannot use computeIfAbsent, as we want to store the handle before the install method returns
        NamespaceHandle<?, ?> namespaceHandle = m.get(nk);
        if (namespaceHandle == null) {
            PackedNamespaceInstaller pni = new PackedNamespaceInstaller((PackedNamespaceTemplate) template, extension, extension, name);
            namespaceHandle = factory.apply(pni);
            if (namespaceHandle != pni.handle) {
                throw new InternalExtensionException("must return newly installed namespace handle");
            }
        }

        return (T) namespaceHandle;
    }
}
