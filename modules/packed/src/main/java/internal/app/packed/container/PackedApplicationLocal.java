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
package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationLocalAccessor;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.component.PackedComponentLocal;
import internal.app.packed.component.PackedLocalMap.KeyAndLocalMapSource;

/**
 *
 */
public final class PackedApplicationLocal<T> extends PackedComponentLocal<ApplicationLocalAccessor, T> implements ApplicationLocal<T> {

    /**
     * @param initialValueSupplier
     */
    protected PackedApplicationLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected KeyAndLocalMapSource extract(ApplicationLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return ApplicationSetup.crack(accessor);
    }
}
