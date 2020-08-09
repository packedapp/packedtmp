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

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.base.Key;

/**
 * A set of services that may contain multiple services with the same key.
 */
// Skal have providers
// Skal have descriptors
// Skal have instancer

// For en plugin struktur. Skal vi havde adgang til hvilke modul der definere den

// ServiceCollection?
// Extends Iterable???

// Vi vil gerne paa en eller anden maade have Attributes, ServiceDescriptors, osv med...
// ServiceDescriptor -> Instance
public interface ServiceSet<T> {

    default void forEach(Consumer<? super T> action) {
        toList().forEach(action);
    }

    /**
     * Returns an immutable set of keys for each service in this set. If there are multiple services with the same key, only
     * one key instance will be present in the set.
     * 
     * @return an immutable set of keys for each service in this set
     */
    Set<Key<? extends T>> keys();

    /**
     * Returns the number of services in this set.
     * 
     * @return the number of services in this set
     */
    int size();

    /**
     * Returns an immutable list with all service instance.
     * 
     * @return an immutable list with all service instance
     */
    List<T> toList();
}
