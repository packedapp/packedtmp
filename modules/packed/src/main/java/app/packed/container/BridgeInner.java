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
package app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.base.Key;
import app.packed.container.bridge.PackedBridge;
import app.packed.operation.Op;

/**
 *
 */
public class BridgeInner<E extends Extension<E>> {

    private PackedBridge<E> bridge;

    BridgeInner(PackedBridge<E> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    public BridgeOuter build() {
        return new BridgeOuter(bridge);
    }

    // A runtime argument passed along to ???
    public BridgeInner<E> addInvocationArgument(Class<?> key) {
        throw new UnsupportedOperationException();
    }

    public BridgeInner<E> addInvocationArgument(Key<?> key) {
        throw new UnsupportedOperationException();
    }

    public BridgeInner<E> onNeverUsed(Runnable action) {
        throw new UnsupportedOperationException();
    }

    public BridgeInner<E> onUse(Consumer<E> action) {
        bridge = bridge.onUse(action);
        return this;
    }

    public BridgeInner<E> provideUp(Op<?> op) {
        throw new UnsupportedOperationException();
    }

    public <S> BridgeInner<E> provideOutOr(Op<?> op, Supplier<S> supplier) {
        throw new UnsupportedOperationException();
    }

    public <S> BridgeInner<E> provideOutOrInstance(Op<S> op, S constant) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> BridgeInner<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        throw new UnsupportedOperationException();
    }
}
