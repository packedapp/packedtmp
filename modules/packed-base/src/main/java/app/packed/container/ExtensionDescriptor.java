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

import app.packed.api.Contract;
import packed.internal.container.extension.ExtensionModel;

/**
 * An extension descriptor.
 * <p>
 * This class describes an extension and defines various methods to obtain information about the extension. An instance
 * of this class is normally acquired by calling {@link #of(Class)}.
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
    public Set<Class<? extends Contract>> contracts() {
        return model.contracts.keySet();
    }

    /**
     * Returns an immutable set of any other extensions this extension depends on. The returned set does not include
     * transitive dependencies.
     * 
     * @return any other extensions this extension depends on
     */
    public Set<Class<? extends Extension>> dependencies() {
        return model.dependenciesDirect;
    }

    /**
     * Returns the module that the extension belongs to.
     * 
     * @return the module that the extension belongs to
     * @see Class#getModule()
     */
    public Module module() {
        return type().getModule();
    }

    /**
     * Returns the type of extension this descriptor describes.
     * 
     * @return the type of extension this descriptor describes
     */
    public Class<? extends Extension> type() {
        return model.extensionType;
    }

    // public Hook
    // Map<Class<? extends Hook>, List<Object>>

    /**
     * Returns a descriptor for the specified extension type.
     * 
     * @param extensionType
     *            the extension type to return a descriptor for
     * @return a descriptor for the specified extension type
     */
    public static ExtensionDescriptor of(Class<? extends Extension> extensionType) {
        // Maybe just create one descriptor for each model and keep it
        requireNonNull(extensionType, "extensionType is null");
        return new ExtensionDescriptor(ExtensionModel.of(extensionType));
    }
}
// A method for transitive dependencies...

// DirectedVertexGraph<T>
// Or Just DependencyGraph<T>
// DependencyGraph<Module>
// DependencyGraph<ExtensionDescriptor>
// DependencyGraph<ServiceDescriptor>...
//// What about export... Change stuff

//// Her vil vi maaske gerne have
// Hook Annotations
//// Field | Method | Activating (Although you can see that on the Annotation)
//// Other Extension
//// Sidecars
