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

import packed.internal.container.PackedContainerConfiguration;

/**
 * An extension that is always automatically added whenever a new container is configured. Even while the
 * 
 * <p>
 * It is an error to depend on this via {@link ExtensionSidecar#dependencies()}. Not sure you can with subtensions and
 * stuff.
 */
public final class BaseExtension extends Extension {

    /** The container configuration. This extension is the only extension that can use it. */
    private final PackedContainerConfiguration pcc;

    /**
     * Creates a new extension.
     * 
     * @param pcc
     *            the configuration of the container in which the extension is used.
     */
    /* package-private */ BaseExtension(PackedContainerConfiguration pcc) {
        this.pcc = requireNonNull(pcc, "pcc is null");
    }

    /**
     * Returns all the extensions that are currently in use. This (BaseExtension) extension is never included.
     * 
     * @return all the extensions that are currently in use
     */
    public ExtensionOrdering extensions() {
        return ExtensionOrdering.of(pcc.extensions()); // view/not-view?
    }
}
// Hmm vi har ikke
// public static final BaseExtension defaultOrder = new BaseExtension();

// onExtensionAdded()// printStackTrace()

// introduce order between(e1, e2)
// (only works if there is not a dependency between them
