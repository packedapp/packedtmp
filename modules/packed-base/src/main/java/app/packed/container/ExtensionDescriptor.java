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

import static java.util.Objects.requireNonNull;

import java.util.Set;

import app.packed.base.Contract;
import packed.internal.container.ExtensionModel;
import packed.internal.container.PackedExtensionDescriptor;

/**
 * An extension descriptor.
 * <p>
 * This class describes an extension and defines various methods to obtain information about the extension. An instance
 * of this class is normally acquired by calling {@link #of(Class)}.
 */
public interface ExtensionDescriptor {

    /**
     * Returns all the different types of contracts the extension exposes.
     * 
     * @return all the different types of contracts the extension exposes
     */
    Set<Class<? extends Contract>> contracts();

    /**
     * Returns an immutable set of any other extensions this extension depends on. The returned set does not include
     * transitive dependencies.
     * <p>
     * The returned set includes optional dependencies specified via ... that could be resolved successfully.
     * 
     * @return any other extensions this extension depends on
     */
    Set<Class<? extends Extension>> dependencies();

    /**
     * Returns the module that the extension belongs to.
     * 
     * @return the module that the extension belongs to
     * @see Class#getModule()
     */
    Module module();

    /**
     * Returns the type of extension this descriptor describes.
     * 
     * @return the type of extension this descriptor describes
     */
    Class<? extends Extension> type();

    /**
     * Returns a descriptor for the specified extension type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return a descriptor for the specified extension type
     */
    static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        return new PackedExtensionDescriptor(ExtensionModel.of(extensionType)); // we could cache this
    }
}