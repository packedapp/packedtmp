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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.extension.ContainerLifetimeChannel;
import app.packed.extension.Extension;
import app.packed.util.Key;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionPreLoad;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.LookupUtil;

/**
 * Represent a communication channel between a parent container lifetime and a child container lifetime. This class is
 * exposed as {@link ContainerLifetimeChannel}.
 */
public record PackedContainerLifetimeChannel<E extends Extension<E>>(Class<? extends Extension<E>> extensionClass, Consumer<? super ExtensionSetup> onUse,
        Set<Key<?>> exports) {

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BRIGE = LookupUtil.findVarHandle(MethodHandles.lookup(), ContainerLifetimeChannel.class, "channel",
            PackedContainerLifetimeChannel.class);

    /**
     * @param type
     * @return
     */
    public PackedContainerLifetimeChannel<E> addInvocationArgument(Class<?> type) {
        return null;
    }

    /**
     * @param containerSetup
     */
    public void install(ContainerSetup container) {
        if (onUse != null) {
            ExtensionPreLoad epl = container.preLoad.computeIfAbsent(extensionClass, k -> new ExtensionPreLoad());
            epl.add(this);
        }
    }

    public List<Class<?>> invocationArguments() {
        return List.of();
    }

    /**
     * Cracks a {@link ContainerLifetimeChannel} and returns its internal {@link PackedContainerLifetimeChannel}.
     *
     * @param channel
     *            the channel to crack
     * @return the cracked channel
     */
    public static PackedContainerLifetimeChannel<?> crack(ContainerLifetimeChannel channel) {
        return (PackedContainerLifetimeChannel<?>) VH_BRIGE.get(channel);
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
