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
package app.packed.inject;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import app.packed.base.AttributedElementStream;
import app.packed.base.Key;
import packed.internal.inject.service.runtime.AbstractServiceRegistry;
import packed.internal.util.PackedAttributeHolderStream;

/**
 * A collection of services each having a unique {@link Service#key() key}.
 * <p>
 * Unlike {@link ServiceLocator} and {@link ServiceSelection} this interface does not contain methods to acquire actual
 * service instances.
 */
// Auto activating... Hvis man har den som parameter...// Use ServiceRegistry if you want information, use
public interface ServiceRegistry extends Iterable<Service> {

    /**
     * Returns whether or not this registry contains a service with the specified key.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /**
     * Returns whether or not this registry contains a service with the specified key.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        return find(key).isPresent();
    }

    /**
     * Finds and returns a service with the specified key if present. Otherwise return {@link Optional#empty()}.
     * 
     * @param key
     *            the key to find a service for
     * @return the service, or empty if no service with the specified key exist
     * @see #find(Key)
     */
    default Optional<Service> find(Class<?> key) {
        return find(Key.of(key));
    }

    /**
     * Finds and returns a service with the specified key if present. Otherwise return {@link Optional#empty()}.
     * 
     * @param key
     *            the key to find a service for
     * @return the service, or empty if no service with the specified key exist
     * @see #find(Class)
     */
    default Optional<Service> find(Key<?> key) {
        requireNonNull(key, "key is null");
        for (Service s : this) {
            if (s.key().equals(key)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns whether or this registry contains any services.
     * 
     * @return true if this registry contains at least 1 service, otherwise false
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<Service> iterator() {
        return services().iterator();
    }

    /**
     * Returns a set containing the keys of every service in this registry.
     * <p>
     * There are no guarantees on the mutability, serializability, or thread-safety of the {@code Set} returned.
     * 
     * @return a set containing the keys of every service in this registry
     */
    default Set<Key<?>> keys() {
        return services().map(e -> e.key()).collect(Collectors.toSet());
    }

    /**
     * Returns the number of services in this registry.
     * 
     * @return the number of services in this registry
     */
    default int size() {
        return keys().size();
    }

    /**
     * Returns a unordered {@code Stream} of all services in this registry.
     *
     * @return a unordered {@code Stream} of all services in this registry
     */
    // services???, instances()
    // ServiceRegistry services() -> services().services()
    // Giver god mening hvis vi har instances() i fx ServiceSelection
    default AttributedElementStream<Service> services() {
        return new PackedAttributeHolderStream<>(StreamSupport.stream(spliterator(), false));
    }

    /**
     * Returns a list of every service in this registry in any order.
     * <p>
     * There are no guarantees on the mutability, serializability, or thread-safety of the {@code List} returned.
     * 
     * @return a list of every service in this registry in any order
     */
    // Syntes vi returnere en immutable liste...
    default List<Service> toList() {
        return services().collect(Collectors.toList());
    }

    /**
     * Returns a map (indexed by its key) of every service in this registry in any order.
     * <p>
     * There are no guarantees on the mutability, serializability, or thread-safety of the {@code Map} returned.
     * 
     * @return a map of every service in this registry in no particular order
     */
    // er asMap() bedre??? Nej men kan jo ikke bare indseatte services...
    default Map<Key<?>, Service> toMap() {
        return services().collect(Collectors.toMap(s -> s.key(), s -> s));
    }

    /**
     * Returns an empty service registry.
     * 
     * @return an empty service registry.
     */
    static ServiceRegistry of() {
        return AbstractServiceRegistry.EMPTY;
    }
}
