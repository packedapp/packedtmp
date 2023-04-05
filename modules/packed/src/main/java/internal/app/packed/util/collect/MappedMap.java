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
package internal.app.packed.util.collect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 *
 */
public final class MappedMap<K, V, M> extends ImmutableMap<K, M> {

    final Map<K, V> map;

    final ValueMapper<V, ? extends M> mapper;

    public MappedMap(Map<K, V> map, ValueMapper<V, ? extends M> mapper) {
        this.mapper = mapper;
        this.map = map;
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        Optional<Object> v = mapper.forValueSearch(value);
        if (v.isEmpty()) {
            return false;
        }
        return map.containsValue(v.get());
    }

    @Override
    public Set<Entry<K, M>> entrySet() {
        return new MappedEntrySet<>(this);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super M> action) {
        super.forEach(action);
    }

    @Override
    public M get(Object key) {
        V v = map.get(key);
        return mapper.mapValue(v);
    }

    @Override
    public M getOrDefault(Object key, M defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Collection<M> values() {
        return new MappedCollection<>(map.values(), mapper);
    }

    private static /* value */ class MappedEntrySet<K, V, M> extends ImmutableSet<Map.Entry<K, M>> {

        private final MappedMap<K, V, M> mappedMap;

        private MappedEntrySet(MappedMap<K, V, M> mappedMap) {
            this.mappedMap = mappedMap;
        }

        @Override
        public boolean isEmpty() {
            return mappedMap.isEmpty();
        }

        @Override
        public Iterator<Entry<K, M>> iterator() {
            Iterator<Entry<K, V>> iter = mappedMap.map.entrySet().iterator();
            return new Iterator<Entry<K, M>>() {

                @Override
                public boolean hasNext() {
                    return iter.hasNext();
                }

                @Override
                public Entry<K, M> next() {
                    Entry<K, V> next = iter.next();
                    M value = mappedMap.mapper.mapValue(next.getValue());
                    return Map.entry(next.getKey(), value);
                }
            };
        }

        @Override
        public int size() {
            return mappedMap.size();
        }
    }
}
