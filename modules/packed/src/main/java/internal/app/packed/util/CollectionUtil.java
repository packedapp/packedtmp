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
package internal.app.packed.util;

import static java.util.Objects.requireNonNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 *
 */
public class CollectionUtil {

    public static <K, V, W> Map<K, W> copyOf(Map<? extends K, ? extends V> map, Function<? super V, ? extends W> transformer) {
        HashMap<K, W> tmp = new HashMap<>();
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            tmp.put(e.getKey(), transformer.apply(e.getValue()));
        }
        return Map.copyOf(tmp);
    }

    public static <F, T> Collection<T> unmodifiableView(Collection<F> collection, Function<? super F, ? extends T> mapper) {
        return new MappedCollection<>(collection, mapper);
    }

    /**
    *
    */
    public static final class ForwardingCollection<E> implements Collection<E> {
        final Collection<E> delegate;
        final ForwardingStrategy strategy;

        public ForwardingCollection(Collection<E> delegate, ForwardingStrategy strategy) {
            this.delegate = requireNonNull(delegate);
            this.strategy = requireNonNull(strategy);
        }

        @Override
        public boolean add(E e) {
            return delegate.add(e);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return delegate.addAll(c);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return delegate.removeIf(filter);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return delegate.toArray(generator);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }

    }

    public static final class ForwardingMap<K, V> implements Map<K, V> {
        final Map<K, V> delegate;
        final ForwardingStrategy strategy;

        public ForwardingMap(Map<K, V> delegate, ForwardingStrategy strategy) {
            this.delegate = requireNonNull(delegate);
            this.strategy = requireNonNull(strategy);
        }

        @Override
        public void clear() {
            strategy.checkRemove(this);
            delegate.clear();
        }

        @Override
        public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return delegate.compute(key, remappingFunction);
        }

        @Override
        public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
            return delegate.computeIfAbsent(key, mappingFunction);
        }

        @Override
        public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
            return delegate.computeIfPresent(key, remappingFunction);
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return new SetView<>(delegate.entrySet(), strategy);
        }

        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            delegate.forEach(action);
        }

        @Override
        public V get(Object key) {
            return delegate.get(key);
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            return delegate.getOrDefault(key, defaultValue);
        }

        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public Set<K> keySet() {
            return new SetView<>(delegate.keySet(), strategy);
        }

        @Override
        public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
            return delegate.merge(key, value, remappingFunction);
        }

        @Override
        public V put(K key, V value) {
            return delegate.put(key, value);
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            delegate.putAll(m);
        }

        @Override
        public V putIfAbsent(K key, V value) {
            return delegate.putIfAbsent(key, value);
        }

        @Override
        public V remove(Object key) {
            return delegate.remove(key);
        }

        @Override
        public boolean remove(Object key, Object value) {
            return delegate.remove(key, value);
        }

        @Override
        public V replace(K key, V value) {
            return delegate.replace(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return delegate.replace(key, oldValue, newValue);
        }

        @Override
        public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
            delegate.replaceAll(function);
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public Collection<V> values() {
            return delegate.values();
        }
    }

    // Replace with an int. And then maybe a common abstract class that can check
// or on the view itself
    public static class ForwardingStrategy {

        protected void checkInsert(Object instance) {}

        protected void checkRemove(Object instance) {}

        protected void checkUpdate(Object instance) {}
    }

    public static final class MappedCollection<F, T> extends AbstractCollection<T> {
        private final Collection<F> collection;
        private final Function<? super F, ? extends T> mapper;

        public MappedCollection(Collection<F> collection, Function<? super F, ? extends T> mapper) {
            this.collection = requireNonNull(collection);
            this.mapper = requireNonNull(mapper);
        }

        @Override
        public boolean isEmpty() {
            return collection.isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            record MappedUnmodifiableIterator<F, T> (Iterator<? extends F> iterator, Function<? super F, ? extends T> mapper) implements Iterator<T> {

                @Override
                public final boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public final T next() {
                    return mapper.apply(iterator.next());
                }
            }

            return new MappedUnmodifiableIterator<>(collection.iterator(), mapper);
        }

        @Override
        public int size() {
            return collection.size();
        }
    }

    public static final class SetView<E> implements Set<E> {

        final Set<E> delegate;

        final ForwardingStrategy strategy;

        public SetView(Set<E> delegate, ForwardingStrategy strategy) {
            this.delegate = requireNonNull(delegate);
            this.strategy = requireNonNull(strategy);
        }

        /** {@inheritDoc} */
        @Override
        public boolean add(E e) {
            return delegate.add(e);
        }

        /** {@inheritDoc} */
        @Override
        public boolean addAll(Collection<? extends E> c) {
            return delegate.addAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public void clear() {
            delegate.clear();
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(Object o) {
            return delegate.contains(o);
        }

        /** {@inheritDoc} */
        @Override
        public boolean containsAll(Collection<?> c) {
            return delegate.containsAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public boolean equals(Object o) {
            return delegate.equals(o);
        }

        /** {@inheritDoc} */
        @Override
        public void forEach(Consumer<? super E> action) {
            delegate.forEach(action);
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return delegate.hashCode();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<E> iterator() {
            return delegate.iterator();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<E> parallelStream() {
            return delegate.parallelStream();
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(Object o) {
            return delegate.remove(o);
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeAll(Collection<?> c) {
            return delegate.removeAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            return delegate.removeIf(filter);
        }

        /** {@inheritDoc} */
        @Override
        public boolean retainAll(Collection<?> c) {
            return delegate.retainAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public int size() {
            return delegate.size();
        }

        /** {@inheritDoc} */
        @Override
        public Spliterator<E> spliterator() {
            return delegate.spliterator();
        }

        /** {@inheritDoc} */
        @Override
        public Stream<E> stream() {
            return delegate.stream();
        }

        /** {@inheritDoc} */
        @Override
        public Object[] toArray() {
            return delegate.toArray();
        }

        /** {@inheritDoc} */
        @Override
        public <T> T[] toArray(IntFunction<T[]> generator) {
            return delegate.toArray(generator);
        }

        /** {@inheritDoc} */
        @Override
        public <T> T[] toArray(T[] a) {
            return delegate.toArray(a);
        }
    }

    /**
    *
    */
    public static final class UnremovableIterator<E> implements Iterator<E> {

        /** The iterator to delegate to. */
        private final Iterator<E> iterator;

        public UnremovableIterator(Iterator<E> delegate) {
            this.iterator = requireNonNull(delegate);
        }

        /** {@inheritDoc} */
        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            iterator.forEachRemaining(action);
        }

        /** {@inheritDoc} */
        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        /** {@inheritDoc} */
        @Override
        public E next() {
            return iterator.next();
        }

        /** {@inheritDoc} */
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported.");
        }
    }

}
