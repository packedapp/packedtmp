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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import app.packed.lang.Nullable;

/**
 * A concurrent weak intern set.
 * 
 * @param <T>
 *            the interned type
 */
public final class ConcurrentWeakInternedSet<T> {

    /** All interned elements. */
    private final ConcurrentMap<InternedEntry<T>, InternedEntry<T>> elements = new ConcurrentHashMap<>();

    /** The reference queue. */
    private final ReferenceQueue<T> stale = new ReferenceQueue<>();

    /** Expunge stale elements. */
    private void expungeStaleElements() {
        for (Reference<? extends T> reference; (reference = stale.poll()) != null;) {
            elements.remove(reference);
        }
    }

    /**
     * Returns an existing interned element if present, otherwise null.
     *
     * @param element
     *            the element to get
     * @return the interned element
     */
    @Nullable
    public T get(T element) {
        requireNonNull(element, "element is null");
        expungeStaleElements();
        InternedEntry<T> value = elements.get(new InternedEntry<>(element));
        return value == null ? null : value.get();
    }

    /**
     * Tries to intern the specified element. Returns the interned if it was successfully added. If a race to add this
     * element was lost to another invocation of this method with an equivalent element. This method will return the other
     * (equivalent) element. In any case this method always returns a non-null result.
     *
     * @param element
     *            the element to intern
     * @return the element that was actually interned
     */
    public T tryIntern(T element) {
        requireNonNull(element, "element is null");

        T result;
        InternedEntry<T> newEntry = new InternedEntry<>(element, stale);
        do {
            expungeStaleElements();
            InternedEntry<T> existing = elements.putIfAbsent(newEntry, newEntry);
            result = existing == null ? element : existing.get();
        } while (result == null);
        return result;
    }

    /** The interned entry. */
    private static class InternedEntry<T> extends WeakReference<T> {

        /** The hash code of the element. */
        private final int hashCode;

        private InternedEntry(T key) {
            super(key);
            hashCode = key.hashCode();
        }

        private InternedEntry(T key, ReferenceQueue<T> queue) {
            super(key, queue);
            hashCode = key.hashCode();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object other) {
            return other instanceof InternedEntry && Objects.equals(((InternedEntry<T>) other).get(), get());
        }

        /** {@inheritDoc} */
        @Override
        public int hashCode() {
            return hashCode;
        }
    }
}
