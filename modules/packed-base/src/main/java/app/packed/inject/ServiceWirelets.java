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

import java.util.function.Consumer;

import app.packed.container.Wirelet;
import app.packed.util.Key;

/**
 *
 */
public final class ServiceWirelets {

    /** No instantiation. */
    private ServiceWirelets() {}

    // provide
    // provideMapped
    // restrict optional services going in (some contract????) Bare besvaereligt at lave negative contracter.
    // Med mindre vi arbejder med commotative, associative osv. kontrakter...

    /**
     * Returns a wirelet that will provide the specified service, invoking this method is identical to
     * {@code provide(service.getClass(), service)}. The service will be available in the receiving container if it is a
     * requirement of the container
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

    public static <T> Wirelet provide(Class<T> key, T service) {
        return provide(Key.of(key), service);
    }

    public static <T> Wirelet provide(Key<T> key, T service) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will provide all services that the specified injector provides
     * 
     * @param injector
     * @return stuff
     */
    public static Wirelet provideAll(Injector injector /* , Wirelet... wirelets */) {
        throw new UnsupportedOperationException();
    }

    // Can we have dependencies.... Det kan vi vel godt...
    public static Wirelet provideAll(Consumer<InjectorConfigurator> c) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet provideOnly(Class<?>... keys) {
        // Retain
        // Only
        // Predicate
        throw new UnsupportedOperationException();
    }

    // Maaske bare tag et factory?????
    //// Multiplicity many or singleton???
    // Saa kan vi have vilkaerlige

    // public static <T> Wirelet provideMapped(Factory<T> Key<T> type, T service) {
    // throw new UnsupportedOperationException();
    // }

    // Problemet er at vi skal angive 2 noegler
    public static Wirelet provideMapped(Rebinder<?, ?> r) {
        throw new UnsupportedOperationException();
    }

    static class Rebinder<F, T> {}
}

/// into
//// Provide
//// Transformation
//// Removal

/// outfrom
//// Transformation
//// Removal (contract??)