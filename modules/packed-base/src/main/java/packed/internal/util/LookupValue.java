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

import java.lang.invoke.MethodHandles;
import java.util.concurrent.ConcurrentHashMap;

/** Lazily associate a computed value with a lookup object much like {@link ClassValue} but for lookup objects. */
public abstract class LookupValue<T> {

    /** The cache of values. */
    private final ClassValue<ConcurrentHashMap<Integer, T>> LOOKUP_CACHE = new ClassValue<>() {

        /** {@inheritDoc} */
        @Override
        protected ConcurrentHashMap<Integer, T> computeValue(Class<?> type) {
            return new ConcurrentHashMap<Integer, T>(1);
        }
    };

    /**
     * Computes the given lookup objects's derived value for this {@code LookupValue}.
     * <p>
     * This method will be invoked within the first thread that accesses the value with the {@link #get get} method.
     * <p>
     * If this method throws an exception, the corresponding call to {@code get} will terminate abnormally with that
     * exception, and no lookup value will be recorded.
     *
     * @param lookup
     *            the lookup object for which a value must be computed
     * @return the newly computed value associated with this {@code LookupValue}, for the given lookup object
     */
    protected abstract T computeValue(MethodHandles.Lookup lookup);

    /**
     * Returns the value for the given lookup object. If no value has yet been computed, it is obtained by an invocation of
     * the {@link #computeValue computeValue} method.
     * 
     * @param lookup
     *            the lookup object
     * @return the value for the given lookup object
     */
    public final T get(MethodHandles.Lookup lookup) {
        ConcurrentHashMap<Integer, T> chm = LOOKUP_CACHE.get(lookup.lookupClass());
        Integer lookupMode = Integer.valueOf(lookup.lookupModes());
        T value = chm.get(lookupMode);
        if (value == null) {
            value = chm.computeIfAbsent(lookupMode, ignore -> computeValue(lookup));
        }
        return value;
    }
}
