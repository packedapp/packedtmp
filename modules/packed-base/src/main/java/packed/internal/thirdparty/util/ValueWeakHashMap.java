package packed.internal.thirdparty.util;
/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the  domain, as explained at
 * http://creativecommons.org/licenses/domain
 */

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

//Hava a look at https://github.com/raphw/weak-lock-free/blob/master/src/main/java/com/blogspot/mydailyjava/weaklockfree/WeakConcurrentMap.java
public class ValueWeakHashMap<K, V> {

    /*
     * The basic strategy is to subdivide the table among Segments, each of which itself is a concurrently readable hash
     * table.
     */

    /* ---------------- Constants -------------- */

    /**
     * The default concurrency level for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    /**
     * The default initial capacity for this table, used when not otherwise specified in a constructor.
     */
    static final int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for this table, used when not otherwise specified in a constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The maximum number of segments to allow; used to bound constructor arguments.
     */
    static final int MAX_SEGMENTS = 1 << 16; // slightly conservative

    /**
     * The maximum capacity, used if a higher value is implicitly specified by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30 to ensure that entries are indexable using ints.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Number of unsynchronized retries in size and containsValue methods before resorting to locking. This is used to avoid
     * unbounded retries if tables undergo continuous modification which would make it impossible to obtain an accurate
     * result.
     */
    static final int RETRIES_BEFORE_LOCK = 2;

    /**
     * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the segment.
     */
    final int segmentMask;

    /**
     * The segments, each of which is a specialized hash table
     */
    final Segment<K, V>[] segments;
    /**
     * Shift value for indexing within segments.
     */
    final int segmentShift;

    /**
     * Creates a new, empty map with a default initial capacity (16), load factor (0.75) and concurrencyLevel (16).
     */
    public ValueWeakHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
    }

    /**
     * Creates a new, empty map with the specified initial capacity, load factor and concurrency level.
     *
     * @param initialCapacity
     *            the initial capacity. The implementation performs internal sizing to accommodate this many elements.
     * @param loadFactor
     *            the load factor threshold, used to control resizing. Resizing may be performed when the average number of
     *            elements per bin exceeds this threshold.
     * @param concurrencyLevel
     *            the estimated number of concurrently updating threads. The implementation performs internal sizing to try
     *            to accommodate this many threads.
     * @throws IllegalArgumentException
     *             if the initial capacity is negative or the load factor or concurrencyLevel are nonpositive.
     */
    private ValueWeakHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
            throw new IllegalArgumentException();

        if (concurrencyLevel > MAX_SEGMENTS)
            concurrencyLevel = MAX_SEGMENTS;

        // Find power-of-two sizes best matching arguments
        int sshift = 0;
        int ssize = 1;
        while (ssize < concurrencyLevel) {
            ++sshift;
            ssize <<= 1;
        }
        segmentShift = 32 - sshift;
        segmentMask = ssize - 1;
        this.segments = Segment.newArray(ssize);

        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        int c = initialCapacity / ssize;
        if (c * ssize < initialCapacity)
            ++c;
        int cap = 1;
        while (cap < c)
            cap <<= 1;

        for (int i = 0; i < this.segments.length; ++i)
            this.segments[i] = new Segment<K, V>(cap, loadFactor);
    }

    /* ---------------- Public operations -------------- */

    /**
     * Removes all of the mappings from this map.
     */
    void clear() {
        for (int i = 0; i < segments.length; ++i)
            segments[i].clear();
    }

    /**
     * Tests if the specified object is a key in this table.
     *
     * @param key
     *            possible key
     * @return <tt>true</tt> if and only if the specified object is a key in this table, as determined by the
     *         <tt>equals</tt> method; <tt>false</tt> otherwise.
     * @throws NullPointerException
     *             if the specified key is null
     */
    boolean containsKey(Object key) {
        int hash = hash(key.hashCode());
        return segmentFor(hash).containsKey(key, hash);
    }

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this map contains no mapping for the key.
     *
     * <p>
     * More formally, if this map contains a mapping from a key {@code k} to a value {@code v} such that
     * {@code key.equals(k)}, then this method returns {@code v}; otherwise it returns {@code null}. (There can be at most
     * one such mapping.)
     *
     * @throws NullPointerException
     *             if the specified key is null
     */
    public V get(Object key) {
        int hash = hash(key.hashCode());
        return segmentFor(hash).get(key, hash);
    }

    /**
     * Maps the specified key to the specified value in this table. Neither the key nor the value can be null.
     *
     * <p>
     * The value can be retrieved by calling the <tt>get</tt> method with a key that is equal to the original key.
     *
     * @param key
     *            key with which the specified value is to be associated
     * @param value
     *            value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key or value is null
     */
    V put(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        return segmentFor(hash).put(key, hash, value, false);
    }

    V putIfAbsent(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        return segmentFor(hash).put(key, hash, value, true);
    }

    /**
     * Removes the key (and its corresponding value) from this map. This method does nothing if the key is not in the map.
     *
     * @param key
     *            the key that needs to be removed
     * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>
     * @throws NullPointerException
     *             if the specified key is null
     */
    V remove(Object key) {
        int hash = hash(key.hashCode());
        return segmentFor(hash).remove(key, hash, null, false);
    }

    boolean remove(Object key, Object value) {
        int hash = hash(key.hashCode());
        if (value == null)
            return false;
        return segmentFor(hash).remove(key, hash, value, false) != null;
    }

    V replace(K key, V value) {
        if (value == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        return segmentFor(hash).replace(key, hash, value);
    }

    boolean replace(K key, V oldValue, V newValue) {
        if (oldValue == null || newValue == null)
            throw new NullPointerException();
        int hash = hash(key.hashCode());
        return segmentFor(hash).replace(key, hash, oldValue, newValue);
    }

    /**
     * Returns the segment that should be used for key with given hash
     * 
     * @param hash
     *            the hash code for the key
     * @return the segment
     */
    final Segment<K, V> segmentFor(int hash) {
        return segments[(hash >>> segmentShift) & segmentMask];
    }

    /**
     * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash functions. This is
     * critical because ConcurrentWeakHashMap uses power-of-two length hash tables, that otherwise encounter collisions for
     * hashCodes that do not differ in lower or upper bits.
     */
    private static int hash(int h) {
        // Spread bits to regularize both segment and index locations,
        // using variant of single-word Wang/Jenkins hash.
        h += (h << 15) ^ 0xffffcd7d;
        h ^= (h >>> 10);
        h += (h << 3);
        h ^= (h >>> 6);
        h += (h << 2) + (h << 14);
        return h ^ (h >>> 16);
    }

    /**
     * ConcurrentWeakHashMap list entry. Note that this is never exported out as a user-visible Map.Entry.
     *
     * Because the value field is volatile, not final, it is legal wrt the Java Memory Model for an unsynchronized reader to
     * see null instead of initial value when read via a data race. Although a reordering leading to this is not likely to
     * ever actually occur, the Segment.readValueUnderLock method is used as a backup in case a null (pre-initialized) value
     * is ever seen in an unsynchronized access method.
     */
    static final class HashEntry<K, V> {
        final int hash;
        final WeakReference<K> keyRef;
        final HashEntry<K, V> next;
        volatile V value;

        HashEntry(K key, int hash, HashEntry<K, V> next, V value, ReferenceQueue<K> refQueue) {
            this.keyRef = new WeakKeyReference<K>(key, hash, refQueue);
            this.hash = hash;
            this.next = next;
            this.value = value;
        }

        @SuppressWarnings("unchecked")
        static final <K, V> HashEntry<K, V>[] newArray(int i) {
            return new HashEntry[i];
        }
    }

    /**
     * Segments are specialized versions of hash tables. This subclasses from ReentrantLock opportunistically, just to
     * simplify some locking and avoid separate construction.
     */
    static final class Segment<K, V> extends ReentrantLock implements Serializable {
        /*
         * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be read without locking.
         * Next fields of nodes are immutable (final). All list additions are performed at the front of each bin. This makes it
         * easy to check changes, and also fast to traverse. When nodes would otherwise be changed, new nodes are created to
         * replace them. This works well for hash tables since the bin lists tend to be short. (The average length is less than
         * two for the default load factor threshold.)
         *
         * Read operations can thus proceed without locking, but rely on selected uses of volatiles to ensure that completed
         * write operations performed by other threads are noticed. For most purposes, the "count" field, tracking the number of
         * elements, serves as that volatile variable ensuring visibility. This is convenient because this field needs to be
         * read in many read operations anyway:
         *
         * - All (unsynchronized) read operations must first read the "count" field, and should not look at table entries if it
         * is 0.
         *
         * - All (synchronized) write operations should write to the "count" field after structurally changing any bin. The
         * operations must not take any action that could even momentarily cause a concurrent read operation to see inconsistent
         * data. This is made easier by the nature of the read operations in Map. For example, no operation can reveal that the
         * table has grown but the threshold has not yet been updated, so there are no atomicity requirements for this with
         * respect to reads.
         *
         * As a guide, all critical volatile reads and writes to the count field are marked in code comments.
         */

        private static final long serialVersionUID = 2249069246763182397L;

        /**
         * The number of elements in this segment's region.
         */
        transient volatile int count;

        /**
         * The load factor for the hash table. Even though this value is same for all segments, it is replicated to avoid
         * needing links to outer object.
         * 
         * @serial
         */
        final float loadFactor;

        /**
         * Number of updates that alter the size of the table. This is used during bulk-read methods to make sure they see a
         * consistent snapshot: If modCounts change during a traversal of segments computing size or checking containsValue,
         * then we might have an inconsistent view of state so (usually) must retry.
         */
        transient int modCount;

        /**
         * The collected weak-key reference queue for this segment. This should be (re)initialized whenever table is assigned,
         */
        transient volatile ReferenceQueue<K> refQueue;

        /**
         * The per-segment table.
         */
        transient volatile HashEntry<K, V>[] table;

        /**
         * The table is rehashed when its size exceeds this threshold. (The value of this field is always <tt>(int)(capacity *
         * loadFactor)</tt>.)
         */
        transient int threshold;

        Segment(int initialCapacity, float lf) {
            loadFactor = lf;
            setTable(HashEntry.<K, V>newArray(initialCapacity));
        }

        void clear() {
            if (count != 0) {
                lock();
                try {
                    HashEntry<K, V>[] tab = table;
                    for (int i = 0; i < tab.length; i++)
                        tab[i] = null;
                    ++modCount;
                    // replace the reference queue to avoid unnecessary stale cleanups
                    refQueue = new ReferenceQueue<K>();
                    count = 0; // write-volatile
                } finally {
                    unlock();
                }
            }
        }

        boolean containsKey(Object key, int hash) {
            if (count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key.equals(e.keyRef.get()))
                        return true;
                    e = e.next;
                }
            }
            return false;
        }

        boolean containsValue(Object value) {
            if (count != 0) { // read-volatile
                HashEntry<K, V>[] tab = table;
                int len = tab.length;
                for (int i = 0; i < len; i++) {
                    for (HashEntry<K, V> e = tab[i]; e != null; e = e.next) {
                        V v = e.value;
                        if (v == null) // recheck
                            v = readValueUnderLock(e);
                        if (value.equals(v))
                            return true;
                    }
                }
            }
            return false;
        }

        V get(Object key, int hash) {
            if (count != 0) { // read-volatile
                HashEntry<K, V> e = getFirst(hash);
                while (e != null) {
                    if (e.hash == hash && key.equals(e.keyRef.get())) {
                        V v = e.value;
                        if (v != null)
                            return v;
                        return readValueUnderLock(e); // recheck
                    }
                    e = e.next;
                }
            }
            return null;
        }

        /* Specialized implementations of map methods */

        /**
         * Returns properly casted first entry of bin for given hash.
         */
        HashEntry<K, V> getFirst(int hash) {
            HashEntry<K, V>[] tab = table;
            return tab[hash & (tab.length - 1)];
        }

        V put(K key, int hash, V value, boolean onlyIfAbsent) {
            lock();
            try {
                removeStale();
                int c = count;
                if (c++ > threshold) {// ensure capacity
                    int reduced = rehash();
                    if (reduced > 0) // adjust from possible weak cleanups
                        count = (c -= reduced) - 1; // write-volatile
                }

                HashEntry<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                HashEntry<K, V> first = tab[index];
                HashEntry<K, V> e = first;
                while (e != null && (e.hash != hash || !key.equals(e.keyRef.get())))
                    e = e.next;

                V oldValue;
                if (e != null) {
                    oldValue = e.value;
                    if (!onlyIfAbsent)
                        e.value = value;
                } else {
                    oldValue = null;
                    ++modCount;
                    tab[index] = new HashEntry<K, V>(key, hash, first, value, refQueue);
                    count = c; // write-volatile
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        /**
         * Reads value field of an entry under lock. Called if value field ever appears to be null. This is possible only if a
         * compiler happens to reorder a HashEntry initialization with its table assignment, which is legal under memory model
         * but is not known to ever occur.
         */
        V readValueUnderLock(HashEntry<K, V> e) {
            lock();
            try {
                removeStale();
                return e.value;
            } finally {
                unlock();
            }
        }

        int rehash() {
            HashEntry<K, V>[] oldTable = table;
            int oldCapacity = oldTable.length;
            if (oldCapacity >= MAXIMUM_CAPACITY)
                return 0;

            /*
             * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the elements from each bin
             * must either stay at same index, or move with a power of two offset. We eliminate unnecessary node creation by
             * catching cases where old nodes can be reused because their next fields won't change. Statistically, at the default
             * threshold, only about one-sixth of them need cloning when a table doubles. The nodes they replace will be garbage
             * collectable as soon as they are no longer referenced by any reader thread that may be in the midst of traversing
             * table right now.
             */

            HashEntry<K, V>[] newTable = HashEntry.newArray(oldCapacity << 1);
            threshold = (int) (newTable.length * loadFactor);
            int sizeMask = newTable.length - 1;
            int reduce = 0;
            for (int i = 0; i < oldCapacity; i++) {
                // We need to guarantee that any existing reads of old Map can
                // proceed. So we cannot yet null out each bin.
                HashEntry<K, V> e = oldTable[i];

                if (e != null) {
                    HashEntry<K, V> next = e.next;
                    int idx = e.hash & sizeMask;

                    // Single node on list
                    if (next == null)
                        newTable[idx] = e;

                    else {
                        // Reuse trailing consecutive sequence at same slot
                        HashEntry<K, V> lastRun = e;
                        int lastIdx = idx;
                        for (HashEntry<K, V> last = next; last != null; last = last.next) {
                            int k = last.hash & sizeMask;
                            if (k != lastIdx) {
                                lastIdx = k;
                                lastRun = last;
                            }
                        }
                        newTable[lastIdx] = lastRun;
                        // Clone all remaining nodes
                        for (HashEntry<K, V> p = e; p != lastRun; p = p.next) {
                            // Skip GC'd weak refs
                            K key = p.keyRef.get();
                            if (key == null) {
                                reduce++;
                                continue;
                            }
                            int k = p.hash & sizeMask;
                            HashEntry<K, V> n = newTable[k];
                            newTable[k] = new HashEntry<K, V>(key, p.hash, n, p.value, refQueue);
                        }
                    }
                }
            }
            table = newTable;
            return reduce;
        }

        /**
         * Remove; match on key only if value null, else match both.
         */
        V remove(Object key, int hash, Object value, boolean weakRemove) {
            lock();
            try {
                if (!weakRemove)
                    removeStale();
                int c = count - 1;
                HashEntry<K, V>[] tab = table;
                int index = hash & (tab.length - 1);
                HashEntry<K, V> first = tab[index];
                HashEntry<K, V> e = first;
                // a weak remove operation compares the WeakReference instance
                while (e != null && (!weakRemove || key != e.keyRef) && (e.hash != hash || !key.equals(e.keyRef.get())))
                    e = e.next;

                V oldValue = null;
                if (e != null) {
                    V v = e.value;
                    if (value == null || value.equals(v)) {
                        oldValue = v;
                        // All entries following removed node can stay
                        // in list, but all preceding ones need to be
                        // cloned.
                        ++modCount;
                        HashEntry<K, V> newFirst = e.next;
                        for (HashEntry<K, V> p = first; p != e; p = p.next) {
                            K pKey = p.keyRef.get();
                            if (pKey == null) { // Skip GC'd keys
                                c--;
                                continue;
                            }

                            newFirst = new HashEntry<K, V>(pKey, p.hash, newFirst, p.value, refQueue);
                        }
                        tab[index] = newFirst;
                        count = c; // write-volatile
                    }
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        @SuppressWarnings("unchecked")
        void removeStale() {
            WeakKeyReference<K> ref;
            while ((ref = (WeakKeyReference<K>) refQueue.poll()) != null) {
                remove(ref, ref.hash, null, true);
            }
        }

        V replace(K key, int hash, V newValue) {
            lock();
            try {
                removeStale();
                HashEntry<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || !key.equals(e.keyRef.get())))
                    e = e.next;

                V oldValue = null;
                if (e != null) {
                    oldValue = e.value;
                    e.value = newValue;
                }
                return oldValue;
            } finally {
                unlock();
            }
        }

        boolean replace(K key, int hash, V oldValue, V newValue) {
            lock();
            try {
                removeStale();
                HashEntry<K, V> e = getFirst(hash);
                while (e != null && (e.hash != hash || !key.equals(e.keyRef.get())))
                    e = e.next;

                boolean replaced = false;
                if (e != null && oldValue.equals(e.value)) {
                    replaced = true;
                    e.value = newValue;
                }
                return replaced;
            } finally {
                unlock();
            }
        }

        /**
         * Sets table to new HashEntry array. Call only while holding lock or in constructor.
         */
        void setTable(HashEntry<K, V>[] newTable) {
            threshold = (int) (newTable.length * loadFactor);
            table = newTable;
            refQueue = new ReferenceQueue<K>();
        }

        @SuppressWarnings("unchecked")
        static final <K, V> Segment<K, V>[] newArray(int i) {
            return new Segment[i];
        }
    }

    /** A weak-key reference which stores the key hash needed for reclamation. */
    static final class WeakKeyReference<K> extends WeakReference<K> {
        final int hash;

        WeakKeyReference(K key, int hash, ReferenceQueue<K> refQueue) {
            super(key, refQueue);
            this.hash = hash;
        }
    }
}
