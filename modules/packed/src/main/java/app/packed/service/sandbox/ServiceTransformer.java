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
package app.packed.service.sandbox;

import java.util.function.Consumer;
import java.util.function.Function;

import app.packed.binding.Key;

/**
 *
 */

// What information do I have when the actual transformer is ready.

//Idea is to replace ServiceIncomingTransformer and ServiceOutgoingTransformer with this class.
public sealed interface ServiceTransformer {

    /**
     * A version of {@link #decorate(Key, Function)} that takes a {@code class} key. See other method for details.
     *
     * @param <T>
     *            the type of the service that should be decorated
     * @param key
     *            the key of the service that should be decorated
     * @param decoratingFunction
     *            the decoration function
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist
     * @see #decorate(Key, Function)
     */
    default <T> void decorate(Class<T> key, Function<? super T, ? extends T> decoratingFunction) {
        decorate(Key.of(key), decoratingFunction);
    }

    /**
     * Decorates a service with the specified key using the specified decoration function.
     * <p>
     * If the service that is being decorated is constant. The function will be invoked at most. Put this else, it is the
     * same for peek, ect.
     *
     * @param <T>
     *            the type of the service that should be decorated
     * @param key
     *            the key of the service that should be decorated
     * @param decoratingFunction
     *            the decoration function
     * @throws NoSuchElementException
     *             if a service with the specified key does not exist
     * @see #decorate(Class, Function)
     */
    // TODO must check return type..
    <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction);

    default <T> void peek(Class<T> key, Consumer<? super T> consumer) {
        peek(Key.of(key), consumer);
    }

    <T> void peek(Key<T> key, Consumer<? super T> consumer);

    // DecorateAll(BiFunction<Key, T> -> T);

    public non-sealed interface Incoming extends ServiceTransformer {}

    public non-sealed interface Outgoing extends ServiceTransformer {}

}
