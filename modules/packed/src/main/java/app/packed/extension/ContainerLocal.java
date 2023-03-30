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

import app.packed.container.Wirelet;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.PackedContainerLocal;

/**
 * <p>
 * Container locals support 4 different scopes
 * <ul>
 * <li>Container scope: 1 value per container</li>
 * <li>Container Lifetime scope: 1 value per container lifetime</li>
 * <li>Application Scope: 1 value are application</li>
 * <li>Family Scope: 1 value per family, values</li>
 * </ul>
 *
 * @see app.packed.extension.container.ExtensionLink.Builder#consumeLocal(ContainerLocal, java.util.function.Consumer)
 * @see app.packed.extension.container.ExtensionLink.Builder#setLocal(ContainerLocal, Object)
 * @see app.packed.extension.container.ContainerBuilder#setLocal(ContainerLocal, Object)
 * @see app.packed.container.ContainerMirror
 * @see BeanLocal
 */
@SuppressWarnings("rawtypes")
// Tror vi biholder at have 2 type ContainerLocal and BeanLocal
// En extension har altid sin ExtensionInstance

// extends BeanLocal????? Nahh vi goere det nu. Men vi caster BeanLocal en masse steder
public abstract sealed class ContainerLocal<T> extends BeanLocal<T> permits PackedContainerLocal {

    @Override
    protected T get(BeanSetup bean) {
        return get(bean.container);
    }

    /**
     * @param container
     *            the container to return the value for
     * @return the value
     */
    protected abstract T get(ContainerSetup container);

    public T get(Extension<?> extension) {
        return get(extension.extension.container);
    }

    @Override
    protected boolean isPresent(BeanSetup bean) {
        return isPresent(bean.container);
    }

    protected abstract boolean isPresent(ContainerSetup container);

    public boolean isPresent(Extension<?> extension) {
        return isPresent(extension.extension.container);
    }

    @Override
    protected void set(BeanSetup bean, T value) {
        throw new UnsupportedOperationException();
    }

    public BeanLocal<T> toBeanLocal() {
        throw new UnsupportedOperationException();
    }

    public Wirelet wireletConditionalGetter(T expectedValue, Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }

    // Returns a wirelet that sets value of the container local to the specified value
    public abstract Wirelet wireletSetter(T value);

    /**
     * Creates a new container local with application scope.
     * <p>
     * Application scope means that <strong>all containers in the same application</strong> will always see the same value
     * for the specific container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    public static <T> ContainerLocal<T> ofApplication() {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.APPLICATION);
    }

    /**
     * Creates a bean local. The initial value is determined by invoking the {@code get} method on the specified
     * {@code Supplier}.
     *
     * @param <T>
     *            the type of the bean local's value
     * @param initialValueSupplier
     *            a supplier used to determine the initial value
     * @return a new bean local
     *
     */

    public static <T> ContainerLocal<T> ofApplication(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.APPLICATION, initialValueSupplier);
    }

    /**
     * Creates a new container local with container scope.
     * <p>
     * Container scope means that a separate value is stored for every container
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    public static <T> ContainerLocal<T> ofContainer() {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER);
    }

    public static <T> ContainerLocal<T> ofContainer(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER, initialValueSupplier);
    }

    /**
     * Creates a new container local with container lifetime scope.
     * <p>
     * Application scope means that <strong>all containers in the same lifetime</strong> will always see the same value for
     * the specific container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    static <T> ContainerLocal<T> ofContainerLifetime() {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER_LIFETIME);
    }

    public static <T> ContainerLocal<T> ofContainerLifetime(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER_LIFETIME, initialValueSupplier);
    }

    static <T> ContainerLocal<T> ofFamily() {
        throw new UnsupportedOperationException();
    }
}
