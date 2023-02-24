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
package internal.app.packed.lifetime;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.extension.Extension;
import app.packed.extension.container.ExtensionLink;
import app.packed.util.Key;
import internal.app.packed.container.PackedContainerBuilder;

/**
 * Represent a communication channel between a parent container lifetime and a child container lifetime. This class is
 * exposed as {@link ContainerLifetimeChannel}.
 */
public record PackedContainerLifetimeChannel(Class<? extends Extension<?>> extensionClass, Consumer<? super PackedContainerBuilder> onUse,
        Set<Key<?>> keys) implements ExtensionLink {

    // is used in the (unlikely) scenario with multiple links
    // that each provide something with the same key
    @Override
    public ExtensionLink rekey(Key<?> from, Key<?> to) {
        // from key must exist
        // Advanced operation
        // no case checks are performed
        // or maybe we do anywhere, should probably be simple
        throw new UnsupportedOperationException();
    }
    /**
     * @param type
     * @return
     */
    public PackedContainerLifetimeChannel addInvocationArgument(Class<?> type) {
        return null;
    }

    public void use(PackedContainerBuilder builder) {
        if (onUse != null) {
            onUse.accept(builder);
        }
    }

    public List<Class<?>> invocationArguments() {
        return List.of();
    }

}
//
//// ExtensionBean -> T
//public static class Extractor {
//    MethodHandle extractor;
//
//    public PackedContainerLifetimeChannel<E> provide(Class<?> extensionBean, Op<?> op) {
//        // Adds synthetic operation to extensionBean
//        return null;
//    }
//
//    public <K> PackedContainerLifetimeChannel<E> provideGeneratedConstant(Class<K> key, Function<? super E, ? extends K> provider) {
//        return provideGeneratedConstant(Key.of(key), provider);
//    }
//
//    public <K> PackedContainerLifetimeChannel<E> provideGeneratedConstant(Key<K> key, Function<? super E, ? extends K> provider) {
//        // Must only be created once. And used everywhere on the guest
//        return null;
//    }
//    Key<?> key;
//
//    // Must be resolved in lifetime container...
//    Set<Key<?>> requirements; // must only make use of services... Or maybe just resolve it as OperationType
//    // Hvor bliver det her en synthetic metode???
//    // Paa beanen? Ja det maa det jo vaere...
//    // Hvis vi har flere dependencies... kan det jo ikke vaere paa extension beanen...
//    //
//}
