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

import java.util.List;
import java.util.Set;

import app.packed.framework.Key;
import internal.app.packed.application.PackedBridge;

/**
 *
 */
public final class BridgeOuter {

    /** The actual lifetime bridge. */
    final PackedBridge<?> bridge;

    BridgeOuter(PackedBridge<?> bridge) {
        this.bridge = requireNonNull(bridge);
    }
    
    public List<Class<?>> invocationArguments() {
        return bridge.invocationArguments();
    }
    
    public Set<Key<?>> keys() {
        return bridge.keys();
    }
}
