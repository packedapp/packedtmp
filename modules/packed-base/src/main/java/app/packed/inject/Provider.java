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

import java.util.stream.Stream;

import packed.internal.inject.ConstantProvider;

/**
 * A provider of instances.
 * 
 * @param <T>
 *            the type of instances that are provided
 */
// Previously this interface also contained information about where
// the instances came from. However, this information is now only
// available from InjectionContext

// We let people implement this in order to help with testing.
// For example, 
@FunctionalInterface // I don't know if we want this...
public interface Provider<T> {

    /**
     * Returns whether or not this provider is guaranteed to return the same instance on every invocation.
     * <p>
     * This method is always allowed to return false.
     * <p>
     * The default value is false.
     * 
     * @return true if this provider is guaranteed to return the same instance on every invocation. Otherwise false.
     */
    default boolean isConstant() {
        return false;
    }

    /**
     * Provides an instance of type {@code T}.
     *
     * @return the provided value
     * @throws RuntimeException
     *             if an exception is encountered while providing an instance
     */
    T provide();

    /**
     * Returns an infinite stream of instances.
     * 
     * @return an infinite stream of instances
     */
    default Stream<T> stream() {
        return Stream.generate(() -> provide());
    }

    /**
     * @param <T>
     *            the type of the specified instance
     * @param constant
     *            the constant
     * @return a new provider that provides the specified constant everytime
     */
    static <T> Provider<T> ofConstant(T constant) {
        return new ConstantProvider<>(constant);
    }
}

// default Provider<T> lazyConstant() {
//    if (isConstant()) {
//        return this;
//    }
//    // Create lazy sync provider...
//    throw new UnsupportedOperationException();
//}
