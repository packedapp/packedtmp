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
package internal.app.packed.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.InternalExtensionException;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/** A helper class for creating new {@link ExtensionMirror} instances. */
final class ExtensionMirrorHelper {

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

            return ExtensionModel.of(extensionClass).type(); // Check that the extension is valid
        }
    };

    /** A handle for invoking the package-private method {@link ExtensionMirror#initialize(PackedExtensionNavigator)}. */
    private static final MethodHandle MH_EXTENSION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionMirror.class,
            "initialize", void.class, PackedExtensionNavigator.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newExtensionMirror", ExtensionMirror.class);

    /** No help for you. */
    private ExtensionMirrorHelper() {}

    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ExtensionMirror<?> newMirror(ExtensionSetup extension, Class<? extends ExtensionMirror<?>> expectedMirrorClass) {
        ExtensionMirror<?> mirror = null;
        try {
            mirror = (ExtensionMirror<?>) MH_EXTENSION_NEW_EXTENSION_MIRROR.invokeExact(extension.instance());
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

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

        // Initializes the mirror
        try {
            MH_EXTENSION_MIRROR_INITIALIZE.invokeExact(mirror, new PackedExtensionNavigator(extension, extension.extensionType));
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

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
