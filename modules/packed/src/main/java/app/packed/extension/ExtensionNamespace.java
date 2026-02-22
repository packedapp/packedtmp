/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.component.ComponentRealm;

/**
 *
 */
public abstract class ExtensionNamespace<N extends ExtensionNamespace<N, E>, E extends Extension<E>> {

    private final ExtensionNamespaceHandle<N, E> handle;

    protected ExtensionNamespace(ExtensionNamespaceHandle<N, E> handle) {
        this.handle = requireNonNull(handle);
    }

    public final boolean isExtensionNamespace() {
        return owner().isExtension();
    }

    // ExtensionNamespace created for extensions will never use this method
    public abstract E newExtension(ExtensionHandle<E> extensionHandle);

    public final ComponentRealm owner() {
        return handle.owner();
    }

    public final Optional<N> parent() {
        return Optional.empty();
    }
}
