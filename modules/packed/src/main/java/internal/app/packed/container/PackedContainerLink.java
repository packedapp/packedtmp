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
package internal.app.packed.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.binding.Key;
import app.packed.component.guest.OldContainerTemplateLink;
import app.packed.extension.Extension;

/**
 * Represent a communication channel between a parent container lifetime and a child container lifetime. This class is
 * exposed as {@link ContainerLifetimeChannel}.
 */
public record PackedContainerLink(Class<? extends Extension<?>> extension, Consumer<? super PackedContainerInstaller<?>> onUse,
        Map<Key<?>, PackedContainerLink.KeyFragment> services) implements OldContainerTemplateLink {

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        return services.keySet();
    }

    public PackedContainerLink withFragments(Key<?> key, PackedContainerLink.KeyFragment fragment) {
        Map<Key<?>, KeyFragment> s = services;
        if (s == null) {
            s = Map.of(key, fragment);
        } else {
            Map<Key<?>, PackedContainerLink.KeyFragment> newServices = new HashMap<>(s);
            newServices.put(key, fragment);
            s = Map.copyOf(newServices);
        }
        return new PackedContainerLink(extension(), onUse, s);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public PackedContainerLink withUse(Consumer<? super PackedContainerInstaller<?>> action) {
        return new PackedContainerLink(extension(), onUse == null ? action : onUse.andThen((Consumer) action), services);
    }

    public sealed interface KeyFragment {

        record OfConstant(Object constant) implements KeyFragment {}

        record OfExports() implements KeyFragment {}

        record OfExportsOrElse(Object alternative) implements KeyFragment {}
    }
}

///**
//* @param type
//* @return
//*/
//public PackedContainerTemplatePack addInvocationArgument(Class<?> type) {
// return null;
//}
//
//public List<Class<?>> invocationArguments() {
//    return List.of();
//}

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
