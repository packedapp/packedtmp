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
package app.packed.component.guest.old;

import static java.util.Objects.requireNonNull;

import app.packed.binding.Key;
import app.packed.extension.Extension;

/**
 *
 */
public abstract class AltContainerGuestLink<E extends Extension<E>> {

    private final Class<? extends Extension<?>> extensionType;

    protected AltContainerGuestLink(Class<? extends Extension<E>> extensionType, String name) {
        this.extensionType = requireNonNull(extensionType);
        if (extensionType.getModule() != getClass().getModule()) {
            throw new IllegalArgumentException();
        }

        // Scoped local, or maybe have a Configurator object
        define();
    }

    protected abstract void define();

    /**
     * Specifies an action that will be invoked whenever the extension is first used in the targeted container.
     *
     * @param action
     *            the specified action will be invoked whenever the extension is first used.
     * @return this configurator
     */
    protected void onExtensionUsed(E extension) {}

    // The link was specified but the extension was never used
    protected void onExtensionUnused() {}

    public Class<? extends Extension<?>> extensionType() {
        return extensionType;
    }

 // checks module
    // Alternativt, er det bare en wirelet... Naah, vi vil gerne have at ContainerTemplaten bestemmer
    // Og
    interface Configurator<E> {

        <T> Configurator<E> bindConstant(Object constant);

        // extensionClass#MAIN
        <T> Configurator<E> bindNamespaceService();

        <T> Configurator<E> bindNamespaceService(Key<?> as);

        <T> Configurator<E> bindNamespaceServiceFallback(Object alternative);

        <T> Configurator<E> provideConstant(Class<T> key, T constant);

        /**
         * @param <T>
         * @param key
         * @param constant
         * @return
         *
         * @see ContainerTemplate#carrierProvideConstant(Key, Object)
         * @see ContainerCarrierBeanConfiguration#carrierProvideConstant(Key, Object)
         * @see ContainerBuilder#carrierProvideConstant(Key, Object)
         */
        <T> Configurator<E> provideConstant(Key<T> key, T constant);
    }
}
