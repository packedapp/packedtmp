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
package internal.app.packed.lifetime.zbridge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bindings.Key;
import app.packed.extension.Extension;
import app.packed.operation.Op;

/**
 *
 */
// Muligheder

// Vi kan altid require en annotation... @ProvideContainerGuest
// Vi kan registrere nogle ops... som skal knyttes paa en bean..

public final class PackedBridge<E extends Extension<E>> {

    public final Class<? extends Extension<E>> extensionClass;

    PackedBridge(Class<? extends Extension<E>> extensionClass) {
        this.extensionClass = extensionClass;
    }

    /**
     * @param type
     * @return
     */
    public PackedBridge<E> addInvocationArgument(Class<?> type) {
        return null;
    }

    public List<Class<?>> invocationArguments() {
        return List.of();
    }

    public Set<Key<?>> keys() {
        return Set.of();
    }

    /**
     * @param action
     * @return
     */
    public PackedBridge<E> onUse(Consumer<? super E> action) {
        return null;
    }

    public PackedBridge<E> provide(Class<?> extensionBean, Op<?> op) {
        // Adds synthetic operation to extensionBean
        return null;
    }


    public <K> PackedBridge<E> provideGeneratedConstant(Class<K> key, Function<? super E, ? extends K> provider) {
        return provideGeneratedConstant(Key.of(key), provider);
    }

    public <K> PackedBridge<E> provideGeneratedConstant(Key<K> key, Function<? super E, ? extends K> provider) {
        // Must only be created once. And used everywhere on the guest
        return null;
    }

    public static <E extends Extension<E>> PackedBridge<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        return new PackedBridge<>(extensionType);
    }

    // ExtensionBean -> T
    public static class Extractor {
        MethodHandle extractor;

        Key<?> key;

        // Must be resolved in lifetime container...
        Set<Key<?>> requirements; // must only make use of services... Or maybe just resolve it as OperationType
        // Hvor bliver det her en synthetic metode???
        // Paa beanen? Ja det maa det jo vaere...
        // Hvis vi har flere dependencies... kan det jo ikke vaere paa extension beanen...
        //
    }

}
