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
package app.packed.container.extension;

import static java.util.Objects.requireNonNull;

import packed.internal.container.extension.ExtensionModel;

/**
 * Ideen er egentlig at man kan tage en extension klasse som parameter.. og finde ud af dens properties....
 */

/// Maaske man altid skal define hookOnAnnotatedFields... og saa lade @ActivateExtension vaere som den er...
public final class ExtensionDescriptor {

    /** The extension model for this descriptor. */
    private final ExtensionModel<?> model;

    /** Never instantiate. */
    private ExtensionDescriptor(ExtensionModel<?> model) {
        this.model = requireNonNull(model);
    }

    // Hook Annotations
    //// Field | Method | Activating (Although you can see that on the Annotation)

    //// Other Extension

    //// Sidecars

    /**
     * Returns the extension type this descriptor describes.
     * 
     * @return the extension type this descriptor describes
     */
    public Class<? extends Extension> extensionType() {
        return model.extensionType;
    }

    /**
     * Returns a new extension descriptor for the specified type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return a new extension descriptor for the specified type
     */
    public static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new ExtensionDescriptor(ExtensionModel.of(extensionType));
    }
}
