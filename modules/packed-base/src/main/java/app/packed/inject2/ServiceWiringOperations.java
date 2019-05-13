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
package app.packed.inject2;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import app.packed.bundle.WiringOption;
import app.packed.inject.Factory;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;
import packed.internal.inject.ServiceUtils;
import packed.internal.inject.ServiceWiringImportOperation;

/**
 * A number of wiring operations for services.
 */
// InjectorWiringOperations.....Eneste grund til jeg ikke bruger dem
// Er at starting/stopping wiring operations maaske kommer i app.packed.lifecycle package
// Omvendt gider vi jo ikke have en hel klasse til rename af en injector...

// Alias()->creates an alias of a service, Rebind() changes the key, and transform() takes a function

// Tro maaske vi skal to klasser, incoming, outgoing
public final class ServiceWiringOperations {

    /** An wiring operation that removes every service in the import pipeline. */
    public static final WiringOption NO_IMPORTS = new ServiceWiringImportOperation() {

        /** {@inheritDoc} */
        @Override
        public void onEachService(ServiceConfiguration<?> sc) {
            // sc.asNone();
        }
    };

    // public static IIS PRINT_KEY = peek(
    private ServiceWiringOperations() {}

    /**
     * This method exists mainly to support debugging, where you want to see which services are available at a particular
     * place in the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import;
     *
     * Injector.of(c -> {
     *   c.bindInjector(injector, ServiceImportStages.peek(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * <p>
     * This method is typically TODO before after import events
     * 
     * @param action
     *            the action to perform for each service descriptor
     * @return a peeking stage
     */
    public static WiringOption peekImports(Consumer<? super ServiceDescriptor> action) {
        requireNonNull(action, "action is null");
        return new ServiceWiringImportOperation() {
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                action.accept(ServiceUtils.wrapperOf(sc));
            }
        };
    }

    /**
     * Returns a stage that will rebind any service with the specified {@code from} key to the specified {@code from} key.
     * If no service with the specified key is encountered, the stage does nothing.
     * 
     * @param from
     *            the key of the service to rebind
     * @param to
     *            the key that the service should be rebound to
     * @return the new filter
     */
    // rebindToParent
    // rebindToChild
    public static WiringOption rebindImport(Key<?> from, Key<?> to) {
        requireNonNull(from, "from is null");
        requireNonNull(to, "to is null");
        return new ServiceWiringImportOperation() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                if (sc.getKey().equals(from)) {
                    sc.as((Key) to);
                }
            }
        };
    }

    /**
     * Returns a new stage that reject services with any of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static WiringOption removeImports(Class<?>... keys) {
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return removeImports(d -> set.contains(d.key()));
    }

    /**
     * Returns a new import stage that reject services with any of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static WiringOption removeImports(Key<?>... keys) {
        Set<Key<?>> set = Set.of(keys);
        return removeImports(d -> set.contains(d.key()));
    }

    // process(Key<K>, Consumer<K, K>); <- Som @Provides X process(X x) {return x}
    // process(Key<K>, Function<K, K>); <- Som @Provides X process(X x) {return something}

    /**
     * Returns a new import stage that rejects any service accepted by the specified predicate.
     * 
     * @param predicate
     *            the predicate to test against
     * @return the new import stage
     */
    public static WiringOption removeImports(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return retainImports(e -> !predicate.test(e));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified class keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static WiringOption retainImports(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return retainImports(d -> set.contains(d.key()));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static WiringOption retainImports(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Set.of(keys);
        return retainImports(d -> set.contains(d.key()));
    }

    /**
     * Returns a new import stage that only accepts the services accepted by the specified predicate.
     * <p>
     * For example, {@code accept(s -> s.getKey().isQualifiedWith(Blue.class))} will return an import stage that only
     * accepts services whose key are qualified with {@code @Blue}.
     * 
     * @param predicate
     *            the predicate that selects which services are accepted by this stage
     * @return the new import stage
     */
    public static WiringOption retainImports(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return new ServiceWiringImportOperation() {
            @Override
            public void onEachService(ServiceConfiguration<?> sc) {
                if (!predicate.test(ServiceUtils.wrapperOf(sc))) {
                    // sc.asNone();
                }
            }
        };
    }

    /**
     * 
     * The service that is provided will have no description and no tags.
     * 
     * @param <T>
     *            stuff
     * @param <R>
     *            stuff
     * @param instance
     *            the instance to bind
     * @return the wiring operation
     */
    public static <T, R> WiringOption bindDownstream(Object instance) {
        MethodHandles.lookup();
        throw new UnsupportedOperationException();
    }

    public static <T, R> WiringOption bindDownstream(Factory<?> factory) {
        throw new UnsupportedOperationException();
    }

    public static <T, R> WiringOption mapOutgoing(Key<T> from, Key<R> to, Function<? super T, ? extends R> mapper) {
        throw new UnsupportedOperationException();
    }

}
