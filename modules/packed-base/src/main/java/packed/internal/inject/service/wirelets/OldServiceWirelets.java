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
package packed.internal.inject.service.wirelets;

import java.time.ZoneId;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import app.packed.inject.Factory;
import app.packed.inject.Factory0;
import app.packed.inject.Factory1;
import app.packed.inject.Service;
import app.packed.service.Injector;

//Syntes det giver mening kun at prefixe have upstream....
//Eller evt mapTo, og mapFrom.... ddd.mapTo og ma

//peekTo.. peekFrom, /// link(new SomeBundle()

// This wirelet can only be used at assembly time...
// Skal vi have en protected verify();
// som saa kan kalde checkAssemblyTime()?

public final class OldServiceWirelets {

    /** No instantiation. */
    private OldServiceWirelets() {}

    // restrict optional services going in (some contract????) Bare besvaereligt at lave negative contracter.
    // Med mindre vi arbejder med commotative, associative osv. kontrakter...

    // Do

    public static <F, T> Wirelet extractUpstream(Class<F> fromKey, Class<T> toKey, Function<? super F, ? extends T> mapper) {
        return extractUpstream(Key.of(fromKey), Key.of(toKey), mapper);
    }

    public static <F, T> Wirelet extractUpstream(Key<F> fromKey, Key<T> toKey, Function<? super F, ? extends T> mapper) {
        return new PackedUpstreamInjectionWirelet.ApplyFunctionUpstream(fromKey, toKey, mapper, true);
    }

    public static void main(String[] args) {
        provideMapped(new Mapper<Long, Integer>(e -> e.intValue()) {});

        mapTo(new Factory1<TimeZone, ZoneId>(TimeZone::toZoneId) {});

        provideMapped(Long.class, Integer.class, e -> e.intValue());

        provideMapped(Key.of(Long.class), Key.of(Integer.class), e -> e.intValue());
        mapTo(new Factory1<Long, Integer>(Long::intValue) {});
    }

    /**
     * <p>
     * Wirelets returned by this method can only be used at assembly time.
     * 
     * @param factory
     *            a factory taking at least one argument (well
     * @return stuff
     */
    public static Wirelet mapTo(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public static <F, T> Wirelet mapUpstream(Class<F> fromKey, Class<T> toKey, Function<? super F, ? extends T> mapper) {
        return mapUpstream(Key.of(fromKey), Key.of(toKey), mapper);
    }

    public static <F, T> Wirelet mapUpstream(Key<F> fromKey, Key<T> toKey, Function<? super F, ? extends T> mapper) {
        return new PackedUpstreamInjectionWirelet.ApplyFunctionUpstream(fromKey, toKey, mapper, false);
    }

    public static Wirelet mapUpstream(Mapper<?, ?> mapper) {
        throw new UnsupportedOperationException();
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
     * @return a peeking wirelet
     */
    // I think it should be a set or some kind instead of a consumer

    // An immutable thingy of some kind...
    // Taenker vi godt vil foresporge om nogle ting
    public static Wirelet peekFrom(Consumer<? super Service> action) {
        return new PackedUpstreamInjectionWirelet.PeekFrom(action);
    }

    public static Wirelet peekTo(Consumer<? super Service> action) {
        return new PackedDownstreamServiceWirelet.PeekDownstreamWirelet(action);
    }

    // public static <T> Wirelet provideMapped(Factory<T> Key<T> type, T service) {
    // throw new UnsupportedOperationException();
    // }

    // Hmmm, syntes egentlig ikke man kan bruge den her...
    // IDK
    public static <T> Wirelet provide(Factory0<T> factory) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will provide all services that the specified injector provides
     * 
     * @param injector
     *            the injector to provide services from
     * @param wirelets
     *            for transforming and or restricting services
     * @return stuff
     */
    public static Wirelet provideAll(Injector injector, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // reject(Key... key)
    // reject(Class<?>... key)
    // reject(Predicate<? super Key|ServiceDescriptor>)

    public static <F, T> Wirelet provideMapped(Class<F> form, Class<T> to, Function<F, T> r) {
        throw new UnsupportedOperationException();
    }

    public static <F, T> Wirelet provideMapped(Key<F> form, Key<T> to, Function<F, T> r) {
        throw new UnsupportedOperationException();
    }
    // Maaske bare tag et factory?????
    //// Multiplicity many or singleton???
    // Saa kan vi have vilkaerlige

    // Problemet er at vi skal angive 2 noegler
    static Wirelet provideMapped(Mapper<?, ?> r) {
        throw new UnsupportedOperationException();
    }

    // Noget med contracter istedet for
    public static Wirelet provideOnly(Class<?>... keys) {
        // Retain
        // Only
        // Predicate
        throw new UnsupportedOperationException();
    }

    public static Wirelet removeUpstream(Class<?>... keys) {
        return new PackedUpstreamInjectionWirelet.FilterOnKey(Set.of(keys).stream().map(e -> Key.of(e)).collect(Collectors.toSet()));
    }

    public static Wirelet removeUpstream(Key<?>... keys) {
        return new PackedUpstreamInjectionWirelet.FilterOnKey(Set.of(keys));
    }

    // Maybe have a generic mapper, not only for injection...
    // Transformer, maaske i .function package
    static abstract class Mapper<T, R> {
        protected Mapper(Function<? super T, ? extends R> function) {
            throw new UnsupportedOperationException();
        }
    }

    static class Transformer<F, T> {
        Key<F> from() {
            throw new UnsupportedOperationException();
        }

        Key<F> to() {
            throw new UnsupportedOperationException();
        }
    }
}

/// into
//// Provide
//// Transformation
//// Removal

/// outfrom
//// Transformation
//// Removal (contract??)

// Can we have dependencies.... Det kan vi vel godt...
// public static Wirelet provideAll(Consumer<InjectorConfigurator> c) {
// return provideAll(Injector.of(configurator, wirelets));
// throw new UnsupportedOperationException();
// }
