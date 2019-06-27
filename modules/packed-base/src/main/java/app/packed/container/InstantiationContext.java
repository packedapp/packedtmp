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

import java.util.IdentityHashMap;

import app.packed.util.Nullable;

/**
 * An instantiation context is created for every delimited tree hierachy.
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong>
 */
public final class InstantiationContext {

    /** All context objects. */
    private final IdentityHashMap<ContainerConfiguration, IdentityHashMap<Class<?>, Object>> map = new IdentityHashMap<>();

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T get(ContainerConfiguration configuration, Class<T> type) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(type, "type is null");
        var e = map.get(configuration);
        return e == null ? null : (T) e.get(type);
    }

    public void put(ContainerConfiguration configuration, Object obj) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(obj, "obj is null");
        map.computeIfAbsent(configuration, e -> new IdentityHashMap<>()).put(obj.getClass(), obj);
    }

    @SuppressWarnings("unchecked")
    public <T> T use(ContainerConfiguration configuration, Class<T> type) {
        requireNonNull(configuration, "configuration is null");
        requireNonNull(type, "type is null");
        var e = map.get(configuration);
        if (e == null) {
            throw new IllegalArgumentException();
        }
        Object o = e.get(type);
        if (o == null) {
            throw new IllegalStateException();
        }
        return (T) o;
    }

    enum Type {
        APP, INJECTOR; // <- Saa et image, for exempel kan lave begge dele...
    }

    // link(SomeBundle.class, LifecycleWirelets.startBeforeAnythingElse());
    // link(SomeBundle.class, LifecycleWirelets.start(SomeGroup)); //all in same group will be started
}
