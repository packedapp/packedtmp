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
package app.packed.extension.container;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import app.packed.extension.ContainerLocal;
import app.packed.extension.Extension;
import app.packed.util.Key;
import internal.app.packed.container.PackedContainerBuilder;
import internal.app.packed.lifetime.PackedExtensionLink;

// A link has
//// A (possible empty) set of keys that are provided under @ContainerHolderService
//// ContainerLocal operations
//// Rekey
//// (Future) actions to execute if the extension is not installed
/**
 * An extension lifetime channel allows extensions to communicate across containers with different lifetimes.
 * <p>
 * At one end there is the parent container lifetime. And in the other end there is the child container lifetime.
 *
 * <p>
 * The framework comes with a number of commonly used channel types:
 *
 * A (container lifetime) host bean {@link app.packed.extension.BaseExtensionPoint#EXPORTED_SERVICE_LOCATOR}
 */
// Extra functionality
//// Contexts, invocation arguments, key rewriting

// Or just ContainerTemplate.ExtensionLink
// Hvad med mesh
public sealed interface ExtensionLink permits PackedExtensionLink {

    /** {@return the extension that defined the channel.} */
    Class<? extends Extension<?>> extensionClass();

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

    // is used in the (unlikely) scenario with multiple links
    // that each provide something with the same key
    ExtensionLink rekey(Key<?> from, Key<?> to);

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
    static ExtensionLink.Builder builder(MethodHandles.Lookup caller, Class<? extends Extension<?>> extensionType, String name) {
        if (!caller.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("caller must have full privilege access");
        } else if (caller.lookupClass().getModule() != extensionType.getModule()) {
            throw new IllegalArgumentException("extension type must be in the same module as the caller");
        }
        return new ExtensionLink.Builder(new PackedExtensionLink(extensionType, null, Set.of()));
    }

    // Alternativ ContainerLifetimeTemplate.withConstant()

    /**
     * Returns a simple link that simply provides a constant value for the specified key which can be accessed using
     * {@link ContainerHolderService}.
     * <p>
     * The returned extension link will return {@code BaseExtension.class} for {@link #extensionClass()}.
     *
     * @param <T>
     *            the type of constant
     * @param key
     *            the key under which to make the constant available
     * @param constant
     *            the constant
     * @return the link
     */
    static <T> ExtensionLink ofConstant(Class<T> key, T constant) {
        return ofConstant(Key.of(key), constant);
    }

    static <T> ExtensionLink ofConstant(Key<T> key, T arg) {
        // Det er er fint a BaseExtension staar som afsender
        // builder().expose(Op.ofConstant(Key.toVariable, arg).build()
        throw new UnsupportedOperationException();
    }

    /** A builder for {@link ExtensionLink}. */
    // Could move this class to ExtensionContext
    // and have newExtensionLink(String name) paa den
    public static final class Builder {
        // We need this builder. Because it has methods that should be exposed to
        // users of the built product.
        /** The internal channel. */
        private PackedExtensionLink channel;

        private Builder(PackedExtensionLink channel) {
            this.channel = requireNonNull(channel);
        }

        /**
         * Creates a new link.
         *
         *
         * @return the new link
         */
        public ExtensionLink build() {
            return channel;
        }

        // Hvis extensionen ikke er installeret (eller eksportere den)
        // exposeOrAlternative(Key<T>, T alternative)

        public <T> ExtensionLink.Builder consumeLocal(ContainerLocal<T> local, Consumer<T> action) {
            return useBuilder(c -> c.consumeLocal(local, action));
        }

        public ExtensionLink.Builder expose(Class<?>... keys) {
            return expose(Key.ofAll(keys));
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
        // Tror bare det skal vaere almindelige provisions
        // Ser ikke nogen grund til at bl.a. exports into it
        public ExtensionLink.Builder expose(Key<?>... keys) {
            PackedExtensionLink c = channel;
            HashSet<Key<?>> s = new HashSet<>(c.keys());
            s.addAll(Set.of(keys));
            channel = new PackedExtensionLink(c.extensionClass(), c.onUse(), Set.copyOf(s));
            return this;
        }

        // Maybe we want a ContainerRef? ApplicationPath
        // Difficult to throw an exception with a good message here
        ExtensionLink.Builder onNoUse(Runnable action) {
            // An action that will be executed in the target container
            return this;
        }

        public <T> ExtensionLink.Builder setLocal(ContainerLocal<T> local, T value) {
            return useBuilder(c -> c.setLocal(local, value));
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
        ExtensionLink.Builder useBuilder(Consumer<? super PackedContainerBuilder> action) {
            channel = new PackedExtensionLink(channel.extensionClass(),
                    channel.onUse() == null ? action : channel.onUse().andThen((Consumer) action), channel.keys());
            return this;
        }
    }

    // Alternativt en Qualifier og saa local services... Og evt ingen context...
}
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