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
package app.packed.extension;

import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import internal.app.packed.bean.PackedBeanLocal;

/**
 * A bean local can be used internally by extensions to xxx
 * <p>
 * Bean locals are only usable when building and application, they are never available at runtime.
 *
 * @see app.packed.extension.BaseExtensionPoint.BeanInstaller#setLocal(BeanLocal, Object)
 */
// get, use, remove..
@SuppressWarnings("rawtypes")
public sealed interface BeanLocal<T> permits PackedBeanLocal {

    boolean isPresent(BeanConfiguration configuration);

    boolean isPresent(BeanHandle<?> handle);

    boolean isPresent(BeanIntrospector introspector);

    <B extends BeanIntrospector> B set(B introspector, T value);

    <B extends BeanConfiguration> B set(B configuration, T value);

    /**
     * Sets the value of t
     *
     * @param <B>
     *            the type of bean handle
     * @param handle
     *            the bean handle that represents the bean
     * @param value
     *            the value to set
     * @return the specified bean handle
     */
    <B extends BeanHandle<?>> B set(B handle, T value);

    /**
     * Creates a bean local without any initial value.
     *
     * @param <T>
     *            the type of the bean local's value
     * @return a new bean local
     */
    static <T> BeanLocal<T> of() {
        return PackedBeanLocal.of(null);
    }

    /**
     * Creates a bean local . The initial value is determined by invoking the {@code get} method on the specified
     * {@code Supplier}.
     *
     * @param <T>
     *            the type of the bean local's value
     * @param initialValueSupplier
     *            a supplier used to determine the initial value
     * @return a new bean local
     *
     */
    static <T> BeanLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        return PackedBeanLocal.of(initialValueSupplier);
    }
}
