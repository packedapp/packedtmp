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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import app.packed.base.AttributedElementStream;
import app.packed.base.Key;

/**
 * An immutable set of services with unique {@link Service#key() keys}. Unlike {@link ServiceLocator} this interface
 * does not contain any methods to acquire actual service instances.
 */
public interface ServiceRegistry extends Iterable<Service> {

    /**
     * Returns a service contract for this set. The returned service contract will contain all the keys of this set via
     * {@link ServiceContract#provides()}.
     * 
     * @return the service contract contained the services this set provides
     */
    default ServiceContract contract() {
        return ServiceContract.newContract(c -> stream().forEach(s -> c.provides(s.key())));
    }

    /**
     * Finds and returns a service with the specified key if present. Otherwise return {@link Optional#empty()}.
     * 
     * @param key
     *            the key to find a service for
     * @return the service, or empty if no service with the specified key exist
     * @see #findService(Key)
     */
    default Optional<Service> findService(Class<?> key) {
        return findService(Key.of(key));
    }

    /**
     * Finds and returns a service with the specified key if present. Otherwise return {@link Optional#empty()}.
     * 
     * @param key
     *            the key to find a service for
     * @return the service, or empty if no service with the specified key exist
     * @see #findService(Class)
     */
    default Optional<Service> findService(Key<?> key) {
        requireNonNull(key, "key is null");
        return stream().filter(d -> d.key().equals(key)).findFirst();
    }

    /**
     * Returns whether or this registry contains any services.
     * 
     * @return true if this registry contains at least 1 service, otherwise false
     */
    default boolean isEmpty() {
        return keys().isEmpty();
    }

    /**
     * Returns whether or not a service with the specified key is present in this registry.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #isPresent(Key)
     */
    default boolean isPresent(Class<?> key) {
        return isPresent(Key.of(key));
    }

    /**
     * Returns whether or not a service with the specified key is present in this registry.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #isPresent(Class)
     */
    default boolean isPresent(Key<?> key) {
        return keys().contains(key);
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<Service> iterator() {
        return stream().iterator();
    }

    /**
     * Returns a set containing the keys of every service in this registry.
     * 
     * @return a set containing the keys of every service in this registry
     */
    default Set<Key<?>> keys() {
        return stream().map(e -> e.key()).collect(Collectors.toSet());
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
    AttributedElementStream<Service> stream(); // Rename to services????

    /**
     * Returns a list of every service in this registry.
     * 
     * @return a list of every service in this registry
     */
    default List<Service> toList() {
        return stream().collect(Collectors.toList());
    }
}
