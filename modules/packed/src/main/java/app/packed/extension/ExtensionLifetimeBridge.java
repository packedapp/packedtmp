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

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.bindings.Key;
import app.packed.context.ContextTemplate;
import app.packed.lifetime.BeanLifetimeTemplate;
import app.packed.lifetime.sandbox.ManagedLifetimeController;
import app.packed.service.ServiceLocator;
import app.packed.service.sandbox.transform.ServiceExportsTransformer;
import internal.app.packed.bean.PackedBeanHandle;
import internal.app.packed.lifetime.zbridge.PackedBridge;
import internal.app.packed.service.PackedServiceLocator;

/**
 *
 */
// Features
//// Kan expose services that can be used together ContainerGuest
//// Kan

public final class ExtensionLifetimeBridge {

    /** A bridge that makes the name of the container available. */
    public static final ExtensionLifetimeBridge CONTAINER_NAME = null;

    /**
     * A bridge that a container's exported services available as a {@link app.packed.service.ServiceLocator} in the guest.
     */
    public static final ExtensionLifetimeBridge EXPORTED_SERVICE_LOCATOR = baseBuilder().onUse(e -> {
        e.ownBeanInstaller(BeanLifetimeTemplate.CONTAINER).installIfAbsent(PackedServiceLocator.class, h -> {
            h.exportAs(Key.of(ServiceLocator.class));
            e.addCodeGenerated(((PackedBeanHandle<?>) h).bean, new Key<Map<Key<?>, MethodHandle>>() {}, () -> e.extension.container.sm.exportedServices());
        });
    }).keys(ServiceLocator.class).build();

    // Teanker vi altid exportere den
    public static final ExtensionLifetimeBridge MANAGED_LIFETIME_CONTROLLER = baseBuilder().onUse(e -> {
        // check that we have a lifetime
    }).keys(ManagedLifetimeController.class).build();

    /** The internal lifetime bridge. */
    private final PackedBridge<?> bridge;

    private ExtensionLifetimeBridge(PackedBridge<?> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    /** {@return the extension that declares the bridge.} */
    public Class<? extends Extension<?>> extensionClass() {
        return bridge.extensionClass;
    }

    // Context injection???
    public List<Class<?>> invocationArguments() {
        return bridge.invocationArguments();
    }

    /**
     * Guests must use the {@link ContainerGuest} annotation
     *
     * @return any services that are made available
     *
     * @see app.packed.container.ContainerGuest
     */
    public Set<Key<?>> keys() {
        return bridge.keys();
    }

    // Hmmmmmm, fraekt
    public ExtensionLifetimeBridge transformServices(@SuppressWarnings("exports") Consumer<ServiceExportsTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    private static ExtensionLifetimeBridge.Builder<BaseExtension> baseBuilder() {
        return ExtensionLifetimeBridge.builder(MethodHandles.lookup(), BaseExtension.class);
    }

    public static <E extends Extension<E>> ExtensionLifetimeBridge.Builder<E> builder(MethodHandles.Lookup caller, Class<E> extensionType) {
        return new Builder<>(PackedBridge.builder(caller, extensionType));
    }

    public static final class Builder<E extends Extension<E>> {

        /** The internal bridge */
        private PackedBridge<E> bridge;

        Builder(PackedBridge<E> bridge) {
            this.bridge = requireNonNull(bridge);
        }

        /**
         * Builds and returns the new bridge.
         *
         * @return the new bridge
         */
        public ExtensionLifetimeBridge build() {
            return new ExtensionLifetimeBridge(bridge);
        }
        // If arguments are needed from the lifetime start, must use a context.
        public Builder<E> inContext(ContextTemplate template) {
            return this;
        }

        public Builder<E> keys(Class<?>... keys) {
            return keys(Key.ofAll(keys));
        }

        // Must be exported by the extension
        public Builder<E> keys(Key<?>... keys) {
            Set.of(keys);
            return this;
        }

        // The specified action is run immediately before onNew?
        public Builder<E> onUse(Consumer<? super E> action) {
            bridge = bridge.onUse(action);
            return this;
        }
    }
}

// Ideen er at installere beans der kan be exposed to guest objektet.
// Fx ServiceLocator
// Hvorfor er det ikke bare extensions der installere den og ikke bridgen

// (e-> installGuest(F.class).dasd);

// OperationTemplate???

// 123 paa runtime som argument.
// Hvordan faar jeg det ind i en bean
// Anden end via ContextInjection???

// InvocationContextArgument

// Create an internalContext???

// Bliver noedt til at vaere unik. Kan ikke add

//
//public Builder<E> provide(Class<?> extensionBean, Op<?> op) {
//  // Adds synthetic operation to extensionBean
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Class<T> key) {
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Key<T> key) {
//  return this;
//}

//public <T> Builder<E> provide(Key<T> key, Class<T> type) {
//  bridge = bridge.addInvocationArgument(type);
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> type, Key<?> ) {
//  bridge = bridge.addInvocationArgument(type);
//  return this;
//}