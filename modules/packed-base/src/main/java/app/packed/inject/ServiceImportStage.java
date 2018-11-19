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

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import app.packed.util.ConfigurationSite;
import app.packed.util.Nullable;

/**
 * A stage that is executed during the import phase of an injector or module.
 */
// TODO preProcess(), postProcess() <- mainly to check invariants, for example, did not find a key to rebind.
public abstract class ServiceImportStage extends ServiceFilter {

    /** A filter that imports nothing. */
    public static final ServiceImportStage NONE = new ServiceImportStage() {

        @Override
        public void process(ServiceConfiguration<?> sc) {
            sc.asNone();
        }
    };

    /** Creates a new stage */
    protected ServiceImportStage() {}

    /**
     * Creates a new stage with a lookup object. This constructor is typically only used if the extending class makes use of
     * the {@link Provides} annotation.
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    protected ServiceImportStage(MethodHandles.Lookup lookup) {
        super(lookup);
    }

    protected final ServiceConfiguration<?> clone(ServiceConfiguration<?> sc) {
        return sc;// Do we ever need to make a service available under two different keys
    }

    /**
     * Returns a map of all service
     * 
     * @return stuff
     */
    // Don't know if we want this method?????, or we are strictly a passthrough biatch
    protected final Map<Key<?>, ServiceDescriptor> exposedServices() {
        throw new UnsupportedOperationException();
    }

    protected final Map<Key<?>, ServiceConfiguration<?>> imoortedServices() {
        throw new UnsupportedOperationException();
    }

    public void process(ServiceConfiguration<?> sc) {}

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified class keys.
     * 
     * @param keys
     *            the accepted keys
     * @return the new import stage
     */
    public static ServiceImportStage accept(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return accept(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified keys.
     * 
     * @param keys
     *            the accepted keys
     * @return the new import stage
     */
    public static ServiceImportStage accept(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Set.of(keys);
        return accept(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that only accepts the services accepted by the specified predicate.
     * <p>
     * For example, {@code accept(s -> s.getKey().isQualifiedWith(Blue.class))} will return an import stage that will only
     * accept the services that are qualified with a {@code @Blue} annotation.
     * 
     * @param predicate
     *            the predicate that selects which services are accepted by this stage
     * @return the new import stage
     */
    public static ServiceImportStage accept(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return new ServiceImportStage() {
            @Override
            public void process(ServiceConfiguration<?> sc) {
                if (!predicate.test(new DescriptorAdaptor(sc))) {
                    sc.asNone();
                }
            }
        };
    }

    public static <T, S> ServiceImportStage adapt(Key<T> key1, Key<T> newKey1, Function<S, T> f) {
        // Nahhh kun supporte provides her.. Evt. Factory...
        throw new UnsupportedOperationException();

        // Hvis eksempel med Vector

    }

    // Men 1.
    // Man skal kunne sprede ting.
    // For eksempel. Skal vi kunne tage et PropertyMap
    // Og split det ud i Key<@XXX String>.
    // Det vil sige en service til mange...

    // Returns an import filter that accepts all the services selected by the specified predicate

    // c.importServices(i, ServiceImportStage.peek(e -> System.out.println("Available " + e.getKey())),
    // ServiceImportStage.accept(String.class));
    public static ServiceImportStage peek(Consumer<? super ServiceDescriptor> action) {
        requireNonNull(action, "action is null");
        return new ServiceImportStage() {
            @Override
            public void process(ServiceConfiguration<?> sc) {
                action.accept(new DescriptorAdaptor(sc));
            }
        };
    }

    /**
     * Creates
     * <p>
     * If no service with specified key is process the
     * 
     * @param key
     *            the key of the service
     * @param rebindTo
     *            the key that the service should be rebound to
     * @return the new filter
     */
    public static ServiceImportStage rebind(Key<?> key, Key<?> rebindTo) {
        requireNonNull(key, "rebindTo is null");
        requireNonNull(rebindTo, "rebindTo is null");
        return new ServiceImportStage() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void process(ServiceConfiguration<?> sc) {
                if (sc.getKey().equals(key)) {
                    sc.as((Key) rebindTo);
                }
            }
        };
    }

    public static ServiceImportStage reject(Class<?>... keys) {
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return reject(d -> set.contains(d.getKey()));
    }

    public static ServiceImportStage reject(Key<?>... keys) {
        Set<Key<?>> set = Set.of(keys);
        return reject(d -> set.contains(d.getKey()));
    }

    public static ServiceImportStage reject(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return accept(e -> !predicate.test(e));
    }

    static class DescriptorAdaptor implements ServiceDescriptor {

        private final ServiceConfiguration<?> configuration;

        /**
         * @param configuration
         */
        public DescriptorAdaptor(ServiceConfiguration<?> configuration) {
            this.configuration = configuration;
        }

        /** {@inheritDoc} */
        @Override
        public BindingMode getBindingMode() {
            return configuration.getBindingMode();
        }

        /** {@inheritDoc} */
        @Override
        public ConfigurationSite getConfigurationSite() {
            return configuration.getConfigurationSite();
        }

        /** {@inheritDoc} */
        @Override
        public @Nullable String getDescription() {
            return configuration.getDescription();
        }

        /** {@inheritDoc} */
        @Override
        public Key<?> getKey() {
            return configuration.getKey();
        }

        /** {@inheritDoc} */
        @Override
        public Set<String> tags() {
            return Collections.unmodifiableSet(configuration.tags());
        }
    }

    static class OldService {
        String importInfo;
        String nonimportantInfo;
        Date startdato;
        Date stopdato;
        TimeZone timeZone;
    }
}
