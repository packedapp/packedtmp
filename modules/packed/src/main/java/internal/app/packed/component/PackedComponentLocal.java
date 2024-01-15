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

    /** {@inheritDoc} */
    public T get2(A accessor) {
        PackedLocalMap plm = extract(accessor);
        return plm.get(this, plm);
    }

    protected abstract PackedLocalMap extract(A accessor);
}
