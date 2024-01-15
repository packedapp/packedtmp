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
package app.packed.component;

import java.util.function.Consumer;

/**
 *
 */
public interface ComponentLocal<A, T> {

    /**
     * Returns the local value from the specified accessor.
     * <p>
     * If no value has been set for this local previously, this method will:
     * <ul>
     * <li>Initialize lazily, if an initial value supplier was used when creating this local.</li>
     * <li>Fail with {@link java.util.NoSuchElementException}, if no initial value supplier was used when creating this
     * local.</li>
     * </ul>
     *
     * @param accessor
     *            the local accessor
     * @return the value of the local
     *
     * @throws java.util.NoSuchElementException
     *             if a value has not been set previously for this local and an initial value supplier was not specified
     *             when creating the local
     */
    T get(A accessor);

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if a value is present
     * @throws NullPointerException
     *             if value is present and the given action is {@code null}
     */
    default void ifPresent(A accessor, Consumer<? super T> action) {}

    /**
     * Returns whether or not a value has been bound to this local for the component represented by the specified accessor.
     *
     * Was: Returns whether or not a value has been set or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the component local accessor
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    boolean isBound(A accessor);

    /**
     * Returns whether or not a value has been bound or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @param other
     *            the value to return if a value has not been bound previously
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    T orElse(A accessor, T other);
}
