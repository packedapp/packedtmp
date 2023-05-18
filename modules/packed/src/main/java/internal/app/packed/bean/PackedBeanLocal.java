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
package internal.app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.extension.BeanLocal;
import app.packed.util.Nullable;
import internal.app.packed.container.PackedLocal;

/** Implementation of {@link BeanLocal}. */
public final class PackedBeanLocal<T> extends BeanLocal<T> implements PackedLocal<T> {

    /** An optional supplier that can provide initial values for a bean local. */
    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedBeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote this method is public, because we use it internally.
     */
    @Override
    public T get(BeanSetup bean) {
        return bean.container.application.localGet(this, bean);
    }

    /** {@inheritDoc} */
    @Override
    public @Nullable Supplier<? extends T> initialValueSupplier() {
        return initialValueSupplier;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSet(BeanSetup bean) {
        return bean.container.application.localIsSet(this, bean);
    }

    @Override
    public @Nullable T orElse(BeanSetup bean, T other) {
        return bean.container.application.localOrElse(this, bean, other);
    }

    protected <X extends Throwable> T orElseThrow(BeanSetup bean, Supplier<? extends X> exceptionSupplier) throws X {
        return bean.container.application.localOrElseThrow(this, bean, exceptionSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public void set(BeanSetup bean, T value) {
        requireNonNull(bean, "bean is null");
        bean.container.application.localSet(this, bean, value);
    }

    public static <T> PackedBeanLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedBeanLocal<>(initialValueSupplier);
    }
}
