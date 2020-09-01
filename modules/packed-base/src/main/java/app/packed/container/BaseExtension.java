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

import packed.internal.container.PackedContainerAssembly;

/**
 * An extension that is always available from any container. Even while the Every other extension implicitly has this
 * extension as a mandatory dependency.
 * 
 * <p>
 * 
 * <p>
 * Other extensions should never depend on this extension via {@link ExtensionSetup#dependencies()}. Doing so will
 * fail with a runtime exception.
 */
// If it proves strange that it is not in the component tree.
// We can always say Container is a member of BaseExtensions... Nahh
public final class BaseExtension extends Extension {

    /** The container configuration. This extension is the only extension that can use it. */
    private final PackedContainerAssembly container;

    /**
     * Creates a new base extension.
     * 
     * @param container
     *            the configuration of the container.
     */
    /* package-private */ BaseExtension(PackedContainerAssembly container) {
        this.container = requireNonNull(container, "container is null");
    }

    /**
     * Returns all the extensions that are currently in use. This (BaseExtension) extension is never included.
     * 
     * @return all the extensions that are currently in use
     */
    // ExtensionSet is never a view???
    public ExtensionSet extensions() {
        return ExtensionSet.of(container.extensions()); // view/not-view?
    }

    /**
     * Returns whether or not the specified extension type is in use
     * 
     * @param extensionType
     *            the extension type to test
     * @return whether or not the specified extension is in use
     * @throws IllegalArgumentException
     *             if the specified extension type is {@link Extension}
     */
    public boolean isUsed(Class<? extends Extension> extensionType) {
        requireNonNull(extensionType, "extensionType is null");
        if (extensionType == Extension.class) {
            throw new IllegalArgumentException("Extension is never used");
        }
        return container.extensions.keySet().contains(extensionType);
    }
}

// Hmm vi har ikke
// public static final BaseExtension defaultOrder = new BaseExtension();
// onExtensionAdded()// printStackTrace()
// introduce order between(e1, e2)
// (only works if there is not a dependency between them
