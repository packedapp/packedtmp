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
package app.packed.component.guest;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import app.packed.binding.Key;
import app.packed.build.BuildException;
import app.packed.container.ContainerBuildLocal;
import app.packed.extension.Extension;
import internal.app.packed.container.PackedContainerInstaller;
import internal.app.packed.container.PackedContainerLink;
import internal.app.packed.container.PackedContainerLink.KeyFragment;

// A link has

//// A (possible empty) set of keys that are provided under @ContainerHolderService
//// ContainerLocal operations
//// Constants
//// Rekey
//// (Future) actions to execute if the extension is not installed

/**
 * An container lifetime channel allows extensions to communicate across containers with different lifetimes.
 * <p>
 * At one end there is the parent container lifetime. And in the other end there is a distinct lifetime for a child
 * container.
 * <p>
 * The framework comes with a number of commonly used channel types:
 *
 * A (container lifetime) host bean {@link app.packed.extension.BaseExtensionPoint#EXPORTED_SERVICE_LOCATOR}
 */
// Extra functionality
//// Contexts, invocation arguments, key rewriting

// ContainerLifetimeFeature? Maaske man vil switch paa dem i wirelets

// Or just ContainerTemplate.ExtensionLink
// Hvad med mesh

// Move as nested class to ContainerTemplate

// ExtensionBridge?
public sealed interface OldContainerTemplateLink permits PackedContainerLink {

    /** {@return the extension that defined the link} */ // declaringExtension?
    Class<? extends Extension<?>> extension();

    // Can anyone use it? Downside of links being public now
    // List of partial extension? Or maybe hidden instead?
    // Or maybe a hidden marker that says
    default boolean isOpen() {
        return true;
    }

//   // Context injection???
//   // Hvordan faar vi dem hen til en bean???
//   // Vi kender jo ikke positionen.
//   // Non-context invocation args
//   public List<Class<?>> invocationArguments() {
//       return bridge.invocationArguments();
//   }

    /**
     * Host may use the {@link FromLifetimeChannel} annotation to have a service injected.
     *
     * <p>
     * It is build time failure to use multiple channels that have the same exports.
     *
     * @return any services that are made available
     *
     * @see Builder#exposeExports(Class...)
     * @see Builder#exposeExports(Key...)
     * @see FromLifetimeChannel
     */
    Set<Key<?>> keys();

    /**
     * Returns a new container lifetime channel builder for the specified extension.
     *
     * @param <E>
     *            the type of extension
     * @param caller
     *            a lookup object with full privilege access from the same module as the extension
     * @param extensionType
     *            the type of extension
     * @return the builder
     */
    // If we start to extract things we need the caller
    static OldContainerTemplateLink.Configurator of(MethodHandles.Lookup caller, Class<? extends Extension<?>> extensionType, String name) {
        if (!caller.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("caller must have full privilege access");
        } else if (caller.lookupClass().getModule() != extensionType.getModule()) {
            throw new IllegalArgumentException("extension type must be in the same module as the caller");
        }
        return new OldContainerTemplateLink.Configurator(new PackedContainerLink(extensionType, null, Map.of()));
    }

    // Man kan ikke lave det uden en extension class
//    /**
//     * Returns a simple link that simply provides a constant value for the specified key which can be accessed using
//     * {@link ContainerHolderService}.
//     * <p>
//     * The returned extension link will return {@code BaseExtension.class} from {@link #extensionClass()}.
//     *
//     * @param <T>
//     *            the type of constant
//     * @param key
//     *            the key under which to make the constant available
//     * @param constant
//     *            the constant
//     * @return the tunnel
//     */
//    static <T> ContainerTemplatePack ofConstant(Class<T> key, T constant) {
//        return ofConstant(Key.of(key), constant);
//    }
//
//    static <T> ContainerTemplatePack ofConstant(Key<T> key, T constant) {
//        return new ConstantContainerLifetimeTunnel(key, constant);
//    }

    /** A builder for {@link ExtensionLink}. */
    // Could move this class to ExtensionContext
    // and have newExtensionLink(String name) paa den
    // We need this builder. Because it has methods that should be exposed to
    // users of the built product.
    public static final class Configurator {

        /** The internal pack. */
        private PackedContainerLink pack;

        private Configurator(PackedContainerLink pack) {
            this.pack = requireNonNull(pack);
        }

        /**
         * Creates a new pack.
         *
         * @return the new pack
         */
        public OldContainerTemplateLink build() {
            return pack;
        }

        /**
         * @param from
         * @param to
         * @return the new template pack
         */

        // is used in the (unlikely) scenario with multiple links
        // that each provide something with the same key
        public OldContainerTemplateLink.Configurator rekey(Key<?> from, Key<?> to) {
            // from key must exist
            // Advanced operation
            // no case checks are performed
            // or maybe we do anywhere, should probably be simple
            throw new UnsupportedOperationException();
        }

        public <T> Configurator carrierProvideConstant(Class<T> key, T constant) {
            return carrierProvideConstant(Key.of(key), constant);
        }

        /**
         * @param <T>
         * @param key
         * @param constant
         * @return
         *
         * @see ContainerTemplate#carrierProvideConstant(Key, Object)
         * @see ContainerCarrierBeanConfiguration#carrierProvideConstant(Key, Object)
         * @see ContainerBuilder#carrierProvideConstant(Key, Object)
         */
        public <T> Configurator carrierProvideConstant(Key<T> key, T constant) {
            throw new UnsupportedOperationException();
        }

        // Typically used with locals with lazy initialization
        public <T> Configurator localConsume(ContainerBuildLocal<T> local, Consumer<T> action) {
            pack = pack.withUse(c -> c.localConsume(local, action));
            return this;
        }

        /**
         * @param <T>
         * @param local
         * @param value
         * @return
         *
         * @see ContainerTemplate#localSet(ContainerLocal, Object)
         * @see ContainerBuilder#localSet(ContainerLocal, Object)
         */
        public <T> Configurator localSet(ContainerBuildLocal<T> local, T value) {
            pack = pack.withUse(c -> c.setLocal(local, value));
            return this;
        }

        // Must be exported from an extension from container being linked
        public Configurator provideExpose(Class<?>... keys) {
            return provideExpose(Key.ofAll(keys));
        }

        /**
         * <p>
         * If this channel is applied to a container that does not export services with the specified keys. A build time
         * exception is thrown.
         *
         * @param keys
         *            the keys to include
         * @return this builder
         */

        // Taenker vi smider noget a.la. Foo foo template said it would expose Y but the extension provides no beans
        // (or provides no exports from [list all container beans])

        // Tror bare det skal vaere almindelige provisions
        // Ser ikke nogen grund til at blande. exports into it
        public Configurator provideExpose(Key<?>... keys) {
            for (Key<?> key : keys) {
                pack = pack.withFragments(key, new KeyFragment.OfExports());
            }
            return this;
        }

        // Hvis extensionen ikke er installeret (or exported)
        public <T> Configurator provideExposeOrElse(Key<T> key, T alternative) {
            return this;
        }

        public Configurator requireExtension() {
            return this;
        }

        public Configurator requireExtension(String errorMessage) {
            return this;
        }

        public Configurator requireExtension(Supplier<? extends BuildException> throwaer) {
            return this;
        }

        /**
         * Registers an action that will be run immediately after the channels extension is installed. But before
         * {@link Extension#onNew()} is called.
         * <p>
         * If the channels extension is never installed in the child container of the channel. The specified action is never
         * executed.
         *
         * @param action
         *            the action to execute
         * @return this builder
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        OldContainerTemplateLink.Configurator useBuilder(Consumer<? super PackedContainerInstaller> action) {
            pack = new PackedContainerLink(pack.extension(), pack.onUse() == null ? action : pack.onUse().andThen((Consumer) action), pack.services());
            return this;
        }
    }

    // Alternativt en Qualifier og saa local services... Og evt ingen context...
}

interface Zaandbox {

    // allow multiple instance of the same action [ExtensionClass+name]
    Zaandbox allowMultipleInstances();
}

// Maybe we want a ContainerRef? ApplicationPath
// Difficult to throw an exception with a good message here
//ContainerTemplatePack.Builder onNoUse(Runnable action) {
//    // An action that will be executed in the target container
//    return this;
//}
//
//// If arguments are needed from the lifetime start, must use a context.
//// Taenker den er til raadighed i hele extension lifetimen?
//// @InvocationArgs kan kun bruges af ejeren af en Lifetime
//ExtensionLink.Builder inContext(ContextTemplate template) {
//    requireNonNull(template, "template is null");
//    if (channel.extensionClass() != template.extensionClass()) {
//        throw new IllegalArgumentException();
//    }
//    return this;
//}
//public void install(ContainerSetup container) {
//    if (onUse != null) {
//        ExtensionPreLoad epl = container.preLoad.computeIfAbsent(extensionClass, k -> new ExtensionPreLoad());
//        epl.add(this);
//    }
//}
//ServiceLocator
//
//ExtractSingleExportedService (not the whole ServiceLocator)
//ServiceLocator
//ManagedLifetimeController
//JobResult
////extractService(Key k) <
//
////Hvad hvis extensionen ikke er installeret....

//Nope, saa har vi brug at kunne knytte operationer op et sted
////Arguments must be exported services. Or maybe context services
//public ExtensionLink.Builder<E> expose(Op<?> op) {
//throw new UnsupportedOperationException();
//}
