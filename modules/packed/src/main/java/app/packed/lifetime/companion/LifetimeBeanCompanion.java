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
package app.packed.lifetime.companion;

import java.lang.invoke.MethodHandles;
import java.util.function.Function;

import app.packed.base.Key;
import app.packed.container.Extension;

/**
 * Det den her kan er at kommunikere paa tvaers af lifetime barriere
 * <p>
 * Hvorfor kan jeg ikke klare det paa andre maader?
 */

// ManagedLifetime er ikke en Extension...
// Restart vil jeg heller ikke mene er en extension? Eller er det


// Alt konfigurationen omkring managed state maa ligger paa application/container driver

// Til venstre er der altid en ContainerLifetimeBean vil jeg mene...

// Maaske defineret nested paa ExtensionPoint?
public interface LifetimeBeanCompanion {

    static <E extends Extension<E>> Builder<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        throw new UnsupportedOperationException();
    }

    // Hvad hvis extensionen ikke er installeret?
    interface Builder<E extends Extension<E>> {
        Builder<E> onBuild(E extension);

        <B, C> Builder<E> provide(Class<C> companionType, Class<B> extensionBeanType, Function<B, C> extractor);

        <B, C> Builder<E> provide(Key<C> companionType, Class<B> extensionBeanType, Function<B, C> extractor);

        LifetimeBeanCompanion build();
    }
}
//ExtractSingleExportedService (not the whole ServiceLocator)
//ServiceLocator
//JobResult
//RestartFoo?
// WebServerController?

//ManagedLifetimeController // Den her tror jeg kommer fra ApplicationDriver/ContainerDriver

// JobResult -> JobResult er taet knyttet lifetimes og ikke bare realms...
