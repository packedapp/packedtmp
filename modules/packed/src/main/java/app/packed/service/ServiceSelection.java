/*
 * Copyright (c) 2026 Kasper Nielsen.
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
import java.util.stream.Stream;

import app.packed.binding.Provider;

/**
 * A specialization of {@link ServiceLocator} where all service instances have a common super type {@code <S>}.
 * <p>
 * Instances of this interface are normally created via the various select methods on ServiceLocator.
 * <P>
 *
 * @see ServiceLocator#selectAll()
 * @see ServiceLocator#selectAssignableTo(Class)
 */
// Should it include scoped services?
public interface ServiceSelection<S> extends ServiceLocator, Iterable<S> {

    /**
     * Acquires a service instance for each service in this selection returning them as a stream
     *
     * @return a stream of all instances in the selection
     */
    Stream<S> stream();

    Stream<Provider<S>> streamOfProviders();

    List<S> toList();
    // Problemet er lidt inject af List<SomeService>
    // Hvis de alle har forskellige quarlifiers
    //
}