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

import java.util.Set;

import app.packed.contract.Contract;
import packed.internal.container.extension.ExtensionModel;

/**
 * An extension descriptor.
 * <p>
 * A extension descriptor describes an extension and defines various methods to obtain information about the extension.
 * An instance is normally acquired by calling {@link #of(Class)}.
 */
public final class ExtensionDescriptor {

    /** The extension model we wrap. */
    private final ExtensionModel<?> model;

    /** Never instantiate. */
    private ExtensionDescriptor(ExtensionModel<?> model) {
        this.model = requireNonNull(model);
    }

    /**
     * Returns all the different types of contract types the extension provides.
     * 
     * @return all the different types of contract types the extension provides
     */
    public Set<Class<? extends Contract>> contractTypes() {
        return model.contracts.keySet();
    }

    /**
     * Returns any other extensions this extension depends on.
     * 
     * @return any other extensions this extension depends on
     */
    public Set<Class<? extends Extension>> dependencies() {
        return Set.of();
    }

    /**
     * Returns the extension type this descriptor describes.
     * 
     * @return the extension type this descriptor describes
     */
    public Class<? extends Extension> type() {
        return model.extensionType;
    }

    /**
     * Returns an extension descriptor for the specified extension type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return an extension descriptor for the specified extension type
     */
    public static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        // Maybe just create one descriptor for each model and keep it
        requireNonNull(extensionType, "extensionType is null");
        return new ExtensionDescriptor(ExtensionModel.of(extensionType));
    }
}

// Hook Annotations
//// Field | Method | Activating (Although you can see that on the Annotation)

//// Other Extension

//// Sidecars
