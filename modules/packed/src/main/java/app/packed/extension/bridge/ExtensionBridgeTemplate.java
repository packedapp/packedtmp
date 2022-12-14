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
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.service.Key;
import internal.app.packed.application.PackedBridge;

/**
 *
 */
// Det er jo egentlig lidt en context i context'en...
public final class ExtensionBridgeTemplate<E extends Extension<E>> {

    private PackedBridge<E> bridge;

    ExtensionBridgeTemplate(PackedBridge<E> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    public ExtensionBridge build() {
        return new ExtensionBridge(bridge);
    }

    // A runtime argument passed along to ???
    public ExtensionBridgeTemplate<E> addInvocationArgument(Class<?> key) {
        throw new UnsupportedOperationException();
    }

    public ExtensionBridgeTemplate<E> addInvocationArgument(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public ExtensionBridgeTemplate<E> onNeverUsed(Runnable action) {
        throw new UnsupportedOperationException();
    }

    public ExtensionBridgeTemplate<E> onUse(Consumer<E> action) {
        bridge = bridge.onUse(action);
        return this;
    }

    public ExtensionBridgeTemplate<E> provideUp(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    public <S> ExtensionBridgeTemplate<E> provideOutOr(Op<?> op, Supplier<S> supplier) {
        throw new UnsupportedOperationException();
    }

    public <S> ExtensionBridgeTemplate<E> provideOutOrInstance(Op<S> op, S constant) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> ExtensionBridgeTemplate<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        throw new UnsupportedOperationException();
    }
}
