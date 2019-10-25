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
package packed.internal.util.tiny;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import app.packed.lang.Nullable;

/**
 *
 */
public final class Linked<E> {

    public final E element;

    @Nullable
    final Linked<E> next;

    private final int predecessors;

    public Linked(E element, @Nullable Linked<E> next) {
        this.element = requireNonNull(element);
        this.next = next;
        this.predecessors = next == null ? 0 : next.predecessors + 1;
    }

    public static <E> boolean anyMatch(@Nullable Linked<E> node, Predicate<? super E> predicate) {
        requireNonNull(predicate, "predicate is null");
        for (; node != null; node = node.next) {
            if (predicate.test(node.element)) {
                return true;
            }
        }
        return false;
    }

    public static <T> Set<T> toSet(@Nullable Linked<T> node) {
        Set<T> set = toSetOrNull(node);
        return set == null ? Set.of() : set;
    }

    @Nullable
    public static <T> Set<T> toSetOrNull(@Nullable Linked<T> node) {
        if (node == null) {
            return null;
        }
        switch (node.predecessors) {
        case 0:
            return Set.of(node.element);
        case 1:
            return Set.of(node.element, node.next.element);
        case 2:
            Linked<T> prev = node.next;
            return Set.of(node.element, prev.element, prev.next.element);
        case 3:
            prev = node.next;
            Linked<T> prevPrev = prev.next;
            return Set.of(node.element, prev.element, prevPrev.element, prevPrev.next.element);
        }
        HashSet<T> set = new HashSet<>(node.predecessors + 1);
        for (Linked<T> t = node; t != null; t = t.next) {
            if (!set.add(t.element)) {
                throw new IllegalArgumentException("Contains more than one elements of " + t.element);
            }
        }
        return Set.copyOf(set);
    }

    public static <K, V> Map<K, Set<V>> toMapOrNull(Map<K, Linked<V>> map) {
        if (map == null) {
            return null;
        }
        switch (map.size()) {
        case 0:
            return null;
        case 1:
            Entry<K, Linked<V>> e = map.entrySet().iterator().next();
            return Map.of(e.getKey(), toSet(e.getValue()));
        case 2:
            e = map.entrySet().iterator().next();
            Entry<K, Linked<V>> e1 = map.entrySet().iterator().next();
            return Map.of(e.getKey(), toSet(e.getValue()), e1.getKey(), toSet(e1.getValue()));
        }
        throw new UnsupportedOperationException("Unsupported");
    }
}
