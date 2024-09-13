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
package app.packed.container;

import java.util.function.Supplier;

import app.packed.application.ApplicationLocal;
import app.packed.build.BuildLocal;
import internal.app.packed.container.PackedContainerLocal;

/**
 * This class provides container-local variables at build-time.
 * <p>
 * Container locals are typically used for sharing (per-container) data between various parts of the application while
 * building it.
 *
 * @see app.packed.extension.container.ExtensionLink.Builder#consumeLocal(ContainerLocal, java.util.function.Consumer)
 * @see app.packed.extension.container.ExtensionLink.Builder#setLocal(ContainerLocal, Object)
 * @see app.packed.extension.container.ContainerBuilder#setLocal(ContainerLocal, Object)
 * @see app.packed.container.ContainerMirror
 */
public sealed interface ContainerLocal<T> extends BuildLocal<ContainerLocal.Accessor, T> permits PackedContainerLocal {

    /**
     * Returns a wirelet that will set the value of this container local, overriding any previous set value.
     * <p>
     * The returned wirelet can only be used at build-time. Attempting to use the returned wirelet at runtime will result in
     * a WireletException being thrown.
     *
     * @param value
     *            the value to set the local to
     * @return the new wirelet
     */
    // Hmm, extension wirelet??
    // Maybe on wirelet???

    // Problemet er consumption... Vi checker jo ikke om en wirelet er blevet laest
    Wirelet wireletSetter(T value);

    /**
     * Creates a new container local.
     *
     * @param <T>
     *            the type of value to store
     * @return the new container local
     */
    static <T> ContainerLocal<T> of() {
        return new PackedContainerLocal<>(null);
    }

    static <T> ContainerLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        return new PackedContainerLocal<>(initialValueSupplier);
    }

    /** An accessor where {@link ContainerLocal container local} values can be stored and retrieved. */
    // Extension?
    public sealed interface Accessor extends ApplicationLocal.Accessor permits ContainerConfiguration, ContainerHandle, ContainerMirror {}

    // En god maade at traekke sig selv ud...
    // ContainerLocal<FooExtension> myLocal = FooExtension.local();

    // ExtensionLocal(Application) // Map<Extension, T>
    // ExtensionLocal(Container) // Map<Extension, T>

    // AuthorLocal, FX med namespaces Map<Author, T>
}
//
//// A wirelet that will only applied if the value of the local is the specified value.
//// Cannot really see how it would work. Because now the order of wirelets are really important
//default Wirelet wireletConditionalGetter(T expectedValue, Wirelet wirelet) {
//    throw new UnsupportedOperationException();
//}
//

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
//
//default BeanLocal<T> toBeanLocal() {
//  throw new UnsupportedOperationException();
//}

//static <T> ContainerLocal<T> ofContainerLifetime() {
//    return PackedAbstractContainerLocal.of(PackedContainerLocal.PackedAbstractContainerLocal.CONTAINER_LIFETIME);
//}

//public static <T> ContainerLocal<T> ofContainerLifetime(Supplier<? extends T> initialValueSupplier) {
//    return PackedContainerLocal.of(PackedContainerLocal.Scope.CONTAINER_LIFETIME, initialValueSupplier);
//}

//static <T> ContainerLocal<T> ofDeployment() {
//    throw new UnsupportedOperationException();
//}

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
//
//static <T> ContainerLocal<T> ofFamily() {
//    throw new UnsupportedOperationException();
//}
//Extension?
//En god maade at traekke sig selv ud...
//ContainerLocal<FooExtension> myLocal = FooExtension.local();
