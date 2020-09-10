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

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import app.packed.base.Key;

/** An immutable set of services. Where each service has an unique {@link Service#key()}. */
public interface ServiceMap extends Iterable<Service> {

    /**
     * Returns true if the system contains a service with the specified key.
     *
     * @param key
     *            the key of the service
     * @return true if a service with the specified key exists.
     * @see #contains(Key)
     */
    default boolean contains(Class<?> key) {
        return contains(Key.of(key));
    }

    /**
     * Returns {@code true} if a service with the specified key exists. Otherwise {@code false}.
     *
     * @param key
     *            the type of service
     * @return true if a service with the specified key exists. Otherwise false.
     * @see #contains(Class)
     */
    default boolean contains(Key<?> key) {
        return keys().contains(key);
    }

    /**
     * Returns the service contract for this system. The contract will only have {@link ServiceContract#provides()} filled
     * out.
     * 
     * @return the service contract of this injector
     */
    default ServiceContract contract() {
        return ServiceContract.newContract(c -> stream().forEach(s -> c.provides(s.key())));
    }

    default Optional<Service> findService(Class<?> key) {
        return findService(Key.of(key));
    }

    // Problemet er her navngivning
    // Vi vil gerne have service(descriptor), instance, og provider
    // So findService, findProvider, find

    default Optional<Service> findService(Key<?> key) {
        requireNonNull(key, "key is null");
        return stream().filter(d -> d.key().equals(key)).findFirst();
    }

    /** {@inheritDoc} */
    @Override
    default Iterator<Service> iterator() {
        return stream().iterator();
    }

    /**
     * Returns a set of all unique keys in this system
     * 
     * @return a set of all unique keys in this system
     */
    default Set<Key<?>> keys() {
        return stream().map(e -> e.key()).collect(Collectors.toSet());
    }

    /**
     * Returns a unordered {@code Stream} of all services contained in this system.
     *
     * @return a unordered {@code Stream} of all services contained in this system
     */
    Stream<Service> stream();
}

// Map<Key<?>, Service> services()

////IDK
//default Map<Key<?>, Service> toServiceMap() {
//  return stream().collect(Collectors.toMap(e -> e.key(), e -> e));
//}

// contains->containsService??? Syntes jeg ikke. findService er kun fordi vi har find(instance)

//An important questions are keys unique????
//They have to be if we have findService, as it cannot return more than 1...
