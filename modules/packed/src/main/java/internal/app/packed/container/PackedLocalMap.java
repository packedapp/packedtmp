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

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import app.packed.util.Nullable;

/**
 * A map of bean or container locals.
 */
public final class PackedLocalMap {

    /** This map containing every local. */
    private final ConcurrentHashMap<LocalKey, Object> locals = new ConcurrentHashMap<>();

    public <T> T get(PackedLocal<T> local, Object key) {
        requireNonNull(local, "local is null");
        T t = getNullable(local, key);
        if (t == null) {
            throw new NoSuchElementException("A value has not been set for the specified local");
        }
        return t;
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T getNullable(PackedLocal<T> local, Object key) {
        Supplier<? extends T> ivs = local.initialValueSupplier;
        if (ivs == null) {
            return (T) locals.get(new LocalKey(local, key));
        } else {
            return (T) locals.computeIfAbsent(new LocalKey(local, key), e -> ivs.get());
        }
    }

    public boolean isBound(PackedLocal<?> local, Object key) {
        return locals.contains(new LocalKey(local, key));
    }

    public <T> @Nullable T orElse(PackedLocal<T> local, Object key, T other) {
        T t = getNullable(local, key);
        return t == null ? other : t;
    }

    public <X extends Throwable, T> @Nullable T orElseThrow(PackedLocal<T> local, Object key, Supplier<? extends X> exceptionSupplier) throws X {
        T t = getNullable(local, key);
        if (t == null) {
            throw exceptionSupplier.get();
        }
        return t;
    }

    public <T> void set(PackedLocal<T> local, Object key, T value) {
        requireNonNull(value, "value is null");

        // But writing initial value is okay???

        locals.put(new LocalKey(local, key), value);
    }

    private record LocalKey(PackedLocal<?> local, Object value) {}
}
