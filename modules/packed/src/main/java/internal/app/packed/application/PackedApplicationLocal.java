/*
 * Copyright (c) 2026 Kasper Nielsen.
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
package internal.app.packed.application;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import app.packed.application.ApplicationConfiguration;
import app.packed.application.ApplicationHandle;
import app.packed.application.ApplicationLocal;
import app.packed.application.ApplicationMirror;
import app.packed.assembly.Assembly;
import app.packed.container.ContainerBuildLocal;
import app.packed.container.Wirelet;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.build.PackedBuildLocal;
import internal.app.packed.container.PackedContainerBuildLocal;

/** Implementation of {@link ApplicationLocal}. */
public final class PackedApplicationLocal<T> extends PackedBuildLocal<ApplicationLocal.Accessor, T> implements ApplicationLocal<T> {

    /**
     * @param initialValueSupplier
     */
    protected PackedApplicationLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected BuildLocalSource extract(ApplicationLocal.Accessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case ApplicationConfiguration a -> ApplicationSetup.crack(a);
        case ApplicationMirror a -> ApplicationSetup.crack(a);
        case ApplicationHandle<?, ?> a -> ApplicationSetup.crack(a);
        case Assembly _ -> throw new UnsupportedOperationException();
        case ContainerBuildLocal.Accessor b -> PackedContainerBuildLocal.crack(b).application;
        };
    }
}
