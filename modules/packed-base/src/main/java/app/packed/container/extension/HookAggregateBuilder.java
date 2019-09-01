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
package app.packed.container.extension;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.function.BiFunction;

/**
 *
 */
// HookCollector
// Tror godt vi vil have en specific alligevel....
// Bliver hurtig lidt rodet nu med en

// Alternativ kan vi lave en abstract klasse, med hjaelpe metoder...
// Group instead.... HookGroup

// Take all methods that starts with "on"

// HookGroupBuilder istedet for???
// HookAggregator
public interface HookAggregateBuilder<T> {

    /** Builds and returns the aggregate object. */
    T build();

    /**
     * Is typically used during tests to verify
     * 
     * @param <A>
     * @param <T>
     * @param t
     * @param target
     * @return a new aggregate
     */
    static <A extends HookAggregateBuilder<T>, T> T generate(Lookup lookupBuilder, Class<A> t, Lookup lookupTarget, Class<?> target) {
        // Ideen er at folk kan proeve at lave deres egen aggregate...
        throw new UnsupportedOperationException();
    }

    static <A extends HookAggregateBuilder<T>, T> BiFunction<Lookup, Class<?>, T> generateGenerator(Lookup lookup, Class<A> t) {
        // Virker ikke med cross-module class hierachies. Maaske skal vi bare droppe det til andet end tests....
        throw new UnsupportedOperationException();
    }
}

// Vi kan godt supportere consumation af nogle instantiated method handles....
