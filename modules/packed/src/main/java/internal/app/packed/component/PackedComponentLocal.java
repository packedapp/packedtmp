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
package internal.app.packed.component;

import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.component.ComponentLocal;
import app.packed.util.Nullable;

/**
 * The base class for container and bean locals.
 */
//BuildLocal?
public abstract non-sealed class PackedComponentLocal<A, T> implements ComponentLocal<A, T> {

    /** An optional supplier that can provide initial values for a bean local. */
    final @Nullable Supplier<? extends T> initialValueSupplier;

    protected PackedComponentLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action
     *            the action to be performed, if a value is present
     * @throws NullPointerException
     *             if value is present and the given action is {@code null}
     */
    @Override
    public void ifBound(A accessor, Consumer<? super T> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns whether or not a value has been bound to this local for the bean represented by the specified accessor.
     *
     * Was: Returns whether or not a value has been set or previously initialized in the specified accessor.
     *
     * @param accessor
     *            the bean local accessor
     * @return true if a value has been set or initialized, otherwise false
     *
     * @apiNote Calling this method will <strong>never</strong> initialize the value of the local even if a initial value
     *          supplier was specified when creating the local. As such this method rarely makes sense to call if an initial
     *          value supplier was specified when creating the local.
     */
    @Override
    public boolean isBound(A accessor) {
        PackedLocalKeyAndSource bean = extract(accessor);
        return bean.locals().isBound(this, bean);
    }

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
    @Override
    public T orElse(A accessor, T other) {
        PackedLocalKeyAndSource bean = extract(accessor);
        return bean.locals().orElse(this, bean, other);
    }

    @Override
    public <X extends Throwable> T orElseThrow(A accessor, Supplier<? extends X> exceptionSupplier) throws X {
        PackedLocalKeyAndSource bean = extract(accessor);
        return bean.locals().orElseThrow(this, bean, exceptionSupplier);
    }

    // I think these are nice. We can use use for transformers. Add something for pre-transform.
    // Remove them for post, no need to keep them around
    @Override
    public T remove(A accessor) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the bound value of this local for the bean represented by the specified accessor.
     *
     * @param bean
     *            the bean
     * @param value
     *            the value to bind
     */
    @Override
    public void set(A accessor, T value) {
        PackedLocalKeyAndSource bean = extract(accessor);
        bean.locals().set(this, bean, value);
    }

    /** {@inheritDoc} */
    @Override
    public T get(A accessor) {
        PackedLocalKeyAndSource kas = extract(accessor);
        return kas.locals().get(this, kas);
    }

    protected abstract PackedLocalKeyAndSource extract(A accessor);
}
