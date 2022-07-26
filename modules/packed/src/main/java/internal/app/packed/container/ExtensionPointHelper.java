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

import static internal.app.packed.util.StringFormatter.format;
import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.container.InternalExtensionException;
import internal.app.packed.util.ClassUtil;
import internal.app.packed.util.LookupUtil;
import internal.app.packed.util.ThrowableUtil;
import internal.app.packed.util.typevariable.TypeVariableExtractor;

/** A helper class for creating new {@link ExtensionPoint} instances. */
public final class ExtensionPointHelper {

    /** A ExtensionPoint class to Extension class mapping. */
    private final static ClassValue<Class<? extends Extension<?>>> EXTENSION_TYPES = new ClassValue<>() {

        /** A type variable extractor. */
        private static final TypeVariableExtractor TYPE_LITERAL_EP_EXTRACTOR = TypeVariableExtractor.of(ExtensionPoint.class);

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        protected Class<? extends Extension<?>> computeValue(Class<?> type) {
            ClassUtil.checkProperSubclass(ExtensionPoint.class, type, InternalExtensionException::new);
            
            // Extract the extension class from ExtensionPoint<E>
            Class<? extends Extension<?>> extensionClass = (Class<? extends Extension<?>>) TYPE_LITERAL_EP_EXTRACTOR.extractProperSubClassOf(type,
                    Extension.class, InternalExtensionException::new);

            // Check that the extension point is in the same module as the extension itself
            if (extensionClass.getModule() != type.getModule()) {
                throw new InternalExtensionException("The extension point " + type + " must be a part of the same module (" + extensionClass.getModule()
                        + ") as " + extensionClass + ", but was part of '" + type.getModule() + "'");
            }

            return ExtensionModel.of(extensionClass).type(); // Will check that the extension is valid
        }
    };

    /** A handle for invoking the package-private method {@link ExtensionPoint#initialize(PackedExtensionPointContext)}. */
    private static final MethodHandle MH_EXTENSION_POINT_INITIALIZE = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ExtensionPoint.class,
            "initialize", void.class, PackedExtensionPointContext.class);

    /** A handle for invoking the protected method {@link Extension#newExtensionPoint()}. */
    private static final MethodHandle MH_EXTENSION_NEW_EXTENSION_POINT = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), Extension.class,
            "newExtensionPoint", ExtensionPoint.class);

    /** No help for you. */
    private ExtensionPointHelper() {}

    /** {@return an extension point of the specified type} */
    public static ExtensionPoint<?> newExtensionPoint(ExtensionSetup requestingExtension, Class<?> extensionPointType) {
        requireNonNull(extensionPointType, "extensionPointType is null");
        Class<? extends Extension<?>> extensionClass = EXTENSION_TYPES.get(extensionPointType); // checks proper subclass
        Class<? extends Extension<?>> requestingExtensionClass = requestingExtension.extensionType;

        // Check that the extension of requested extension point's is a direct dependency of the requesting extension
        if (!requestingExtension.model.dependencies().contains(extensionClass)) {
            // Special message if you try to use your own extension point
            if (extensionClass == requestingExtensionClass) {
                throw new InternalExtensionException(extensionClass.getSimpleName() + " cannot use its own extension point " + extensionPointType);
            }
            throw new InternalExtensionException(requestingExtensionClass.getSimpleName() + " must declare " + format(extensionClass)
                    + " as a dependency in order to use " + extensionPointType);
        }

        ExtensionSetup extension = requestingExtension.container.useExtensionSetup(extensionClass, requestingExtension);

        // Create a new extension point
        ExtensionPoint<?> instance = null;
        try {
            instance = (ExtensionPoint<?>) MH_EXTENSION_NEW_EXTENSION_POINT.invokeExact(extension.instance());
        } catch (Throwable t) {
            throw ThrowableUtil.orUndeclared(t);
        }

        // Cannot return a null extension point
        if (instance == null) {
            throw new NullPointerException(
                    "Extension " + extension.model.fullName() + " returned null from " + extension.model.name() + ".newExtensionPoint()");
        }

        if (!extensionPointType.isInstance(instance)) {
            throw new InternalExtensionException(extension.extensionType.getSimpleName() + ".newExtensionPoint() was expected to return an instance of "
                    + extensionPointType + ", but returned an instance of " + instance.getClass());
        }

        // Initializes the extension point
        PackedExtensionPointContext context = new PackedExtensionPointContext(extension, requestingExtension);
        try {
            MH_EXTENSION_POINT_INITIALIZE.invokeExact(instance, context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }

        return instance;
    }
}
