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
package app.packed.bindings;

import static java.util.Objects.requireNonNull;

import java.util.stream.Stream;

/**
 * A provider of values.
 *
 * @param <T>
 *            the type of instances that are provided
 */
@FunctionalInterface
public interface Provider<T> {

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided instance
     */
    T provide();

    /**
     * Returns an infinite stream of instances.
     *
     * @return an infinite stream of instances
     */
    // Cannot come up with a single use case for this method
    default Stream<T> stream() {
        return Stream.generate(() -> provide());
    }

    /**
     * Returns a provider that will be provide the specified instance for every invocation of {@link #provide()}.
     *
     * @param <T>
     *            the type of the provider
     * @param instance
     *            the instance
     * @return a new provider that provides the specified instance
     */
    // or ofConstant? or just of?
    static <T> Provider<T> ofInstance(T instance) {
        requireNonNull(instance, "instance is null");

        record InstanceProvider<T>(T provide) implements Provider<T> {}
        return new InstanceProvider<>(instance);
    }
}

///**
// * Returns whether or not this provider is guaranteed to return the same instance on every invocation.
// * <p>
// * This method is always allowed to return false.
// * <p>
// * The default value is false.
// *
// * @return true if this provider is guaranteed to return the same instance on every invocation. Otherwise false.
// */
//// I'm not sure if we want this...
//default boolean isConstant() {
//    return false;
//}

// Provider's can also be obtained for non-services.

//Previously this interface also contained information about where
//the instances came from. However, this information is now only
//available from InjectionContext or ServiceProvider.. ServiceProvider
//To avoid storing all this static information if we don't need to
//We let people implement this in order to help with testing.
