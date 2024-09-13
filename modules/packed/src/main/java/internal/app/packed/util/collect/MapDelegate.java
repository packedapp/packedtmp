package internal.app.packed.util.collect;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

// Tror
public class MapDelegate<K, F, T> implements Map<K, T> {

    private final Map<K, F> map;
    private final Function<F, T> forwardFunction;
    private final Function<T, F> backwardFunction;

    public MapDelegate(Map<K, F> map, Function<F, T> forwardFunction, Function<T, F> backwardFunction) {
        this.map = Objects.requireNonNull(map);
        this.forwardFunction = Objects.requireNonNull(forwardFunction);
        this.backwardFunction = Objects.requireNonNull(backwardFunction);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object value) {
        try {
            F fValue = backwardFunction.apply((T) value);
            return map.containsValue(fValue);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public T get(Object key) {
        F value = map.get(key);
        return value != null ? forwardFunction.apply(value) : null;
    }

    @Override
    public T put(K key, T value) {
        F oldValue = map.put(key, backwardFunction.apply(value));
        return oldValue != null ? forwardFunction.apply(oldValue) : null;
    }

    @Override
    public T remove(Object key) {
        F removedValue = map.remove(key);
        return removedValue != null ? forwardFunction.apply(removedValue) : null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends T> m) {
        for (Entry<? extends K, ? extends T> entry : m.entrySet()) {
            map.put(entry.getKey(), backwardFunction.apply(entry.getValue()));
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet(); // Direct view of the keys
    }

    @Override
    public Collection<T> values() {
        return new TransformedCollection<>(map.values(), forwardFunction);
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        return new TransformedEntrySet<>(map.entrySet(), forwardFunction, backwardFunction);
    }

    @Override
    public String toString() {
        Iterator<Entry<K, T>> i = entrySet().iterator();
        if (!i.hasNext())
            return "{}";

        StringBuilder sb = new StringBuilder();
        sb.append('{');
        while (true) {
            Entry<K, T> e = i.next();
            K key = e.getKey();
            T value = e.getValue();
            sb.append(key == this ? "(this Map)" : key);
            sb.append('=');
            sb.append(value == this ? "(this Map)" : value);
            if (!i.hasNext())
                return sb.append('}').toString();
            sb.append(',').append(' ');
        }
    }

    // Inner class for transformed collection view
    private static class TransformedCollection<F, T> implements Collection<T> {
        private final Collection<F> collection;
        private final Function<F, T> transformer;

        TransformedCollection(Collection<F> collection, Function<F, T> transformer) {
            this.collection = collection;
            this.transformer = transformer;
        }

        @Override
        public int size() {
            return collection.size();
        }

        @Override
        public boolean isEmpty() {
            return collection.isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            try {
                return collection.contains(((Function<T, F>) transformer).apply((T) o));
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public Iterator<T> iterator() {
            return new Iterator<T>() {
                private final Iterator<F> it = collection.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public T next() {
                    return transformer.apply(it.next());
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        @Override
        public Object[] toArray() {
            return stream().toArray();
        }

        @Override
        public <A> A[] toArray(A[] a) {
            return stream().toArray(size -> a);
        }

        @Override
        public boolean add(T t) {
            throw new UnsupportedOperationException("Add not supported on values view");
        }

        @Override
        public boolean remove(Object o) {
            return collection.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return c.stream().allMatch(this::contains);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException("AddAll not supported on values view");
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return collection.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return collection.retainAll(c);
        }

        @Override
        public void clear() {
            collection.clear();
        }
    }

    // Inner class for transformed entry set view
    private static class TransformedEntrySet<K, F, T> implements Set<Entry<K, T>> {
        private final Set<Entry<K, F>> entrySet;
        private final Function<F, T> forwardFunction;
        private final Function<T, F> backwardFunction;

        TransformedEntrySet(Set<Entry<K, F>> entrySet, Function<F, T> forwardFunction, Function<T, F> backwardFunction) {
            this.entrySet = entrySet;
            this.forwardFunction = forwardFunction;
            this.backwardFunction = backwardFunction;
        }

        @Override
        public int size() {
            return entrySet.size();
        }

        @Override
        public boolean isEmpty() {
            return entrySet.isEmpty();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            F fValue = backwardFunction.apply((T) e.getValue());
            return entrySet.contains(new AbstractMap.SimpleEntry<>(e.getKey(), fValue));
        }

        @Override
        public Iterator<Entry<K, T>> iterator() {
            return new Iterator<Entry<K, T>>() {
                private final Iterator<Entry<K, F>> it = entrySet.iterator();

                @Override
                public boolean hasNext() {
                    return it.hasNext();
                }

                @Override
                public Entry<K, T> next() {
                    Entry<K, F> entry = it.next();
                    return new TransformedEntry(entry);
                }

                @Override
                public void remove() {
                    it.remove();
                }
            };
        }

        // Entry class that transforms values
        private class TransformedEntry implements Entry<K, T> {
            private final Entry<K, F> entry;

            TransformedEntry(Entry<K, F> entry) {
                this.entry = entry;
            }

            @Override
            public K getKey() {
                return entry.getKey();
            }

            @Override
            public T getValue() {
                return forwardFunction.apply(entry.getValue());
            }

            @Override
            public T setValue(T value) {
                F oldFValue = entry.setValue(backwardFunction.apply(value));
                return forwardFunction.apply(oldFValue);
            }

            @Override
            public boolean equals(Object o) {
                if (!(o instanceof Entry))
                    return false;
                Entry<?, ?> e = (Entry<?, ?>) o;
                return Objects.equals(getKey(), e.getKey()) && Objects.equals(getValue(), e.getValue());
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
            }
        }

        @Override
        public Object[] toArray() {
            return stream().toArray();
        }

        @Override
        public <A> A[] toArray(A[] a) {
            return stream().toArray(size -> a);
        }

        @Override
        public boolean add(Entry<K, T> e) {
            return entrySet.add(new AbstractMap.SimpleEntry<>(e.getKey(), backwardFunction.apply(e.getValue())));
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            return entrySet.remove(new AbstractMap.SimpleEntry<>(e.getKey(), backwardFunction.apply((T) e.getValue())));
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return c.stream().allMatch(this::contains);
        }

        @Override
        public boolean addAll(Collection<? extends Entry<K, T>> c) {
            boolean modified = false;
            for (Entry<K, T> e : c) {
                modified |= add(e);
            }
            return modified;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return entrySet.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return entrySet.removeAll(c);
        }

        @Override
        public void clear() {
            entrySet.clear();
        }
    }
}
