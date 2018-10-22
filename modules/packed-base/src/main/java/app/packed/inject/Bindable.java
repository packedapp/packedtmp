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

import java.util.List;

/**
 * A bindable object allows for the dynamic bindings of a set of dependencies.
 * 
 * An interface that allows an object to bound
 * 
 * object or factories that will
 */
public interface Bindable extends Dependant {

    default <T> Bindable bind(Class<T> type, T argument) {
        throw new UnsupportedOperationException();
    }

    default <T> Bindable bind(Key<T> key, T argument) {
        throw new UnsupportedOperationException();
    }

    /**
     * Binds the specified instance to the argument that matches the specified dependency. If an argument for the specified
     * dependency have already been bound, the existing binding is overridden.
     *
     * @param dependency
     *            the dependency for which to bind the matching argument
     * @param instance
     *            the instance to bind to the argument matching the dependency
     * @return this bindable
     * @throws IllegalArgumentException
     *             if the specified dependency is not a dependency of this object
     * @throws ClassCastException
     *             if the specified argument does not match the type of the dependency
     * @throws NullPointerException
     *             if the specified argument is null and the type of the dependency is not optional, or sa primitive type
     */
    Bindable bind(Dependency dependency, Object instance);

    Bindable bindLazy(Dependency dependency, Factory<?> factory);

    Bindable bindPrototype(Dependency dependency, Factory<?> factory);

    /**
     * Returns a list of dependencies that are bindable for this object. Unlike {@link #getDependencies()} this method
     * returns the same list of dependencies for the lifetime of this object. Unlike {@link #getDependencies()} which only
     * includes the parameters that have not already been bound.
     *
     * @return a list of dependencies that are bindable for this instance
     */
    List<Dependency> getBindableDependencies();

}
// * @param index
// * the index of the dependency
// * @param argument
// * the argument to bind to the dependency
// * @return this bindable
// * @throws IndexOutOfBoundsException
// * if the index is out of range ({@code index < 0 || index >= getNumberOfDependencies()})
// * @throws ClassCastException
// * if the specified argument does not match the type of the dependency
// * @throws NullPointerException
// * if the specified argument is null and the type of the dependency is a primitive type
