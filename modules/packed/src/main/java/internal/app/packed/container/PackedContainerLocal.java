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

/** Implementation of {@link ContainerLocal}. */
// Tror foerst vi skal beslutte os om vi har initial value
public final class PackedContainerLocal<T> extends ContainerLocal<T> {

    /** The scope of this container local. */
    private final LocalScope scope;

    private PackedContainerLocal(LocalScope scope, @Nullable Supplier<? extends T> initialValueSupplier) {
        super(initialValueSupplier);
        this.scope = requireNonNull(scope);
    }

    /** {@inheritDoc} */
    @Override
    public T get(ContainerSetup container) {
        return locals(container).get(this, container);
    }

    public T get(NonRootContainerBuilder contianer) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(ContainerSetup container) {
        return locals(container).isSet(this, container);
    }

    public PackedLocalMap locals(ContainerSetup container) {
        ApplicationSetup application = container.application;
        return switch (scope) {
        case DEPLOYMENT -> application.deployment.locales;
        default -> application.locals;
        };
    }

    public void set(ContainerSetup container, T value) {
        requireNonNull(container);
        locals(container).set(this, container, value);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        final class ContainerSetLocalWirelet extends InternalBuildWirelet {

            @Override
            protected void onBuild(PackedContainerBuilder installer) {
                installer.locals.put(PackedContainerLocal.this, value);
            }
        }
        return new ContainerSetLocalWirelet();
    }

    public static <T> PackedContainerLocal<T> of(LocalScope scope) {
        return new PackedContainerLocal<>(scope, null);
    }

    public static <T> PackedContainerLocal<T> of(LocalScope scope, @Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedContainerLocal<>(scope, initialValueSupplier);
    }

    // ContainerBoundaryKind
    public enum LocalScope {
        APPLICATION, CONTAINER, CONTAINER_LIFETIME, DEPLOYMENT;
    }
}
