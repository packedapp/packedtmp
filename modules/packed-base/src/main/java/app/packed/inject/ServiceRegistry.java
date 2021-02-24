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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.base.Key;
import app.packed.hooks.AutoService;
import packed.internal.inject.service.AbstractServiceRegistry;
import packed.internal.util.PackedAttributeHolderStream;

/**
 * A collection of services with each service having a unique {@link Service#key() key}. Unlike {@link ServiceLocator}
 * and {@link ServiceSelection} this interface does not contain any methods to acquire actual service instances. It
 * merely provides information about the type of services that are available.
 * <p>
 * Unless otherwise specified, implementations of this interface does not instances of this interface are immutable
 * collections. One notable exception is the {@link ServiceComposer} interface. Which support mutation operations on the
 * iterators returned by {@link #iterator()} and sets returned by {@link #keys()}. Kun remove operationer jo
 * <p>
 * Unless otherwise specified, implementations of this interface does not override hashCode/equals.
 * Hvorfor kan vi ikke definere den udfra asMap?
 * <p>
 * If used as an auto activating variable sidecar the registry injected will be an immutable
 */
@AutoService
public interface ServiceRegistry extends Iterable<Service> {

    /**
     * Returns a map view of every service in this registry in no particular order.
     * <p>
     * If this registry supports removals. The returned map will also support removals. However, the map will never support
     * insertions or updates.
     * <p>
     * The returned map will support any thread-safety guarantees provided by this registry instance.
     * 
     * @return a map of every service in this registry in no particular order
     */
    Map<Key<?>, Service> asMap();

    /**
     * Returns {@code true} if this registry contains a service with the specified key.
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
     * Returns {@code true} if this registry contains a service with the specified key.
     *
     * @param key
     *            key whose presence in this registry is to be tested
     * @return {@code true} if a service with the specified key is present in this registry. Otherwise {@code false}
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return asMap().containsKey(key);
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
        return Optional.ofNullable(asMap().get(key));
    }

    /** {@inheritDoc} */
    @Override
    default void forEach(Consumer<? super Service> action) {
        asMap().values().forEach(action);
    }

    /**
     * Returns whether or this registry contains any services.
     * 
     * @return true if this registry contains at least 1 service, otherwise false
     */
    default boolean isEmpty() {
        return asMap().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<Service> iterator() {
        return asMap().values().iterator();
    }

    /**
     * Returns a set containing the keys of every service in this registry.
     * <p>
     * There are no guarantees on the mutability, serializability, or thread-safety of the {@code Set} returned.
     * 
     * @return a set containing the keys of every service in this registry
     */
    default Set<Key<?>> keys() {
        return asMap().keySet();
    }

    /**
     * Returns a unordered {@code Stream} of all services in this registry.
     *
     * @return a unordered {@code Stream} of all services in this registry
     */
    default Stream<Service> services() {
        return new PackedAttributeHolderStream<>(asMap().values().stream());
    }

    /**
     * Returns the number of services in this registry.
     * 
     * @return the number of services in this registry
     */
    default int size() {
        return asMap().size();
    }

    /** {@inheritDoc} */
    @Override
    default Spliterator<Service> spliterator() {
        return asMap().values().spliterator();
    }

    /**
     * Returns an empty immutable service registry.
     * 
     * @return an empty immutable service registry.
     */
    static ServiceRegistry of() {
        return AbstractServiceRegistry.EMPTY;
    }
}
// --------- NOTES --------
//
// extends AttributeHolder?
