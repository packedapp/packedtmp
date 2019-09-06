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
package app.packed.inject;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.packed.container.Wirelet;
import app.packed.util.Key;
import packed.internal.inject.build.wirelets.PackedUpstreamInjectionWirelet;

/**
 *
 */
public final class UpstreamServiceWirelets {

    private UpstreamServiceWirelets() {}

    static class Transformer<F, T> {
        Key<F> from() {
            throw new UnsupportedOperationException();
        }

        Key<F> to() {
            throw new UnsupportedOperationException();
        }
    }

    // reject(Key... key)
    // reject(Class<?>... key)
    // reject(Predicate<? super Key|ServiceDescriptor>)

    public static <F, T> Wirelet map(Key<F> fromKey, Key<T> toKey, Function<? super F, ? extends T> mapper) {
        return new PackedUpstreamInjectionWirelet.ApplyFunction(fromKey, toKey, mapper);
    }

    public static Wirelet remove(Class<?>... keys) {
        return new PackedUpstreamInjectionWirelet.FilterOnKey(Set.of(keys).stream().map(e -> Key.of(e)).collect(Collectors.toSet()));
    }

    public static Wirelet remove(Key<?>... keys) {
        return new PackedUpstreamInjectionWirelet.FilterOnKey(Set.of(keys));
    }

    /**
     * This method exists mainly to support debugging, where you want to see which services are available at a particular
     * place in a the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import;
     *
     * Injector.of(c -> {
     *   c.importAll(injector, DownstreamServiceWirelets.peek(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * <p>
     * This method is typically TODO before after import events
     * 
     * @param action
     *            the action to perform for each service descriptor
     * @return a peeking stage
     */
    public static Wirelet peek(Consumer<? super ServiceDescriptor> action) {
        return new PackedUpstreamInjectionWirelet.Peek(action);
    }
}
