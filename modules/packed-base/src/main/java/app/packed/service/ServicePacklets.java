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
package app.packed.service;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.container.Wirelet;
import app.packed.inject.Factory;
import packed.internal.service.build.wirelets.PackedDownstreamInjectionWirelet;
import packed.internal.service.build.wirelets.PackedUpstreamInjectionWirelet;

/**
 *
 */
// mapTo -> removes original
// mapFrom -> removes original
public class ServicePacklets {

    public static Wirelet mapFrom(Class<?> originalKey, Class<?> newKey) {
        return mapFrom(requireNonNull(originalKey, "originalKey is null"), requireNonNull(newKey, "newKey is null"));
    }

    public static Wirelet mapFrom(Factory<?> mapper) {
        // What if we have zero dependencies???? Fail or allow.. Why not allow...
        throw new UnsupportedOperationException();
    }

    public static Wirelet mapFrom(Key<?> originalKey, Key<?> newKey) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet
     * 
     * @param originalKey
     *            the key of the service to map
     * @param newKey
     *            the new key of the service
     * @return the new wirelet
     * 
     * @see #mapTo(Key, Key)
     * @see #mapTo(Factory)
     */
    // What kind of exception (if any) do we throw if the original key not there???
    // WiringException??
    // Can we have an ifPresent(Key.., Wirelet)? Nah,
    public static Wirelet mapTo(Class<?> originalKey, Class<?> newKey) {
        return mapTo(requireNonNull(originalKey, "originalKey is null"), requireNonNull(newKey, "newKey is null"));
    }

    public static Wirelet mapTo(Factory<?> mapper) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet mapTo(Key<?> originalKey, Key<?> newKey) {
        throw new UnsupportedOperationException();
    }

    /**
     * This method exists mainly to support debugging, where you want to see which services are available at a particular
     * place in a the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import all services from;
     *
     * Injector.of(c -> {
     *   c.provideAll(injector, ServicePacklets.peekFrom(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * <p>
     * This method is typically TODO before after import events
     * 
     * @param action
     *            the action to perform for each service descriptor
     * @return a peeking stage
     */
    public static Wirelet peekFrom(Consumer<? super ServiceDescriptor> action) {
        return new PackedUpstreamInjectionWirelet.PeekFrom(action);
    }

    /**
     * <p>
     * Note: That every service available for the ... Service that are not required (mandatory or optionally) are ignored.
     * 
     * @param action
     * @return the new packlet
     */
    public static Wirelet peekTo(Consumer<? super ServiceDescriptor> action) {
        return new PackedDownstreamInjectionWirelet.PeekDownstreamWirelet(action);
    }
}
