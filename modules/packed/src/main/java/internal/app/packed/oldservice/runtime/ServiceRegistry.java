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
package internal.app.packed.oldservice.runtime;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.framework.Key;
import internal.app.packed.oldservice.InternalServiceUtil;
import internal.app.packed.oldservice.build.ServiceComposer;
import internal.app.packed.oldservice.sandbox.Service;

/**
 * A collection of {@link Service services}, with each service having a unique {@link Service#key() key}.
 * <p>
 * Packed provides a number of subinterfaces and abstract implementations of this interface:
 * <ul>
 * <li>{@link OldServiceLocator}, extends this interface with various method for obtaining service instances and service
 * providers.</li>
 * <li>{@link ServiceSelection}, a specialization of service locator, where each service shares a common super type. Is
 * commonly used for creating plugin-based systems.</li>
 * </ul>
 * <p>
 * Unless otherwise specified, implementations of this interface holds an unchangeable collection of services. One
 * notable exception is the {@link ServiceComposer} interface.
 * <p>
 * Implementations of this interface does not normally override hashCode/equals.
 * <p>
 * If this interface is used as an auto service. The registry will contain all services that available to a given
 * component instance. It will not include auto services.
 */
//@BeanVariable.AnnotatedWithHook(extension = BeanExtension.class)
public interface ServiceRegistry extends Iterable<Service> {
    
    /**
     * Returns a map view of every entry (key-service pair) in this registry in no particular order.
     * <p>
     * If this registry supports removals, the returned map will also support removal operations: {@link Map#clear()},
     * {@link Map#remove(Object)}, and {@link Map#remove(Object, Object)} and via views on {@link Map#keySet()},
     * {@link Map#values()} and {@link Map#entrySet()}. The returned map never supports insertion or update operations.
     * <p>
     * The returned map will retain any thread-safety guarantees provided by the registry itself.
     * 
     * @return a map view of every entry in this registry in no particular order
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

    /** {@return true if this registry contains any services, otherwise false} */
    default boolean isEmpty() {
        return asMap().isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<Service> iterator() {
        return asMap().values().iterator();
    }

    /**
     * Returns a set view containing the keys for every service in this registry.
     * <p>
     * If this registry supports removals, the returned set will also support removal operations: {@link Set#clear()},
     * {@link Set#remove(Object)}, {@link Set#removeAll(java.util.Collection)},
     * {@link Set#removeIf(java.util.function.Predicate)} and {@link Set#retainAll(java.util.Collection)}. or via any set
     * iterators. The returned map will never support insertion or update operations.
     * <p>
     * The returned map will retain any thread-safety guarantees provided by the registry itself.
     * 
     * @return a set view containing the keys for every service in this registry
     */
    default Set<Key<?>> keys() {
        return asMap().keySet();
    }

    /** {@return a unordered {@code Stream} of all services in this registry} */
    default Stream<Service> services() {
        return asMap().values().stream();
    }

    /** { @return the number of services in this registry} */
    default int size() {
        return asMap().size();
    }

    /** {@inheritDoc} */
    @Override
    default Spliterator<Service> spliterator() {
        return asMap().values().spliterator();
    }

    /** {@return an immutable service registry containing no services} */
    static ServiceRegistry of() {
        return InternalServiceUtil.EMPTY;
    }
}
