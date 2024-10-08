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

import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.binding.Provider;

/**
 * A specialization of {@link ServiceLocator} where all service instances have a common super type {@code <S>}.
 * <p>
 * Instances of this interface are normally created via the various select methods on ServiceLocator.
 *
 * @see ServiceLocator#selectAll()
 * @see ServiceLocator#selectAssignableTo(Class)
 */
// Should it include scoped services?
public interface ServiceSelection<S> extends ServiceLocator {

    /**
     * Performs the specified action for each service in this selection.
     *
     * @param action
     *            the action to perform on each service instance
     */
    default void forEachInstance(Consumer<? super S> action) {
        requireNonNull(action, "action is null");
        instanceStream().forEach(action);
    }

    /**
     * Acquires a service instance for each service in this selection returning them as a stream
     *
     * @return a stream of all instances in the selection
     */
    Stream<S> instanceStream();

    Stream<Provider<S>> providerStream();
}