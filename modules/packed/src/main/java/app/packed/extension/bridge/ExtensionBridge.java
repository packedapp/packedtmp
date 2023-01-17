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

import app.packed.binding.Key;
import app.packed.extension.Extension;
import internal.app.packed.lifetime.PackedBridge;

/**
 *
 */
public final class ExtensionBridge {

    /** The actual lifetime bridge. */
    final PackedBridge<?> bridge;

    ExtensionBridge(PackedBridge<?> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    /** {@return the extension that declares the bridge.} */
    public Class<? extends Extension<?>> extensionClass() {
        return bridge.extensionClass;
    }

    public List<Class<?>> invocationArguments() {
        return bridge.invocationArguments();
    }

    public Set<Key<?>> keys() {
        return bridge.keys();
    }

    public static <E extends Extension<E>> ExtensionBridge.Builder<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        throw new UnsupportedOperationException();
    }

    public static final class Builder<E extends Extension<E>> {

        PackedBridge<E> bridge;

        Builder(PackedBridge<E> bridge) {
            this.bridge = requireNonNull(bridge);
        }

        ExtensionBridge build() {
            return new ExtensionBridge(bridge);
        }

        public Builder<E> addInvocationArgument(Class<?> key) {
            throw new UnsupportedOperationException();
        }

        public Builder<E> onUse(Consumer<? super E> action) {
            bridge = bridge.onUse(action);
            return this;
        }
    }
}
