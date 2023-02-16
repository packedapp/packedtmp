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

/**
 * Implementation of {@link BeanLocal}. Internally we use this to allow querying using BeanSetup instance.
 */
public final class PackedBeanLocal<T> extends BeanLocal<T> {

    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedBeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(BeanSetup bean) {
        if (initialValueSupplier == null) {
            return (T) bean.locals.get(this);
        } else {
            return (T) bean.locals.computeIfAbsent(this, e -> e.initialValueSupplier.get());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanSetup setup) {
        // Cannot come up with any situations where you want to call isPresent
        // and at the same time have an initial value supplier
        if (initialValueSupplier != null) {
            throw new UnsupportedOperationException("isPresent is not supported for bean locals that have been created with a initial-value supplier");
        }
        return setup.locals.containsKey(this);
    }

    /** {@inheritDoc} */
    @Override
    public void set(BeanSetup bean, T value) {
        requireNonNull(value);
        bean.locals.put(this, value);
    }

    public static <T> PackedBeanLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedBeanLocal<>(initialValueSupplier);
    }
}
