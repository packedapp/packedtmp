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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.bindings.Key;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionLifetimeBridge;
import app.packed.operation.Op;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionPreLoad;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.util.LookupUtil;

/**
 *
 */
// Muligheder

// Vi kan altid require en annotation... @ProvideContainerGuest
// Vi kan registrere nogle ops... som skal knyttes paa en bean..

public final class PackedBridge<E extends Extension<E>> {

    /** A handle that can access BeanConfiguration#handle. */
    private static final VarHandle VH_BRIGE = LookupUtil.findVarHandle(MethodHandles.lookup(), ExtensionLifetimeBridge.class, "bridge", PackedBridge.class);

    public final Class<? extends Extension<E>> extensionClass;

    public final Set<Key<?>> keys;

    public final Consumer<? super ExtensionSetup> onUse;

    PackedBridge(Class<? extends Extension<E>> extensionClass, Consumer<? super ExtensionSetup> onUse, Set<Key<?>> keys) {
        this.extensionClass = extensionClass;
        this.onUse = onUse;
        this.keys = Set.copyOf(keys);
    }

    /**
     * @param type
     * @return
     */
    public PackedBridge<E> addInvocationArgument(Class<?> type) {
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

    public Set<Key<?>> keys() {
        return Set.of();
    }

    public PackedBridge<E> addKeys(Set<Key<?>> keys) {
        HashSet<Key<?>> s = new HashSet<>(keys);
        s.addAll(keys);
        return new PackedBridge<>(extensionClass, onUse, s);

    }

    /**
     * @param action
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PackedBridge<E> onUse(Consumer<? super E> action) {
        requireNonNull(action, "action is null");
        Consumer<? super ExtensionSetup> c = e -> action.accept((E) e.instance());
        return new PackedBridge<>(extensionClass, onUse == null ? c : onUse.andThen((Consumer) c), keys);
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

    public static <E extends Extension<E>> PackedBridge<E> builder(MethodHandles.Lookup caller, Class<E> extensionType) {
        if (!caller.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("caller must have full privilege access");
        } else if (caller.lookupClass().getModule() != extensionType.getModule()) {
            throw new IllegalArgumentException("extension type must be in the same module as the caller");
        }
        return new PackedBridge<>(extensionType, null, Set.of());
    }

    public static PackedBridge<?> crack(ExtensionLifetimeBridge bridge) {
        return (PackedBridge<?>) VH_BRIGE.get(bridge);
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
