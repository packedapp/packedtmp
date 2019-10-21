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
package packed.internal.util;

import static java.util.Objects.requireNonNull;

/**
 *
 */

public final class ImmutableClassMap<V> {

    /** An empty set. */
    private static final ImmutableClassMap<Object> EMPTY = new ImmutableClassMap<>(0);

    /** The number of entries in the map. */
    private final int size;

    /** The keys and values. */
    private final Object[] table;

    private ImmutableClassMap(int size, Object... val) {
        this.size = size;
        this.table = requireNonNull(val);
    }

    public boolean contains(Class<?> key) {
        requireNonNull(key, "key is null");
        switch (size) {
        case 0:
            return false;
        case 1:
            return table[0] == key;
        case 2:
            return table[0] == key || table[2] == key;
        default:
            return probe(key) >= 0;
        }
    }

    @SuppressWarnings("unchecked")
    public V get(Class<?> key) {
        requireNonNull(key, "key is null");
        switch (size) {
        case 0:
            return null;
        case 1:
            return table[0] == key ? (V) table[1] : null;
        case 2:
            return table[0] == key ? (V) table[1] : table[2] == key ? (V) table[3] : null;
        default:
            int i = probe(key);
            return (i >= 0) ? (V) table[i + 1] : null;
        }
    }

    private int probe(Class<?> key) {
        int index = Math.floorMod(key.hashCode(), table.length >> 1) << 1;
        for (;;) {
            Object tabKey = table[index];
            if (tabKey == null) {
                return -1; // could not be found
            } else if (key == tabKey) {
                return index; // found it
            } else if ((index += 2) == table.length) {
                index = 0; // wrap around
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <E> ImmutableClassMap<E> of() {
        return (ImmutableClassMap<E>) EMPTY;
    }

    public static <E> ImmutableClassMap<E> of(Class<?> key, E value) {
        requireNonNull(key, "key is null");
        requireNonNull(value, "value null");
        return new ImmutableClassMap<>(1, key, value);
    }
}
