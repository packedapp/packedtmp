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
package internal.app.packed.build;

import java.util.function.Supplier;

import app.packed.build.BuildLocal;
import app.packed.util.Nullable;
import internal.app.packed.build.PackedLocalMap.KeyAndLocalMapSource;

/** The base class for component locals. */
public abstract non-sealed class PackedBuildLocal<A, T> implements BuildLocal<A, T> /*permits PackedApplicationLocal, PackedContainerLocal, PackedBeanLocal */ {

    /** An optional supplier that can provide initial values for a bean local. */
    final @Nullable Supplier<? extends T> initialValueSupplier;

    protected PackedBuildLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    protected abstract KeyAndLocalMapSource extract(A accessor);

    /** {@inheritDoc} */
    @Override
    public final T get(A accessor) {
        KeyAndLocalMapSource kas = extract(accessor);
        return kas.locals().get(this, kas);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isBound(A accessor) {
        KeyAndLocalMapSource bean = extract(accessor);
        return bean.locals().isBound(this, bean);
    }

    /** {@inheritDoc} */
    @Override
    public final T orElse(A accessor, T other) {
        KeyAndLocalMapSource bean = extract(accessor);
        return bean.locals().orElse(this, bean, other);
    }

    @Override
    public final <X extends Throwable> T orElseThrow(A accessor, Supplier<? extends X> exceptionSupplier) throws X {
        KeyAndLocalMapSource bean = extract(accessor);
        return bean.locals().orElseThrow(this, bean, exceptionSupplier);
    }

    // I think these are nice. We can use use for transformers. Add something for pre-transform.
    // Remove them for post, no need to keep them around
    @Override
    public final T remove(A accessor) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public final void set(A accessor, T value) {
        KeyAndLocalMapSource bean = extract(accessor);
        bean.locals().set(this, bean, value);
    }
}
