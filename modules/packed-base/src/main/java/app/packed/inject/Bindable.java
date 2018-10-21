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
 * This interface allows for the binding of arguments to
 */
// Can we inject Injector?? I think we need to distinguish between injection and Bindable.
// This is more lightWeight
public interface Bindable extends Dependant {

    /**
     * Binds the specified dependency to the argument. to the dependency with the specified index. Any existing binding will
     * be overridden.
     *
     * @param index
     *            the index of the dependency
     * @param argument
     *            the argument to bind to the dependency
     * @return this bindable
     * @throws IndexOutOfBoundsException
     *             if the index is out of range ({@code index < 0 || index >= getNumberOfDependencies()})
     * @throws ClassCastException
     *             if the specified argument does not match the type of the dependency
     * @throws NullPointerException
     *             if the specified argument is null and the type of the dependency is a primitive type
     */
    Bindable bind(Dependency dependency, Object instance);

    Bindable bindLazy(Dependency dependency, Factory<?> factory);

    Bindable bindPrototype(Dependency dependency, Factory<?> factory);

    /**
     * Returns a list of dependencies that are bindable for this instance. Unlike {@link #getDependencies()} this method
     * returns the same list of dependencies for the lifetime of the bindable. Unlike {@link #getDependencies()} which only
     * includes the parameters that have not already been bound.
     *
     * @return a list of dependencies that are bindable for this instance
     */
    List<Dependency> getBindableDependencies();

}
