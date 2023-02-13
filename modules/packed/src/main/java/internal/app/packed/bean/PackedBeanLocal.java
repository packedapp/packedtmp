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
package internal.app.packed.bean;

import static internal.app.packed.bean.BeanSetup.crack;
import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.extension.BeanHandle;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.BeanLocal;
import app.packed.util.Nullable;

/**
 *
 */
// Alternativ til attachments

// Min eneste anke er an man maaske gerne vil kunne bruge navnet for noget
// Der fungere paa runtime

// Maybe have a BeanLocalMap as well
public final class PackedBeanLocal<T> implements BeanLocal<T> {

    private final @Nullable Supplier<? extends T> initialValueSupplier;

    private PackedBeanLocal(@Nullable Supplier<? extends T> initialValueSupplier) {
        this.initialValueSupplier = initialValueSupplier;
    }

    public T get(BeanConfiguration configuration) {
        return get(BeanSetup.crack(configuration));
    }

    public T get(BeanHandle<?> handle) {
        return get(crack(handle));
    }

    public T get(BeanIntrospector introspector) {
        return get(BeanSetup.crack(introspector));
    }

    @SuppressWarnings("unchecked")
    public T get(BeanSetup bean) {
        if (initialValueSupplier == null) {
            return (T) bean.locals.get(this);
        } else {
            return (T) bean.locals.computeIfAbsent(this, e -> e.initialValueSupplier.get());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanConfiguration configuration) {
        return isPresent(crack(configuration));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanHandle<?> handle) {
        return isPresent(crack(handle));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent(BeanIntrospector introspector) {
        return isPresent(crack(introspector));

    }

    public boolean isPresent(BeanSetup setup) {
        return setup.locals.containsKey(this);
    }

    /** {@inheritDoc} */
    @Override
    public <B extends BeanIntrospector> B set(B introspector, T value) {
        set(crack(introspector), value);
        return introspector;
    }

    /** {@inheritDoc} */
    @Override
    public <B extends BeanConfiguration> B set(B configuration, T value) {
        set(crack(configuration), value);
        return configuration;
    }

    /** {@inheritDoc} */
    @Override
    public <B extends BeanHandle<?>> B set(B handle, T value) {
        set(crack(handle), value);
        return handle;
    }

    public void set(BeanSetup bean, T value) {
        requireNonNull(value);
        bean.locals.put(this, value);
    }

    public static <T> PackedBeanLocal<T> of(@Nullable Supplier<? extends T> initialValueSupplier) {
        return new PackedBeanLocal<>(initialValueSupplier);
    }
}
