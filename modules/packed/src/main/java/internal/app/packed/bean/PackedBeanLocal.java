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
// TODO, don't think we should allow updates after the application has been assembled.
public final class PackedBeanLocal<T> extends BeanLocal<T> {

    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedBeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    public PairKey keyOf(BeanSetup bean) {
        return new PairKey(bean, this);
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked")
    public T get(BeanSetup bean) {
        if (initialValueSupplier == null) {
            return (T) bean.container.application.beanLocals.get(keyOf(bean));
        } else {
            return (T) bean.container.application.beanLocals.computeIfAbsent(keyOf(bean), e -> e.beanLocal.initialValueSupplier.get());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanSetup setup) {
        return setup.container.application.beanLocals.containsKey(keyOf(setup));
    }

    /** {@inheritDoc} */
    @Override
    public void set(BeanSetup bean, T value) {
        requireNonNull(value);
        // We don't have the extension so can't check
        // OMG, power users can actually use this as well.
        bean.container.application.beanLocals.put(keyOf(bean), value);
    }

    public static <T> PackedBeanLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedBeanLocal<>(initialValueSupplier);
    }

    /** We have a single bean local map for an application, this is the key in the map. */
    public record PairKey(BeanSetup bean, PackedBeanLocal<?> beanLocal) {}
}
