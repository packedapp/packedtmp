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

import app.packed.bean.BeanLocal;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerLocal;
import app.packed.container.ContainerLocalAccessor;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.util.Nullable;
import internal.app.packed.component.PackedComponentLocal;
import internal.app.packed.component.PackedLocalKeyAndSource;
import sandbox.extension.container.ContainerHandle;

/** Implementation of {@link ContainerLocal}. */
public abstract non-sealed class PackedAbstractContainerLocal<T> extends PackedComponentLocal<ContainerLocalAccessor, T> implements ContainerLocal<T> {

    private PackedAbstractContainerLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public BeanLocal<T> toBeanLocal() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletConditionalGetter(T expectedValue, Wirelet wirelet) {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        requireNonNull(value, "value is null");
        final class ContainerSetLocalWirelet extends InternalBuildWirelet {

            @Override
            protected void onBuild(PackedContainerBuilder installer) {
                installer.locals.put(PackedAbstractContainerLocal.this, value);
            }
        }
        return new ContainerSetLocalWirelet();
    }

    /** {@inheritDoc} */
    protected ContainerSetup extract0(ContainerLocalAccessor accessor) {
        requireNonNull(accessor, "accessor is null");
        return switch (accessor) {
        case ContainerConfiguration bc -> ContainerSetup.crack(bc);
        case ContainerHandle bc -> ContainerSetup.crack(bc);
        case ContainerMirror bc -> ContainerSetup.crack(bc);
        };
    }

    public static class PackedContainerLocal<T> extends PackedAbstractContainerLocal<T> {
        public PackedContainerLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
            super(initialValueSupplier);
        }

        /** {@inheritDoc} */
        @Override
        protected PackedLocalKeyAndSource extract(ContainerLocalAccessor accessor) {
            return extract0(accessor);
        }
    }

    public static class PackedAssemblyLocal<T> extends PackedAbstractContainerLocal<T> {
        public PackedAssemblyLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
            super(initialValueSupplier);
        }

        /** {@inheritDoc} */
        @Override
        protected PackedLocalKeyAndSource extract(ContainerLocalAccessor accessor) {
            return extract0(accessor).assembly;
        }
    }

    public static class PackedApplicationLocal<T> extends PackedAbstractContainerLocal<T> {
        public PackedApplicationLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
            super(initialValueSupplier);
        }

        /** {@inheritDoc} */
        @Override
        protected PackedLocalKeyAndSource extract(ContainerLocalAccessor accessor) {
            return extract0(accessor).application;
        }
    }

}
