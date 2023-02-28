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
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.container.PackedContainerLocal;

/**
 * @see app.packed.extension.container.ExtensionLink.Builder#consumeLocal(ContainerLocal, java.util.function.Consumer)
 * @see app.packed.extension.container.ExtensionLink.Builder#setLocal(ContainerLocal, Object)
 * @see app.packed.extension.container.ContainerBuilder#setLocal(ContainerLocal, Object)
 * @see app.packed.container.ContainerMirror
 * @see BeanLocal
 */
@SuppressWarnings("rawtypes")
// Tror vi biholder at have 2 type ContainerLocal and BeanLocal
// En extension har altid sin ExtensionInstance
public abstract sealed class ContainerLocal<T> permits PackedContainerLocal {

    protected abstract boolean isPresent(ContainerSetup container);

    public boolean isPresent(Extension<?> extension) {
        return isPresent(ExtensionSetup.crack(extension).container);
    }

    public T get(BeanIntrospector introspector) {
        return get(BeanSetup.crack(introspector).container);
    }

    public T get(Extension<?> extension) {
        return get(ExtensionSetup.crack(extension).container);
    }

    protected abstract T get(ContainerSetup container);

    public BeanLocal<T> toBeanLocal() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a bean local without any initial value.
     *
     * @param <T>
     *            the type of the bean local's value
     * @return a new bean local
     */
    public static <T> ContainerLocal<T> of() {
        return PackedContainerLocal.of(null);
    }

    public static <T> ContainerLocal<T> ofContainer() {
        return PackedContainerLocal.of(null);
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
    public static <T> ContainerLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(initialValueSupplier);
    }

    public static <T> ContainerLocal<T> ofContainerLifetime(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(initialValueSupplier);
    }

    public static <T> ContainerLocal<T> ofApplication(Supplier<? extends T> initialValueSupplier) {
        return PackedContainerLocal.of(initialValueSupplier);
    }

    static <T> ContainerLocal<T> ofContainerFan() {
        throw new UnsupportedOperationException();
    }

    static <T> ContainerLocal<T> ofContainerLifetime() {
        throw new UnsupportedOperationException();
    }

    Wirelet setLocalWirelet(T value) {
        throw new UnsupportedOperationException();
    }

    // ofApplicationTree
}

//Kunne jo ogsaa bare ofApplicationBarrierCrossing

/// Hvis vi kun ender med ContainerInstaller.
/// Ser jeg ingen grund til at have ApplicationLocal kun for en container

//Inherited

//Man kan saette det paa en application guest... // Gaelder for alle containers.
//Man kan saette det paa en ContainerInstaller
//Dvs vi har gemt et map vi enten cloner eller ogsaa kigger i...

//ContainerGuest -> Installation -> Instance

//ApplicationGuest -> Installation -> Instance

//ApplicationGuest -> RuntimeInstallation -> Instance

//ApplicationDeployer <-- Alle skal have denne contract
interface ApplicationLocal<T> {

    static <T> ApplicationLocal<T> of() {
        throw new UnsupportedOperationException();
    }

    static <T> ApplicationLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        throw new UnsupportedOperationException();
    }

// // E == root extension
// static <T, E extends Extension<E>> ApplicationLocal<T> of(MethodHandles.Lookup caller, Class<E> extensionType, Function<E, T> factory) {
//    return new ApplicationLocal<T>() {};
// }
}
