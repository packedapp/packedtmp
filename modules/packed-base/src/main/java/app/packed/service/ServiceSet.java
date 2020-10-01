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
package app.packed.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import app.packed.base.Key;
import app.packed.inject.Provider;
import app.packed.inject.Service;
import app.packed.inject.ServiceRegistry;

/**
 * A set of services that may contain multiple services with the same key.
 */
// For en plugin struktur. Skal vi havde adgang til hvilke modul der definere den
// Maaske kan vi smide det paa som en attribute???

// ServiceCollection?

// Vi vil gerne paa en eller anden maade have Attributes, ServiceDescriptors, osv med...
// ServiceDescriptor -> Instance

// ServiceMultiSet????
// Vil gerne have den externer ServiceSystem, evt ServiceRegistry... Maaske

// Kan ikke extende ServiceRegistry da den extender ServiceMap som har T findService()... <--- can return max 1

// Service sets in service sets??? IDK

public interface ServiceSet<S> extends Iterable<S> {

    Set<Service> descriptors();

    // ServiceProvider????
    void forEach(BiConsumer<Service, Provider<S>> action);

    /**
     * Returns whether or not all of the services contained in this set has a unique key.
     * <p>
     * If this method returns false. The value returned by {@link #size()} will be greater than the number of keys returned
     * by {@link #keys()}.
     * 
     * @return whether or not all services have a unique key
     */
    boolean isAllUniqueKeys();

    boolean isEmpty();

    /**
     * Returns an immutable set of keys for each service in this set. If there are multiple services with the same key, only
     * one key instance will be present in the set.
     * 
     * @return an immutable set of keys for each service in this set
     */
    Set<Key<? extends S>> keys();

    // ???? Er ikke sikker paa vi skal have providere her. Kun "descriptore"
    Set<Provider<S>> providers();

    // Throw ISE if not all services are unique
    /**
     * @return a service map
     * @throws UnsupportedOperationException
     *             if all keys in this are not unique
     * 
     */
    ServiceRegistry toServiceMap();

    /**
     * Returns the number of services in this set.
     * <p>
     * This number will differ from the number of keys as returned by {@link #keys()}. Unless all keys are unique
     * 
     * @return the number of services in this set
     */
    int size();

    Map<Key<?>, Collection<Service>> all();

    /**
     * Returns a sequential {@code Stream} with this collection as its source.
     *
     * <p>
     * This method should be overridden when the {@link #spliterator()} method cannot return a spliterator that is
     * {@code IMMUTABLE}, {@code CONCURRENT}, or <em>late-binding</em>. (See {@link #spliterator()} for details.)
     *
     * @implSpec The default implementation creates a sequential {@code Stream} from the collection's {@code Spliterator}.
     *
     * @return a sequential {@code Stream} over the elements in this collection
     * @since 1.8
     */
    default Stream<S> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns an array containing all of the elements in this set. If this set makes any guarantees as to what order its
     * elements are returned by its iterator, this method must return the elements in the same order.
     *
     * <p>
     * The returned array will be "safe" in that no references to it are maintained by this set. (In other words, this
     * method must allocate a new array even if this set is backed by an array). The caller is thus free to modify the
     * returned array.
     *
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all the elements in this set
     */
    Object[] toArray();

    /**
     * Returns an array containing all of the elements in this set; the runtime type of the returned array is that of the
     * specified array. If the set fits in the specified array, it is returned therein. Otherwise, a new array is allocated
     * with the runtime type of the specified array and the size of this set.
     *
     * <p>
     * If this set fits in the specified array with room to spare (i.e., the array has more elements than this set), the
     * element in the array immediately following the end of the set is set to {@code null}. (This is useful in determining
     * the length of this set <i>only</i> if the caller knows that this set does not contain any null elements.)
     *
     * <p>
     * If this set makes any guarantees as to what order its elements are returned by its iterator, this method must return
     * the elements in the same order.
     *
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs.
     * Further, this method allows precise control over the runtime type of the output array, and may, under certain
     * circumstances, be used to save allocation costs.
     *
     * <p>
     * Suppose {@code x} is a set known to contain only strings. The following code can be used to dump the set into a newly
     * allocated array of {@code String}:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that {@code toArray(new Object[0])} is identical in function to {@code toArray()}.
     *
     * @param <T>
     *            t
     * @param a
     *            the array into which the elements of this set are to be stored, if it is big enough; otherwise, a new
     *            array of the same runtime type is allocated for this purpose.
     * @return an array containing all the elements in this set
     * @throws ArrayStoreException
     *             if the runtime type of the specified array is not a supertype of the runtime type of every element in
     *             this set
     * @throws NullPointerException
     *             if the specified array is null
     */
    <T> T[] toArray(T[] a);

    /**
     * Returns an immutable list an instance for each service.
     * 
     * @return an immutable list an instance for each service
     */
    List<S> toList();
}

//Extends Iterable???
//Skal have providers
//Skal have descriptors
//Skal have instancer
