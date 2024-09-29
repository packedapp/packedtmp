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
package app.packed.component.guest;

import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.extension.Extension;

/**
 *
 */
// Ways to bind
// Constant
// CodeGen Supplier
// Lookup in extension service namespace

public interface ContainerLinker {

    // Void/void.class will be ignored, but you can use onExtensionUsed. But not any of the bind methods
    static <E extends Extension<E>> ContainerLinker of(Class<E> extensionType, Class<?> key, Consumer<? super Configurator<E>> configurator) {
        throw new UnsupportedOperationException();
    }

    static <E extends Extension<E>> ContainerLinker of(Class<E> extensionType, Key<?> key, Consumer<? super Configurator<E>> configurator) {
        throw new UnsupportedOperationException();
    }

    // checks module
    interface Configurator<E> {

        /**
         * Specifies an action that will be invoked whenever the extension is first used in the targeted container.
         *
         * @param action
         *            the specified action will be invoked whenever the extension is first used.
         * @return this configurator
         */
        Configurator<E> onExtensionUsed(Consumer<? super E> action);

        // The extension was never used in the target
        Configurator<E> onExtensionUnused(Runnable action);

        Configurator<E> named(String string);

        // Hvis extensionen ikke er installeret (or exported)

        <T> Configurator<E> bindConstant(Object constant);

        // extensionClass#MAIN
        <T> Configurator<E> bindNamespaceService();

        <T> Configurator<E> bindNamespaceService(Key<?> as);

        <T> Configurator<E> bindNamespaceServiceFallback(Object alternative);
    }
}
