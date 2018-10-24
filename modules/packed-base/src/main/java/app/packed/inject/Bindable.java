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

/**
 * A bindable object allows for the dynamic bindings of a set of dependencies.
 * 
 * An interface that allows an object to bound
 * 
 * object or factories that will
 * 
 * Injector#bind (im afraid people will forget this.
 */
public interface Bindable extends Dependant {

    /**
     * Binds the specified instances to every dependency with an <b>exact</b> matching type (including any qualifier
     * annotations). Invoking this method is equivalent to:
     * 
     * <pre> {@code 
     *  Key<T> key = Key.of(type);
     *  for (Dependency d : getDependencies()) {
     *    if (d.getKey().equals(key)) {
     *      bind(d, instance);
     *    }
     *  }
     *  return this;}
     * </pre>
     * <p>
     * If there are no matching dependencies invoking this method has no effect.
     * 
     * @param type
     *            the type for which to bind every matching dependency
     * @param instance
     *            the instance to bind
     * @return this bindable
     * @throws NullPointerException
     *             if the specified argument is null and the type of the dependency is not optional, or a primitive type
     */
    default <T> Bindable bind(Class<T> type, T instance) {
        return bind(Key.of(type), instance);
    }

    /**
     * Binds the specified instances to every dependency with an <b>exact</b> matching key. Invoking this method is
     * equivalent to:
     * 
     * <pre> {@code 
     *  for (Dependency d : getDependencies()) {
     *    if (d.getKey().equals(key)) {
     *      bind(d, instance);
     *    }
     *  }
     *  return this;}
     * </pre>
     * <p>
     * If there are no matching dependencies invoking this method has no effect.
     * 
     * @param key
     *            the key for which to bind every matching dependency
     * @param instance
     *            the instance to bind
     * @return this bindable
     * @throws NullPointerException
     *             if the specified argument is null and the type of the dependency is not optional, or a primitive type
     */
    default <T> Bindable bind(Key<T> key, T instance) {
        for (Dependency d : getDependencies()) {
            if (d.getKey().equals(key)) {
                bind(d, instance);
            }
        }
        return this;
    }

    /**
     * Binds the specified instance to the argument that matches the specified dependency. If an argument for the specified
     * dependency have already been bound, the existing binding is overridden.
     * <p>
     * Once the dependency has been bound, it will no longer appear in the list of dependencies returned by
     * {@link #getDependencies()}.
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
     *             if the specified argument is null and the type of the dependency is is not optional, or a primitive type
     */
    Bindable bind(Dependency dependency, Object instance);

    // Does not support binding factories with dependencies??????
    // Jo det kan man godt, de bliver instantierede som newInstance()....
    // Vi skal checke access her.

    // Hvad med Provider?????Taenker vi ogsaa kunne bruge saadan en.

    default Bindable bindLazy(Dependency dependency, Provider<?> factory) {
        //Tag et factory, saa vi kan verificere typen????
        return this;
    }

    default Bindable bindPrototype(Dependency dependency, Provider<?> factory) {
        return this;
    }

    Bindable bindLazy(Dependency dependency, Factory<?> factory);

    /**
     * Binds the specified factory to the argument that matches the specified dependency. If an argument for the specified
     * dependency have already been bound, the existing binding is overridden. Every time this object needs to access is
     * created???
     * <p>
     * Injector#newInstance()
     * <p>
     * Once the dependency has been bound, it will no longer appear in the list of dependencies returned by
     * {@link #getDependencies()}.
     * 
     * @param dependency
     *            the dependency for which to bind the matching argument
     * @param factory
     *            the factory to bind to the argument matching the dependency
     * @return this bindable
     * @throws IllegalArgumentException
     *             if the specified dependency is not a dependency of this object
     * @throws ClassCastException
     *             if the specified argument does not match the type of the dependency
     * @throws NullPointerException
     *             if the specified argument is null and the type of the dependency is is not optional, or a primitive type
     */
    Bindable bindPrototype(Dependency dependency, Factory<?> factory);
    //
    // /**
    // * Returns a list of dependencies that are bindable for this object. Unlike {@link #getDependencies()} this method
    // * returns the same list of dependencies for the lifetime of this object. {@link #getDependencies()} only includes the
    // * parameters that have not already been bound.
    // *
    // * @return a list of dependencies that are bindable for this instance
    // */
    // List<Dependency> getBindableDependencies();
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
