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
package internal.app.packed.service.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.Function;

import app.packed.binding.Key;
import internal.app.packed.ValueBased;

/**
 * A simple map to hold services.
 */
@ValueBased
public final class ServiceMap<V> implements Iterable<V> {

    private final LinkedHashMap<Key<?>, V> services = new LinkedHashMap<>();

    public V computeIfAbsent(Key<?> key, Function<? super Key<?>, ? extends V> mappingFunction) {
        return services.computeIfAbsent(key, mappingFunction);
    }

    public boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }
    public boolean contains(Key<?> key) {
        return services.containsKey(key);
    }

    /**
     * @param key
     * @return
     */
    public V get(Key<?> key) {
        return services.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<V> iterator() {
        return services.values().iterator();
    }

    /**
     * @return
     */
    public Set<Key<?>> keySet() {
        return services.keySet();
    }

    public V put(Key<?> key, V value) {
        return services.put(key, value);
    }
    public V putIfAbsent(Key<?> key, V value) {
        return services.putIfAbsent(key, value);
    }

    /**
     * @param key
     */
    public V remove(Key<?> key) {
        return services.remove(key);
    }

    public <E> Map<Key<?>, E> toUnmodifiableMap(Function<? super V, ? extends E> mapper) {
        HashMap<Key<?>, E> tmp = new HashMap<>();
        for (Map.Entry<? extends Key<?>, ? extends V> e : services.entrySet()) {
            tmp.put(e.getKey(), mapper.apply(e.getValue()));
        }
        return Map.copyOf(tmp);
    }

    public <E> SequencedMap<Key<?>, E> toUnmodifiableSequenceMap(Function<? super V, ? extends E> mapper) {
        LinkedHashMap<Key<?>, E> tmp = new LinkedHashMap<>();
        for (Map.Entry<? extends Key<?>, ? extends V> e : services.entrySet()) {
            tmp.put(e.getKey(), mapper.apply(e.getValue()));
        }
        return Collections.unmodifiableSequencedMap(tmp);

    }
}
