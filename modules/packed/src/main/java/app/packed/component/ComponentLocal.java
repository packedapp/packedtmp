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
import java.util.function.Supplier;

import app.packed.application.ApplicationLocal;
import app.packed.bean.BeanLocal;
import app.packed.container.ContainerLocal;
import internal.app.packed.component.PackedComponentLocal;

/**
 *
 * <p>
 * What about ISE do we support valid points you can use a local???
 *
 *
 * <p>
 * Component locals are only intended to be used by secure code. Hence there is no support for checking the values that
 * are stored with it.
 * <p>
 * Component locals are only intended to be used at build time. If you need to share state at runtime the right way to
 * do so is by installing a shared bean of some kind
 * <p>
 * We do not cache errors from suppliers... These are intended to not be handled at build-time.
 */

// Locals vs Wirelets, there is some overlap. Which have quite figured out

// Locals cannot have a mirror, wirelets would be able to have it

// AssemblyLocal -> But is an Assembly a component?

// Maybe BuildLocal instead of ComponentLocal???
public sealed interface ComponentLocal<A, T> permits PackedComponentLocal, ApplicationLocal, ContainerLocal, BeanLocal {

    /**
     * Returns the local value via the specified accessor.
     * <p>
     * If no value has been set for this local previously, this method will:
     * <ul>
     * <li>Initialize lazily, if an initial value supplier was specified when creating this local.</li>
     * <li>Fail with {@link java.util.NoSuchElementException}, if no initial value supplier was specified when creating this
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
    default void ifBound(A accessor, Consumer<? super T> action) {}

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

    <X extends Throwable> T orElseThrow(A accessor, Supplier<? extends X> exceptionSupplier) throws X;

    // I think these are nice. We can use use for transformers. Add something for pre-transform.
    // Remove them for post, no need to keep them around
    // What if initial value supplier?? Remove, and allow to reinitialze, or UnsupportedOperationException
    T remove(A accessor);

    /**
     * Sets the bound value of this local for the bean represented by the specified accessor.
     *
     * @param bean
     *            the bean
     * @param value
     *            the value to bind
     * @throws UnsupportedOperationException
     *             if an initial value supplier was specified when creating the local
     */
    // Or maybe we can set it. Initial value supplier is not very initial
    void set(A accessor, T value);

    // https://docs.oracle.com/en/java/javase/20/docs/api/jdk.incubator.concurrent/jdk/incubator/concurrent/ScopedValue.html#get()

}
