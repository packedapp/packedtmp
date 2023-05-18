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
 * This class provides container-local variables at build-time.
 * <p>
 * Container locals are typically used for sharing per-bean data between various parts of the application while building
 * it.
 *
 * <p>
 * A container local support 4 different scopes:
 * <ul>
 * <li><b>Container scope</b>: A single value is stored for every container</li>
 * <li><b>Container-Lifetime scope</b>: Every container in the same lifetime shares a single value.</li>
 * <li><b>Application scope</b>: All containers in the same application shares a single value</li>
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
public abstract sealed class ContainerLocal<T> permits PackedContainerLocal {

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

    protected boolean isPresent(BeanSetup bean) {
        return isPresent(bean.container);
    }

    protected abstract boolean isPresent(ContainerSetup container);

    public boolean isPresent(Extension<?> extension) {
        return isPresent(extension.extension.container);
    }

    protected void set(BeanSetup bean, T value) {
        throw new UnsupportedOperationException();
    }

    public BeanLocal<T> toBeanLocal() {
        throw new UnsupportedOperationException();
    }

    Wirelet wireletConditionalGetter(T expectedValue, Wirelet wirelet) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that can be used to set the value of this bean local.
     * <p>
     * The returned wirelet cannot be used at runtime.
     *
     * @param value
     *            the value to set the local to
     * @return the new wirelet
     */
    public abstract Wirelet wireletSetter(T value);

    /**
     * Creates a new container local with application scope.
     * <p>
     * Application scope means that <strong>all containers in the same application</strong> will always see the same value
     * for a specific container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    public static <T> ContainerLocal<T> ofApplication() {
        return PackedContainerLocal.of(PackedContainerLocal.LocalScope.APPLICATION);
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
//
//    public static <T> ContainerLocal<T> ofApplication(Supplier<? extends T> initialValueSupplier) {
//        return PackedContainerLocal.of(PackedContainerLocal.Scope.APPLICATION, initialValueSupplier);
//    }

    // What?
    static <T> ContainerLocal<T> ofApplicationLink() {
        throw new UnsupportedOperationException();
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
        return PackedContainerLocal.of(PackedContainerLocal.LocalScope.CONTAINER);
    }

    public static <T> ContainerLocal<T> ofContainer(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(PackedContainerLocal.LocalScope.CONTAINER, initialValueSupplier);
    }

    /**
     * Creates a new container local with container lifetime scope.
     * <p>
     * Container-lifetime scope means that <strong>all containers in the same lifetime</strong> will always see the same
     * value for a specific container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    static <T> ContainerLocal<T> ofContainerLifetime() {
        return PackedContainerLocal.of(PackedContainerLocal.LocalScope.CONTAINER_LIFETIME);
    }

//    public static <T> ContainerLocal<T> ofContainerLifetime(Supplier<? extends T> initialValueSupplier) {
//        return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER_LIFETIME, initialValueSupplier);
//    }

    public static <T> ContainerLocal<T> ofDeployment() {
        throw new UnsupportedOperationException();
    }

    // Vi mangler nok en local der tillader at rekursive leder efter vaerdier...
    // Hvor man kan overskrive det per container.
    // Den giver nok mest mening per application og per family.

    // Use casen er fx FileExtension.defaultDomain() paa tvaers af applicationen.
    // Vi saetter det jo i roden og det kan overskrives per container..
    // Saa enten skal extensionen selv vedligeholde det. Eller ogsaa skal vi bruge container locals.

    // Det er nok taet knyttet til Namespaces/Domains som jo ogsaa en slags nested locals.

    // ContainerBuilder.setLocal kommer noget af vejen. Men

    // Her skal vi pakke den ind i en WeakReference. Fordi vi saadan set maaske kan unloade containere
    // Der bruger den.
    // Vi skal nok ogsaa gemme dem i en CHM fordi vi ikke rigtig ved hvornaar der bliver skrevet til den.

    public static <T> ContainerLocal<T> ofFamily() {
        throw new UnsupportedOperationException();
    }
}
