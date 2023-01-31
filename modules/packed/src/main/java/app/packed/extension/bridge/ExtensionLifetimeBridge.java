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
package app.packed.extension.bridge;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.bindings.Key;
import app.packed.extension.Extension;
import app.packed.service.sandbox.ServiceTransformer;
import internal.app.packed.lifetime.zbridge.PackedBridge;

/**
 *
 */
// Features
//// Kan expose services that can be used together ContainerGuest
//// Kan

public final class ExtensionLifetimeBridge {

    /** A bridge that makes the name of the container available. */
    public static final ExtensionLifetimeBridge CONTAINER_NAME = null;

    /** A bridge that makes exported services available as {@link app.packed.service.ServiceLocator} in the guest. */
    public static final ExtensionLifetimeBridge EXPORTED_SERVICE_LOCATOR = null;

    public static final ExtensionLifetimeBridge MANAGED_LIFETIME_CONTROLLER = null;

    /** The actual lifetime bridge. */
    private final PackedBridge<?> bridge;

    private ExtensionLifetimeBridge(PackedBridge<?> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    /** {@return the extension that declares the bridge.} */
    public Class<? extends Extension<?>> extensionClass() {
        return bridge.extensionClass;
    }

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
    public ExtensionLifetimeBridge transformServices(@SuppressWarnings("exports") Consumer<ServiceTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> ExtensionLifetimeBridge.Builder<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        return new Builder<>(PackedBridge.builder(lookup, extensionType));
    }

    public static final class Builder<E extends Extension<E>> {

        /** The internal bridge */
        private PackedBridge<E> bridge;

        Builder(PackedBridge<E> bridge) {
            this.bridge = requireNonNull(bridge);
        }

        public Builder<E> addInvocationArgument(Class<?> type) {
            bridge = bridge.addInvocationArgument(type);
            return this;
        }

        /**
         * Builds and returns the new bridge.
         *
         * @return the new bridge
         */
        public ExtensionLifetimeBridge build() {
            return new ExtensionLifetimeBridge(bridge);
        }

        // The specified action is run immediately before onNew?
        public Builder<E> onUse(Consumer<? super E> action) {
            bridge = bridge.onUse(action);
            return this;
        }
    }
}
