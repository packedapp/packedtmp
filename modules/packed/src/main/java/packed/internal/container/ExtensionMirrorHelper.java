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
package packed.internal.container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.base.Nullable;
import app.packed.container.Extension;
import app.packed.container.ExtensionMirror;
import app.packed.container.InternalExtensionException;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;
import packed.internal.util.typevariable.TypeVariableExtractor;

/** A helper class for working with ExtensionMirror instances. */
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
                    Extension.class, s -> new InternalExtensionException("The type variable on " + type + " must be a proper subclass of Extension.class"));

            // Check that the mirror is in the same module as the extension itself
            if (extensionClass.getModule() != type.getModule()) {
                throw new InternalExtensionException("The extension mirror " + type + " must be a part of the same module (" + extensionClass.getModule()
                        + ") as " + extensionClass + ", but was part of '" + type.getModule() + "'");
            }

            return ExtensionModel.of(extensionClass).type(); // Check that the extension is valid
        }
    };

    /** A handle for invoking the package-private method {@link ExtensionMirror#initialize(PackedExtensionTree)}. */
    private static final MethodHandle MH_EXTENSION_MIRROR_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionMirror.class,
            "initialize", void.class, PackedExtensionTree.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionMirror()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_MIRROR = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newExtensionMirror", ExtensionMirror.class);

    /** No help for you. */
    private ExtensionMirrorHelper() {}

    @Nullable
    static ExtensionMirror<?> getExactMirrorOrNull(ContainerSetup container, Class<? extends ExtensionMirror<?>> mirrorClass) {
        // First find what extension the mirror belongs to by extracting <E> from ExtensionMirror<E extends Extension>
        Class<? extends Extension<?>> cl = EXTENSION_TYPES.get(mirrorClass);

        // See if the container uses the extension.
        ExtensionSetup extension = container.extensions.get(cl);

        return extension == null ? null : mirror(extension, mirrorClass);
    }

    /** {@return a mirror for the extension. An extension might specialize by overriding {@code Extension#mirror()}} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static ExtensionMirror<?> mirror(ExtensionSetup extension, Class<? extends ExtensionMirror<?>> expectedMirrorClass) {
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

        if (mirror.getClass() != ExtensionMirror.class) {
            if (expectedMirrorClass == null) {
                // Must return a mirror for the same extension
                Class<? extends Extension<?>> mirrorExtensionType = EXTENSION_TYPES.get(mirror.getClass());
                if (mirrorExtensionType != extension.extensionType) {
                    throw new InternalExtensionException("Extension " + extension.model.fullName()
                            + " returned a mirror for another extension, other extension type: " + mirrorExtensionType);
                }
            } else {
                // Fail if the type of mirror returned by the extension does not match the specified mirror type
                if (!expectedMirrorClass.isInstance(mirror)) {
                    throw new InternalExtensionException(
                            extension.extensionType.getSimpleName() + ".newExtensionMirror() was expected to return an instance of " + expectedMirrorClass
                                    + ", but returned an instance of " + mirror.getClass());
                }
            }
        }

        // Initializes the mirror
        try {
            MH_EXTENSION_MIRROR_INITIALIZE.invokeExact(mirror, new PackedExtensionTree(extension, extension.extensionType));
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
     * @return
     */
    static ExtensionMirror<?> newMirror(ExtensionSetup extension) {
        return mirror(extension, null);
    }
}
