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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.component.Wirelet;
import packed.internal.inject.service.wirelets.PackedDownstreamServiceWirelet;

/**
 *
 */
public class ServiceToWirelets {

    public static <T> Wirelet map(Class<T> from, Class<? super T> to) {
        return map(Key.of(from), Key.of(to));
    }

    public static <T> Wirelet map(Key<T> from, Key<? super T> to) {
        // Changes the key of an entry (String -> @Left String
        throw new UnsupportedOperationException();
    }

    public static <T> Wirelet mapAll(Function<Service, ? super Key<?>> mapper) {
        throw new UnsupportedOperationException();
    }

    public static Wirelet peek(Consumer<? super ServiceRegistry> action) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet that will provide the specified service to the target container. Iff the target container has a
     * service of the specific type as a requirement.
     * <p>
     * Invoking this method is identical to invoking {@code provide(service.getClass(), service)}.
     * 
     * @param instance
     *            the service to provide
     * @return a wirelet that will provide the specified service
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Wirelet provide(Object instance) {
        requireNonNull(instance, "instance is null");
        return provideAs(instance, (Class) instance.getClass());
    }

    public static <T> Wirelet provideAs(T instance, Class<T> key) {
        return provideAs(instance, Key.of(key));
    }

    public static <T> Wirelet provideAs(T instance, Key<T> key) {
        return new PackedDownstreamServiceWirelet.ProvideInstance(key, instance);
    }
}

class ZBadIdeas {

    // Service transformere er saa meget lettere....
    // at bruge...
    // Det store problem er incoming hvor hvis vi laver bulk operations.
    // Saa bliver vi noedt til at tracke alle
    public static Wirelet compute(Function<? super ServiceRegistry, ? extends Optional<? extends Wirelet>> function) {
        // Must only provide ServiceWirelets...
        compute(e -> {
            if (e.isPresent(String.class)) {
                return Optional.of(ServiceToWirelets.map(String.class, CharSequence.class));
            }
            return Optional.empty();
        });
        throw new UnsupportedOperationException();
    }

    public static Wirelet compute(Predicate<? super ServiceRegistry> filter, Function<? super ServiceRegistry, Wirelet> function) {
        // Must only provide ServiceWirelets...
        compute(f -> f.isPresent(String.class), e -> ServiceToWirelets.map(String.class, CharSequence.class));
        throw new UnsupportedOperationException();
    }

    public static Wirelet computeFrom(Function<? super ServiceRegistry, ? extends Optional<? extends Wirelet>> function) {
        // Must only provide ServiceWirelets...
        compute(e -> {
            if (e.isPresent(String.class)) {
                return Optional.of(ServiceToWirelets.map(String.class, CharSequence.class));
            }
            return Optional.empty();
        });
        throw new UnsupportedOperationException();
    }

    public static Wirelet computeFrom(Predicate<? super ServiceRegistry> filter, Function<? super ServiceRegistry, Wirelet> function) {
        // Must only provide ServiceWirelets...
        compute(f -> f.isPresent(String.class), e -> ServiceToWirelets.map(String.class, CharSequence.class));
        throw new UnsupportedOperationException();
    }

    // Taenker det bliver lidt noget rod... fordi ServiceLocator er auto aktiverende...
    // Men maa lave en selection hvis man har behov for det...
    public static Wirelet exposeServiceLocator() {
        return exposeServiceLocator(Key.of(ServiceLocator.class));
    }

    public static Wirelet exposeServiceLocator(Key<? extends ServiceLocator> key) {
        throw new UnsupportedOperationException();
    }
}
