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

import app.packed.container.ContainerBuildLocal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHandle;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.container.wirelets.InternalBuildWirelet;

/** Implementation of {@link ContainerLocal}. */
public final class PackedContainerBuildLocal<T> extends PackedBuildLocal<ContainerBuildLocal.Accessor, T> implements ContainerBuildLocal<T> {

    public PackedContainerBuildLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    protected BuildLocalSource extract(ContainerBuildLocal.Accessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return crack(accessor);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        requireNonNull(value, "value is null");
        final class ContainerSetLocalWirelet extends InternalBuildWirelet {

            @Override
            public void onBuild(PackedContainerInstaller installer) {
                installer.locals.put(PackedContainerBuildLocal.this, value);
            }
        }
        return new ContainerSetLocalWirelet();
    }


    public static ContainerSetup crack(ContainerBuildLocal.Accessor accessor) {
        return switch (accessor) {
        case ContainerConfiguration bc -> ContainerSetup.crack(bc);
        case ContainerHandle<?> bc -> ContainerSetup.crack(bc);
        case ContainerMirror bc -> ContainerSetup.crack(bc);
        };
    }
}
