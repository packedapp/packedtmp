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

import app.packed.extension.ContainerLocal;
import app.packed.util.Nullable;

/**
 *
 */
public final class PackedContainerLocal<T> extends ContainerLocal<T> {

    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedContainerLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T get(ContainerSetup contianer) {
        if (initialValueSupplier == null) {
            return (T) contianer.locals.get(this);
        } else {
            return (T) contianer.locals.computeIfAbsent(this, e -> e.initialValueSupplier.get());
        }
    }

    @SuppressWarnings("unchecked")
    public T get(PackedContainerBuilder contianer) {
        if (initialValueSupplier == null) {
            return (T) contianer.locals.get(this);
        } else {
            return (T) contianer.locals.computeIfAbsent(this, e -> e.initialValueSupplier.get());
        }
    }

    @Override
    public boolean isPresent(ContainerSetup container) {
        return container.locals.containsKey(this);
    }

    public void set(ContainerSetup bean, T container) {
        requireNonNull(container);
        bean.locals.put(this, container);
    }

    public static <T> PackedContainerLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedContainerLocal<>(initialValueSupplier);
    }
}
