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

/**
 *
 */
// Kunne jo ogsaa bare ofApplicationBarrierCrossing


/// Hvis vi kun ender med ContainerInstaller.
/// Ser jeg ingen grund til at have ApplicationLocal kun for en container

// Inherited

// Man kan saette det paa en application guest... // Gaelder for alle containers.
// Man kan saette det paa en ContainerInstaller
// Dvs vi har gemt et map vi enten cloner eller ogsaa kigger i...

// ContainerGuest -> Installation -> Instance

// ApplicationGuest -> Installation -> Instance

// ApplicationGuest -> RuntimeInstallation -> Instance

// ApplicationDeployer <-- Alle skal have denne contract
public interface ApplicationLocal<T> {

    static <T> ApplicationLocal<T> of() {
        throw new UnsupportedOperationException();
    }

    static <T> ApplicationLocal<T> of(Supplier<? extends T> initialValueSupplier) {
        throw new UnsupportedOperationException();
    }

//    // E == root extension
//    static <T, E extends Extension<E>> ApplicationLocal<T> of(MethodHandles.Lookup caller, Class<E> extensionType, Function<E, T> factory) {
//       return new ApplicationLocal<T>() {};
//    }
}
