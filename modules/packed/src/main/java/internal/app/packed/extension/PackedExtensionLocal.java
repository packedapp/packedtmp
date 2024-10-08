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
package internal.app.packed.extension;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.container.ContainerBuildLocal.Accessor;
import app.packed.util.Nullable;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.container.PackedContainerBuildLocal;

/** Implementation of {@link ContainerLocal}. */
//HMMM
public final class PackedExtensionLocal<T> extends PackedBuildLocal<Accessor, T> {

    public PackedExtensionLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    protected BuildLocalSource extract(Accessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return PackedContainerBuildLocal.crack(accessor);
    }

//    /** {@inheritDoc} */
//    @Override
//    public Wirelet wireletSetter(T value) {
//        requireNonNull(value, "value is null");
//        final class ContainerSetLocalWirelet extends InternalBuildWirelet {
//
//            @Override
//            protected void onBuild(PackedContainerBuilder installer) {
//                installer.locals.put(PackedExtensionLocal.this, value);
//            }
//        }
//        return new ContainerSetLocalWirelet();
//    }


}
