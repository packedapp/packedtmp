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
package app.packed.extension;

import java.util.Optional;
import java.util.function.Function;

import app.packed.component.ComponentPath;
import app.packed.namespace.NamespaceHandle;
import app.packed.namespace.NamespaceTemplate;
import app.packed.util.TreeView;
import internal.app.packed.extension.PackedExtensionHandle;

/**
 * A handle for an {@link Extension}, that can be passed along to code outside of the extension itself.
 */
public sealed interface ExtensionHandle<E extends Extension<E>> permits PackedExtensionHandle {

    TreeView.Node<E> applicationNode();

    /**
     * Checks that the extension is configurable, throwing {@link IllegalStateException} if it is not.
     *
     * @throws IllegalStateException
     *             if the extension is no longer configurable.
     */
    void checkIsConfigurable();

    /** {@return the path of the container that this extension belongs to.} */
    ComponentPath containerPath();

    boolean isExtensionUsed(Class<? extends Extension<?>> extensionType);

    <T extends NamespaceHandle<E, ?>> T namespaceLazy(NamespaceTemplate template, String name, Function<NamespaceTemplate.Installer, T> factory);

    /** {@return any parent extension this extension might have in the same application} */
    Optional<E> parent();

    <P extends ExtensionPoint<?>> P use(Class<P> extensionPointClass);
}
