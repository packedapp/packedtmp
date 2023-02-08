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
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import app.packed.bindings.Key;
import app.packed.context.ContextTemplate;
import app.packed.extension.Extension;
import app.packed.operation.Op;
import app.packed.service.sandbox.transform.ServiceExportsTransformer;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.lifetime.PackedContainerLifetimeChannel;

// Link, Channel
/**
 * What allows extensions to communicate across containers with different lifetimes.
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
public final class ContainerLifetimeChannel {

    /** The internal container lifetime channel. */
    private final PackedContainerLifetimeChannel<?> channel;

    private ContainerLifetimeChannel(PackedContainerLifetimeChannel<?> channel) {
        this.channel = requireNonNull(channel);
    }

    /** {@return the extension that defined the channel.} */
    public Class<? extends Extension<?>> extensionClass() {
        return channel.extensionClass();
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
    public Set<Key<?>> keys() {
        return channel.exports();
    }

    ContainerLifetimeChannel transformServices(Consumer<ServiceExportsTransformer> transformer) {
        // Hmmmmmm, fraekt
        // I don't think we need to put this on the builder. Can just use this for post processing
        // if needed. For example, we export under one key but want to return something else.
        throw new UnsupportedOperationException();
    }

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
    public static <E extends Extension<E>> ContainerLifetimeChannel.Builder<E> builder(MethodHandles.Lookup caller, Class<E> extensionType, String name) {
        if (!caller.hasFullPrivilegeAccess()) {
            throw new IllegalArgumentException("caller must have full privilege access");
        } else if (caller.lookupClass().getModule() != extensionType.getModule()) {
            throw new IllegalArgumentException("extension type must be in the same module as the caller");
        }
        return new ContainerLifetimeChannel.Builder<>(new PackedContainerLifetimeChannel<>(extensionType, null, Set.of()));
    }

    // Alternativ ContainerLifetimeTemplate.withConstant()
    public static <T> ContainerLifetimeChannel ofConstant(Class<T> key, T arg) {
        return ofConstant(Key.of(key), arg);
    }

    public static <T> ContainerLifetimeChannel ofConstant(Key<T> key, T arg) {
        // Det er er fint a BaseExtension staar som afsender
        // builder().expose(Op.ofConstant(Key.toVariable, arg).build()
        throw new UnsupportedOperationException();
    }


    /** A builder for container lifetime channels. */
    public static final class Builder<E extends Extension<E>> {

        /** The internal channel. */
        private PackedContainerLifetimeChannel<E> channel;

        private Builder(PackedContainerLifetimeChannel<E> channel) {
            this.channel = requireNonNull(channel);
        }

        /**
         * Builds and returns a new channel.
         *
         * @return the new channel
         */
        public ContainerLifetimeChannel build() {
            return new ContainerLifetimeChannel(channel);
        }

        // Arguments must be exported services. Or maybe context services
        public ContainerLifetimeChannel.Builder<E> expose(Op<?> op) {
            throw new UnsupportedOperationException();
        }

        public ContainerLifetimeChannel.Builder<E> expose(Class<?>... keys) {
            return expose(Key.ofAll(keys));
        }

        /**
         * <p>
         * If this channel is applied to a container that does not export services with the specified keys. A build time
         * exception is thrown.
         *
         *
         * @param keys
         *            the keys to include
         * @return this builder
         *
         */
        public ContainerLifetimeChannel.Builder<E> expose(Key<?>... keys) {
            PackedContainerLifetimeChannel<E> c = channel;
            HashSet<Key<?>> s = new HashSet<>(c.exports());
            s.addAll(Set.of(keys));
            channel = new PackedContainerLifetimeChannel<>(c.extensionClass(), c.onUse(), Set.copyOf(s));
            return this;
        }

        // If arguments are needed from the lifetime start, must use a context.
        // Taenker den er til raadighed i hele extension lifetimen?
        // @InvocationArgs kan kun bruges af ejeren af en Lifetime
        ContainerLifetimeChannel.Builder<E> inContext(ContextTemplate template) {
            requireNonNull(template, "template is null");
            if (channel.extensionClass() != template.extensionClass()) {
                throw new IllegalArgumentException();
            }
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
        public ContainerLifetimeChannel.Builder<E> onUse(Consumer<? super E> action) {
            requireNonNull(action, "action is null");
            PackedContainerLifetimeChannel<E> ch = channel;
            Consumer<? super ExtensionSetup> c = e -> action.accept((E) e.instance());
            channel = new PackedContainerLifetimeChannel<E>(ch.extensionClass(), ch.onUse() == null ? c : ch.onUse().andThen((Consumer) c), ch.exports());
            return this;
        }

        // Maybe we want a ContainerRef? ApplicationPath
        // Difficult to throw an exception with a good message here
        ContainerLifetimeChannel.Builder<E> onNoUse(Runnable action) {
            // An action that will be executed in the target container
            return this;
        }

        // containerBuilder(SomeLifetime, "asdasd");
        // I don't know if we will ever need it
        // Will add an argument to lifetime create
        <A> ContainerLifetimeChannel.Builder<E> onUseWithBuildArg(Class<A> argType, BiConsumer<? super E, ? super A> action) {
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
////extractService(Key k) <
//
////Hvad hvis extensionen ikke er installeret....