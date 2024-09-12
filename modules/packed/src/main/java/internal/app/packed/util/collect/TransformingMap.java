package internal.app.packed.util.collect;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

// Single class, may
public final class TransformingMap<K, F, T> implements Map<K, T> {
    private final Map<K, F> originalMap;
    private final Function<T, F> reverseTransform;
    private final Function<F, T> transform;

    public TransformingMap(Map<K, F> originalMap, Function<F, T> transform, Function<T, F> reverseTransform) {
        this.originalMap = originalMap;
        this.transform = transform;
        this.reverseTransform = reverseTransform;
    }

    @Override
    public void clear() {
        originalMap.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return originalMap.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object value) {
        return originalMap.containsValue(reverseTransform.apply((T) value));
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        return new AbstractSet<Entry<K, T>>() {
            @Override
            public boolean contains(Object o) {
                if (!(o instanceof Entry))
                    return false;
                Entry<?, ?> entry = (Entry<?, ?>) o;
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (!containsKey(key))
                    return false;
                return get(key).equals(value);
            }

            @Override
            public Iterator<Entry<K, T>> iterator() {
                return new Iterator<Entry<K, T>>() {
                    private final Iterator<Entry<K, F>> originalIterator = originalMap.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return originalIterator.hasNext();
                    }

                    @Override
                    public Entry<K, T> next() {
                        Entry<K, F> originalEntry = originalIterator.next();
                        return new Entry<K, T>() {
                            @Override
                            public K getKey() {
                                return originalEntry.getKey();
                            }

                            @Override
                            public T getValue() {
                                return transform.apply(originalEntry.getValue());
                            }

                            @Override
                            public T setValue(T value) {
                                F oldValue = originalEntry.setValue(reverseTransform.apply(value));
                                return transform.apply(oldValue);
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        originalIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return originalMap.size();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Map))
            return false;
        Map<?, ?> m = (Map<?, ?>) o;
        if (m.size() != size())
            return false;
        try {
            for (Entry<K, T> e : entrySet()) {
                K key = e.getKey();
                T value = e.getValue();
                if (value == null) {
                    if (!(m.get(key) == null && m.containsKey(key)))
                        return false;
                } else {
                    if (!value.equals(m.get(key)))
                        return false;
                }
            }
        } catch (ClassCastException | NullPointerException unused) {
            return false;
        }
        return true;
    }

    @Override
    public T get(Object key) {
        F value = originalMap.get(key);
        return value == null ? null : transform.apply(value);
    }

    @Override
    public int hashCode() {
        return entrySet().hashCode();
    }

    @Override
    public boolean isEmpty() {
        return originalMap.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return originalMap.keySet();
    }

    @Override
    public T put(K key, T value) {
        F originalValue = originalMap.put(key, reverseTransform.apply(value));
        return originalValue == null ? null : transform.apply(originalValue);
    }

    @Override
    public void putAll(Map<? extends K, ? extends T> m) {
        for (Entry<? extends K, ? extends T> entry : m.entrySet()) {
            originalMap.put(entry.getKey(), reverseTransform.apply(entry.getValue()));
        }
    }

    @Override
    public T remove(Object key) {
        F originalValue = originalMap.remove(key);
        return originalValue == null ? null : transform.apply(originalValue);
    }

    @Override
    public int size() {
        return originalMap.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        for (Entry<K, T> entry : entrySet()) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.append("}").toString();
    }

    @Override
    public Collection<T> values() {
        return new AbstractCollection<T>() {
            @Override
            public boolean contains(Object o) {
                return TransformingMap.this.containsValue(o);
            }

            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private final Iterator<F> originalIterator = originalMap.values().iterator();

                    @Override
                    public boolean hasNext() {
                        return originalIterator.hasNext();
                    }

                    @Override
                    public T next() {
                        return transform.apply(originalIterator.next());
                    }

                    @Override
                    public void remove() {
                        originalIterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return originalMap.size();
            }
        };
    }
}
