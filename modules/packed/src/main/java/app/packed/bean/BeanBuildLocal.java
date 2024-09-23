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
package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;

import app.packed.bean.BeanBuildLocal.Accessor;
import app.packed.build.BuildLocal;
import internal.app.packed.bean.PackedBeanBuildLocal;

/**
 * This class provides build-time bean-local variables primarily for internal use by
 * {@link app.packed.extension.Extension extensions}.
 * <p>
 * Bean locals are typically used for sharing per-bean data between various parts of the application while the
 * application is being built. For example, a value can be set for a bean when installing it. And then later be
 * retrieved from a {@link BeanMirror bean mirror} instance.
 * <p>
 * The nested {@link Accessor} interfaces details all the entities that supports bean local storage in its permit
 * clause.
 * <p>
 * Bean local instances should generally not be shared outside outside of trusted code.
 * <p>
 * Bean locals should only be used while building an application. A notable exception is bean mirrors which may use them
 * for querying at runtime.
 *
 * @see app.packed.extension.bean.BeanBuilder#setLocal(BeanLocal, Object)
 * @see ContainerLocal
 */
public sealed interface BeanBuildLocal<T> extends BuildLocal<Accessor, T> permits PackedBeanBuildLocal {

    /**
     * Creates a new bean local without any initial value supplier.
     *
     * @param <T>
     *            the type of the bean local's value
     * @return a new bean local
     */
    static <T> BeanBuildLocal<T> of() {
        return new PackedBeanBuildLocal<>(null);
    }

    /**
     * Creates a new bean local. The initial value is determined by invoking the {@code get} method on the specified
     * {@code Supplier}. If the specified supplier returns null, no initial value will be set.
     *
     * @param <T>
     *            the type of the bean local's value
     * @param initialValueSupplier
     *            a supplier used to determine the initial value
     * @return a new bean local
     */
    static <T> BeanBuildLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        requireNonNull(initialValueSupplier);
        return new PackedBeanBuildLocal<>(initialValueSupplier);
    }

    /** An element where bean local values can be stored and loaded. */
    sealed interface Accessor permits BeanConfiguration, BeanElement, BeanHandle, BeanIntrospector, BeanMirror {}
}
