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

/** Implementation of {@link BeanLocal}. */
public final class PackedBeanLocal<T> extends BeanLocal<T> {

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
    @SuppressWarnings("unchecked")
    public T get(BeanSetup bean) {
        if (initialValueSupplier == null) {
            return (T) bean.container.application.beanLocals.get(toKey(bean));
        } else {
            return (T) bean.container.application.beanLocals.computeIfAbsent(toKey(bean), e -> e.beanLocal.initialValueSupplier.get());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanSetup setup) {
        return setup.container.application.beanLocals.containsKey(toKey(setup));
    }

    /** {@inheritDoc} */
    @Override
    public void set(BeanSetup bean, T value) {
        requireNonNull(bean);
        requireNonNull(value);
        bean.container.application.checkWriteToLocals().beanLocals.put(toKey(bean), value);
    }

    /**
     * {@return a new bean local key}
     *
     * @param bean
     *            the bean to create the key for
     */
    BeanLocalKey toKey(BeanSetup bean) {
        return new BeanLocalKey(bean, this);
    }

    public static <T> PackedBeanLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedBeanLocal<>(initialValueSupplier);
    }

    /**
     * For space efficiency reasons we store all bean locals in a single map per-application using an instance of this class
     * as the key.
     *
     * @see internal.app.packed.container.ApplicationSetup#beanLocals
     */
    public /* primitive */ record BeanLocalKey(BeanSetup bean, PackedBeanLocal<?> beanLocal) {}
}
