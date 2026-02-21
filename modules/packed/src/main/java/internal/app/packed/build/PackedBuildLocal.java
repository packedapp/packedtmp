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
package internal.app.packed.build;

import static java.util.Objects.requireNonNull;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

import app.packed.build.BuildLocal;
import internal.app.packed.application.PackedApplicationLocal;
import internal.app.packed.bean.PackedBeanBuildLocal;
import internal.app.packed.build.BuildLocalMap.BuildLocalSource;
import internal.app.packed.container.PackedContainerBuildLocal;
import internal.app.packed.extension.PackedExtensionLocal;

/**
 * The base class for component locals.
 * @implNote this class should be a value class as we rely on the identity of it
 */
public abstract sealed class PackedBuildLocal<A, T>
        permits PackedApplicationLocal, PackedContainerBuildLocal, PackedBeanBuildLocal, PackedExtensionLocal {

    /** An optional supplier that can provide initial values for a bean local. */
    final @Nullable Supplier<? extends T> initialValueSupplier;

    protected PackedBuildLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    protected abstract BuildLocalSource extract(A accessor);

    /** {@inheritDoc} */
    public final T get(A accessor) {
        BuildLocalSource kas = extract(accessor);
        return kas.locals().get(this, kas);
    }

    /** {@inheritDoc} */
    public final boolean isBound(A accessor) {
        BuildLocalSource bean = extract(accessor);
        return bean.locals().isBound(this, bean);
    }

    /** {@inheritDoc} */
    public final T orElse(A accessor, T other) {
        BuildLocalSource bean = extract(accessor);
        return bean.locals().orElse(this, bean, other);
    }

    public final <X extends Throwable> T orElseThrow(A accessor, Supplier<? extends X> exceptionSupplier) throws X {
        BuildLocalSource bean = extract(accessor);
        return bean.locals().orElseThrow(this, bean, exceptionSupplier);
    }

    // I think these are nice. We can use use for transformers. Add something for pre-transform.
    // Remove them for post, no need to keep them around
    public final T remove(A accessor) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    public final void set(A accessor, T value) {
        BuildLocalSource bean = extract(accessor);
        bean.locals().set(this, bean, value);
    }

    public static <L extends BuildLocal<?, ?>> Map<L, Object> initMap(Map<L, Object> existing, L local, Object value) {
        requireNonNull(local, "local is null");
        requireNonNull(value, "value is null");
        if (existing == null || existing.isEmpty()) {
            return Map.of(local, value);
        } else {
            IdentityHashMap<L, Object> ihm = new IdentityHashMap<>(existing);
            ihm.put(local, value);
            return Map.copyOf(ihm);
        }
    }
}
