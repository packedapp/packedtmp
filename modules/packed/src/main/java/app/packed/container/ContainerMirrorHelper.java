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
package app.packed.container;

import app.packed.base.Nullable;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/** A helper class for creating new {@link ExtensionMirror} instances. */
final class ContainerMirrorHelper {

    /** A ExtensionMirror class to Extension class mapping. */
    private final static ClassValue<Class<? extends Extension<?>>> EXTENSION_TYPES = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionMirror.class);

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            // Extract the type of extension from ExtensionMirror<E>
            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) TYPE_LITERAL_EP_EXTRACTOR.extractProperSubClassOf(type,
                    Extension.class, InternalExtensionException::new);

            // Check that the mirror is in the same module as the extension itself
            if (extensionClass.getModule() != type.getModule()) {
                throw new InternalExtensionException("The extension mirror " + type + " must be a part of the same module (" + extensionClass.getModule()
                        + ") as " + extensionClass + ", but was part of '" + type.getModule() + "'");
            }

            return ExtensionDescriptor.of(extensionClass).type(); // Check that the extension is valid
        }
    };
    
    /** No help for you. */
    private ContainerMirrorHelper() {}

    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static ExtensionMirror<?> newMirror(ExtensionSetup extension, Class<? extends ExtensionMirror<?>> expectedMirrorClass) {
        ExtensionMirror<?> mirror = extension.instance().newExtensionMirror();

        // Cannot return a null mirror
        if (mirror == null) {
            throw new InternalExtensionException(
                    "Extension " + extension.model.fullName() + " returned null from " + extension.model.name() + ".newExtensionMirror()");
        }

        // If we expect a mirror of a particular type, check it
        if (expectedMirrorClass != null) {
            // Fail if the type of mirror returned by the extension does not match the specified mirror type
            if (!expectedMirrorClass.isInstance(mirror)) {
                throw new InternalExtensionException(extension.extensionType.getSimpleName() + ".newExtensionMirror() was expected to return an instance of "
                        + expectedMirrorClass + ", but returned an instance of " + mirror.getClass());
            }
        } else if (mirror.getClass() != ExtensionMirror.class) {
            // Extensions are are allowed to return ExtensionMirror from newExtensionMirror in which case we have no additional
            // checks

            // If expectedMirrorClass == null we don't know what type of mirror to expect. Other than it must be parameterized with
            // the right extension

            // Must return a mirror for the same extension
            Class<? extends Extension<?>> mirrorExtensionType = EXTENSION_TYPES.get(mirror.getClass());
            if (mirrorExtensionType != extension.extensionType) {
                throw new InternalExtensionException(
                        "Extension " + extension.model.fullName() + " returned a mirror for another extension, other extension type: " + mirrorExtensionType);
            }
        }

        mirror.initialize(new ExtensionNavigatorImpl(extension, extension.extensionType));
        return mirror;
    }

    /**
     * Create a new mirror for extension
     * 
     * @param extension
     *            the extension
     * @return the new mirror
     */
    static ExtensionMirror<?> newMirrorOfUnknownType(ExtensionSetup extension) {
        return newMirror(extension, null);
    }

    /**
     * Creates a new mirror if an. Otherwise returns {@code null}
     * 
     * @param container
     *            the container to test for presence extension may be present
     * @param mirrorClass
     *            the type of mirror to return
     * @return a mirror of the specified type or null if no extension of the matching type was used in the container
     */
    @Nullable
    static ExtensionMirror<?> newMirrorOrNull(ContainerSetup container, Class<? extends ExtensionMirror<?>> mirrorClass) {
        // First find what extension the mirror belongs to by extracting <E> from ExtensionMirror<E extends Extension>
        Class<? extends Extension<?>> extensionClass = EXTENSION_TYPES.get(mirrorClass);

        // See if the container uses the extension.
        ExtensionSetup extension = container.extensions.get(extensionClass);

        return extension == null ? null : newMirror(extension, mirrorClass);
    }
}
