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
package app.packed.lifetime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.bindings.Key;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.service.sandbox.transform.ServiceExportsTransformer;
import internal.app.packed.lifetime.zbridge.PackedBridge;

/**
 *
 */
// Features
//// Kan expose services that can be used together ContainerGuest
//// Kan

////Args
////Contexts???
public final class ExtensionLifetimeBridge {

    /** The internal lifetime bridge. */
    private final PackedBridge<?> bridge;

    private ExtensionLifetimeBridge(PackedBridge<?> bridge) {
        this.bridge = requireNonNull(bridge);
    }

    /** {@return the extension that declares the bridge.} */
    public Class<? extends Extension<?>> extensionClass() {
        return bridge.extensionClass;
    }

//    // Context injection???
//    // Hvordan faar vi dem hen til en bean???
//    // Vi kender jo ikke positionen.
//    // Non-context invocation args
//    public List<Class<?>> invocationArguments() {
//        return bridge.invocationArguments();
//    }

    /**
     * Guests must use the {@link ContainerGuest} annotation
     *
     * @return any services that are made available
     *
     * @see app.packed.container.ContainerGuest
     */
    public Set<Key<?>> keys() {
        return bridge.keys();
    }

    // Hmmmmmm, fraekt
    ExtensionLifetimeBridge transformServices(Consumer<ServiceExportsTransformer> transformer) {
        throw new UnsupportedOperationException();
    }

    public static <E extends Extension<E>> ExtensionLifetimeBridge.Builder<E> builder(MethodHandles.Lookup caller, Class<E> extensionType) {
        return new Builder<>(PackedBridge.builder(caller, extensionType));
    }

    public static final class Builder<E extends Extension<E>> {

        /** The internal bridge */
        private PackedBridge<E> bridge;

        Builder(PackedBridge<E> bridge) {
            this.bridge = requireNonNull(bridge);
        }

        /**
         * Builds and returns a new bridge.
         *
         * @return the new bridge
         */
        public ExtensionLifetimeBridge build() {
            return new ExtensionLifetimeBridge(bridge);
        }

        // If arguments are needed from the lifetime start, must use a context.
        // Taenker den er til raadighed i hele extension lifetimen?
        // @InvocationArgs kan kun bruges af ejeren af en Lifetime
        public Builder<E> inContext(ContextTemplate template) {
            requireNonNull(template, "template is null");
            if (bridge.extensionClass != template.extensionClass()) {
                throw new IllegalArgumentException();
            }
            return this;
        }

        public Builder<E> includeExport(Class<?>... keys) {
            return includeExport(Key.ofAll(keys));
        }

        // Must be exported by the extension
        public Builder<E> includeExport(Key<?>... keys) {
            Set.of(keys);
            return this;
        }

        // The specified action is run immediately before onNew?
        public Builder<E> onUse(Consumer<? super E> action) {
            bridge = bridge.onUse(action);
            return this;
        }

        // containerBuilder(SomeLifetime, "asdasd");
        // I don't know if we will ever need it
        public <A> Builder<E> onUseWithBuildArg(Class<A> argType, BiConsumer<? super E, ? super A> action) {
            throw new UnsupportedOperationException();
        }
    }
}
//ServiceLocator
//
//ExtractSingleExportedService (not the whole ServiceLocator)
//ServiceLocator
//ManagedLifetimeController
//JobResult
//// extractService(Key k) <
//
//// Hvad hvis extensionen ikke er installeret....

// Ideen er at installere beans der kan be exposed to guest objektet.
// Fx ServiceLocator
// Hvorfor er det ikke bare extensions der installere den og ikke bridgen

// (e-> installGuest(F.class).dasd);

// OperationTemplate???

// 123 paa runtime som argument.
// Hvordan faar jeg det ind i en bean
// Anden end via ContextInjection???

// InvocationContextArgument

// Create an internalContext???

// Bliver noedt til at vaere unik. Kan ikke add

//
//public Builder<E> provide(Class<?> extensionBean, Op<?> op) {
//  // Adds synthetic operation to extensionBean
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Class<T> key) {
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> extensionBean, Key<T> key) {
//  return this;
//}

//public <T> Builder<E> provide(Key<T> key, Class<T> type) {
//  bridge = bridge.addInvocationArgument(type);
//  return this;
//}
//
//public <T> Builder<E> provide(Class<T> type, Key<?> ) {
//  bridge = bridge.addInvocationArgument(type);
//  return this;
//}