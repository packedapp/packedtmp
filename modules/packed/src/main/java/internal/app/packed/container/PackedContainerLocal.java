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

import app.packed.container.ContainerLocal;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.build.PackedLocalMap.KeyAndLocalMapSource;

/** Implementation of {@link ContainerLocal}. */
public final class PackedContainerLocal<T> extends PackedBuildLocal<ContainerLocal.ContainerLocalAccessor, T> implements ContainerLocal<T> {

    public PackedContainerLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    protected KeyAndLocalMapSource extract(ContainerLocal.ContainerLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return ContainerSetup.crack(accessor);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        requireNonNull(value, "value is null");
        final class ContainerSetLocalWirelet extends InternalBuildWirelet {

            @Override
            protected void onBuild(PackedContainerInstaller installer) {
                installer.locals.put(PackedContainerLocal.this, value);
            }
        }
        return new ContainerSetLocalWirelet();
    }
}
