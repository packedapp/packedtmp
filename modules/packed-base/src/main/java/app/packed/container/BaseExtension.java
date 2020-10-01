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

import java.util.Collections;
import java.util.Set;

import packed.internal.container.ContainerAssembly;
import packed.internal.container.ExtensionAssembly;

/**
 * An extension that automatically installed into any container.
 * <p>
 * Base extensions are never shown in a component tree.
 */
// Maaske er det simpelthen bare ExtensionManager?? IDK
// Kommer an paa om vi inkludere flere ting...
// Altsaa maaske skal vi bare finde paa et andet navn...
// Ideen er jo lidt at den er tilgaengelig fra alle componenter...
// Og ikke bare containeren.
public final class BaseExtension extends Extension {

    /** The container this extension is part of. */
    private final ContainerAssembly container;

    /**
     * Creates a new base extension.
     * 
     * @param configuration
     *            the configuration of this extension
     */
    /* package-private */ BaseExtension(ExtensionConfiguration configuration) {
        this.container = ((ExtensionAssembly) configuration).container();
    }

    /**
     * Returns a view of all the extensions that are currently in use. This (BaseExtension) extension is not included.
     * 
     * @return all the extensions that are currently in use
     */
    public Set<Class<? extends Extension>> extensions() {
        // TODO fix base extension is included...
        return Collections.unmodifiableSet(container.extensionView());
    }

    /**
     * Returns whether or not the specified extension type is in use.
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
            throw new IllegalArgumentException("Cannot specify Extension.class");
        } else if (extensionType == BaseExtension.class) {
            return true;
        }
        return container.extensions.keySet().contains(extensionType);
    }
}
// public static final BaseExtension defaultOrder = new BaseExtension();
// onExtensionAdded()// printStackTrace()
// introduce order between(e1, e2)
// (only works if there is not a dependency between them
