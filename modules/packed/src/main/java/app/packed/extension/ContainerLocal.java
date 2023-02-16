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

import internal.app.packed.container.PackedContainerLocal;

/**
 *
 */
@SuppressWarnings("rawtypes")
public abstract sealed class ContainerLocal<T> permits PackedContainerLocal {

//    static <T> ContainerLocal<T> ofAssembly() {
//        throw new UnsupportedOperationException();
//    }

    // Hvor brugbar er denne?? Er det ikke bare at store det i selve extension???

    // Den er super god til at saette noget paa ContainerInstaller,

    // Og saa faa det frem i extensionen... Naar man initialize den.
    // Vil sige det er vel den eneste maade
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

    static <T> ContainerLocal<T> ofContainerFan() {
        throw new UnsupportedOperationException();
    }

    static <T> ContainerLocal<T> ofContainerLifetime() {
        throw new UnsupportedOperationException();
    }
}
