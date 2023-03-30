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

import java.util.Map;
import java.util.function.Supplier;

import app.packed.container.Wirelet;
import app.packed.extension.ContainerLocal;
import app.packed.util.Nullable;

/**
 *
 */
public final class PackedContainerLocal<T> extends ContainerLocal<T> {

    final Scope scope;

    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedContainerLocal(Scope scope, @Nullable Supplier<? extends T> initialValueSupplier) {
        this.scope = requireNonNull(scope);
        this.initialValueSupplier = initialValueSupplier;
    }

    public Map.Entry<PackedContainerLocal<?>, Object> keyOf(ContainerSetup container) {
        return Map.entry(this, switch (scope) {
        case APPLICATION -> container.application;
        case CONTAINER_LIFETIME -> container.lifetime;
        case CONTAINER -> container;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(ContainerSetup container) {
        if (initialValueSupplier == null) {
            return (T) container.application.containerLocals.get(keyOf(container));
        } else {
            return (T) container.application.containerLocals.computeIfAbsent(keyOf(container), k -> k.getKey().initialValueSupplier.get());
        }
    }

    @SuppressWarnings("unchecked")
    public T get(LeafContainerOrApplicationBuilder contianer) {
        if (initialValueSupplier == null) {
            return (T) contianer.locals.get(this);
        } else {
            return (T) contianer.locals.computeIfAbsent(this, e -> e.initialValueSupplier.get());
        }
    }

    @Override
    public boolean isPresent(ContainerSetup container) {
        return container.application.containerLocals.containsKey(keyOf(container));
    }

    public void set(ContainerSetup container, T value) {
        requireNonNull(container);
        container.application.containerLocals.put(keyOf(container), value);
    }

    public static <T> PackedContainerLocal<T> of(Scope scope) {
        return new PackedContainerLocal<>(scope, null);
    }

    public static <T> PackedContainerLocal<T> of(Scope scope, @Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedContainerLocal<>(scope, initialValueSupplier);
    }

    /** {@inheritDoc} */
    @Override
    public Wirelet wireletSetter(T value) {
        final class ContainerSetLocalWirelet extends InternalBuildWirelet {

            @Override
            protected void onInstall(PackedContainerBuilder installer) {
                installer.locals.put(PackedContainerLocal.this, value);
            }
        }
        return new ContainerSetLocalWirelet();
    }

    public enum Scope {
        CONTAINER, CONTAINER_LIFETIME, APPLICATION;
    }
}
