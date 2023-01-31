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
package internal.app.packed.lifetime.zbridge;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import app.packed.bindings.Key;
import app.packed.container.AbstractComposer;
import app.packed.container.AbstractComposer.ComposerAction;
import app.packed.extension.Extension;
import app.packed.operation.Op;

/**
 * Bruges til at kommunikere med en extension paa tvaers af en container lifetime
 * <p>
 */

// Usecases
/// ManagedLifetime (Maaske er det en metode)
/// ServiceLocator


// Functionality
/// A notification when the extension is installed
/// A notification if the extension is not installed
/// Provision of some useable value, typically the extension bean, or by extracting a value from the extension bean
/// Default value if the extension is not installed

// Certainty
//// Tror ikke skal bruge en Lookup til andet end at verificere extensionen
//// En almindelge bruger kan ikke skrive dem, de kraever en extension

// ContainerLifetimeWrapperCompanion

// ManagedLifetime er ikke en Extension...
// Restart vil jeg heller ikke mene er en extension? Eller er det

// Til venstre er der altid en ContainerLifetimeBean vil jeg mene...
//Alt konfigurationen omkring managed state maa ligger paa application/container driver
//Det er udelukkende extension exponering
// Maaske defineret nested paa ExtensionPoint? Eller paa ContainerDriver___

// Tror udelukkende det er for extensions..
// De andre ting er defineret

interface ContainerLifetimeCompanion {

    Set<Key<?>> keys(); // will be override services

    ContainerLifetimeCompanion hide(Key<?> key);
    ContainerLifetimeCompanion rekey();

    static <E extends Extension<E>> Builder<E> builder(MethodHandles.Lookup lookup, Class<E> extensionType) {
        throw new UnsupportedOperationException();
    }

    static <E extends Extension<E>> ContainerLifetimeCompanion of(Class<E> extensionType, ComposerAction<? extends Composer<E>> action) {
        throw new UnsupportedOperationException();
    }

    // Hvad hvis extensionen ikke er installeret?
    interface Builder<E extends Extension<E>> {
        void build();

        // or maybe extensionOptional
        Builder<E> requireExtension();

        Builder<E> onBuild(E extension);

        <B, C> Builder<E> provide(Class<C> key, Class<B> extensionBeanType, Function<B, C> extractor);

        <B, C> Builder<E> provide(Key<C> key, Class<B> extensionBeanType, Function<B, C> extractor);
    }

    interface OnKey<K> {

        void fromExtensionBean(Class<?> beanClass);

        <B> void fromExtensionBean(Class<B> beanClass, Function<B, K> function);

        void ifMissingInstance(K instance); // ServiceLocator.empty()
        void ifMissing(Supplier<K> supplier);
    }

    // ting
    //// Notify extension

    //// Key som man er tilgaengelig for

    //// ExtensionBean toKey
    //// Fallback if extension is not available

    public class Composer<E extends Extension<E>> extends AbstractComposer {
        public void onExtensionUse(Consumer<? super E> action) {
            // serviceExtension.externallyProvideServiceLocator = true;
        }

        public <B, C> void provide(Class<C> key, Class<B> extensionBeanType, Function<B, C> extractor) {}

        public <C> void provide(Class<C> key, Class<C> extensionBeanType) {}

        public <B, C> void provide(Key<C> key, Class<B> extensionBeanType, Function<B, C> extractor) {}

        public <C> void provide(Key<C> key, Class<C> extensionBeanType) {}

        public void provideOp(Op<?> op) {

        }
    }
}

// The owner, the installer, other extensions

//ExtractSingleExportedService (not the whole ServiceLocator)
//ServiceLocator
//JobResult
//RestartFoo?
// WebServerController?

//ManagedLifetimeController // Den her tror jeg kommer fra ApplicationDriver/ContainerDriver

// JobResult -> JobResult er taet knyttet lifetimes og ikke bare realms...
