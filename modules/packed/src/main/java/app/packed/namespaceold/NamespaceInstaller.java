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
package app.packed.namespaceold;

import internal.app.packed.oldnamespace.PackedNamespaceInstaller;

/**
 * A installer for a namespace.
 */
public sealed interface NamespaceInstaller<H extends OldNamespaceHandle<?, ?>> permits PackedNamespaceInstaller {

    /**
     * Installs the namespace
     *
     * @return a handle for the new namespace as configured in the underlying {@link NamespaceTemplate} used
     */
    H install();
}