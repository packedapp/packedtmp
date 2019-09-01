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

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.container.Wirelet;
import app.packed.util.Key;

/**
 * Various wirelets that can be used to transform and filter services being pull and pushed into containers.
 */
public final class DownstreamServiceWirelets {

    /** No instantiation. */
    private DownstreamServiceWirelets() {}

    // restrict optional services going in (some contract????) Bare besvaereligt at lave negative contracter.
    // Med mindre vi arbejder med commotative, associative osv. kontrakter...

    /**
     * @param <T>
     * @param factory
     * @return a wirelet that provides
     */
    // providePrototype()
    public static <T> Wirelet provide(Factory0<T> factory) {
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet provide(Class<T> key, T service) {
        return provide(Key.of(key), service);
    }

    public static <T> Wirelet provide(Key<T> key, T service) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will provide the specified service to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code provide(service.getClass(), service)}.
     * 
     * @param service
     *            the service to provide
     * @return a wirelet that will provide the specified service
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Wirelet provide(Object service) {
        requireNonNull(service, "service is null");
        return provide((Class) service.getClass(), service);
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

    // Problemet er at vi skal angive 2 noegler
    public static Wirelet provideMapped(Mapper<?, ?> r) {
        throw new UnsupportedOperationException();
    }

    // Maaske bare tag et factory?????
    //// Multiplicity many or singleton???
    // Saa kan vi have vilkaerlige

    // public static <T> Wirelet provideMapped(Factory<T> Key<T> type, T service) {
    // throw new UnsupportedOperationException();
    // }

    public static void main(String[] args) {
        provideMapped(new Mapper<Long, Integer>(e -> e.intValue()) {});
    }

    public static Wirelet provideOnly(Class<?>... keys) {
        // Retain
        // Only
        // Predicate
        throw new UnsupportedOperationException();
    }

    // Maybe have a generic mapper, not only for injection...
    // Transformer, maaske i .function package
    static abstract class Mapper<T, R> {
        protected Mapper(Function<? super T, ? extends R> function) {
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
