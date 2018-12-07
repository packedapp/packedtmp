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
package app.packed.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import app.packed.inject.Provides;
import app.packed.inject.ServiceConfiguration;
import app.packed.inject.ServiceDescriptor;
import app.packed.util.Key;

/**
 * A stage that is executed during the import phase of an injector or module. A typically usage is to restrict what
 * services are imported from an injector or bundle.
 * <p>
 * In most cases the functionality provided by the various static methods will be enough.
 * 
 * 
 * <p>
 * This class contains common functionality for often used
 */
public abstract class InjectorImportStage extends ImportExportStage {

    /**
     * An import stage that imports no services by invoking {@link ServiceConfiguration#asNone()} on every processed
     * configuration.
     */
    // Rename to noImports() method -> will be much easier to make stages mutable if needed
    // Skal vi have noget med services med?????
    public static final InjectorImportStage NO_SERVICE = new InjectorImportStage() {

        @Override
        public void onService(ServiceConfiguration<?> sc) {
            sc.asNone();
        }
    };

    /** Creates a new stage */
    protected InjectorImportStage() {}

    /**
     * Creates a new stage with a lookup object. This constructor is only needed if the extending class makes use of the
     * {@link Provides} annotation.
     * 
     * @param lookup
     *            a lookup object that will be used for invoking methods annotated with {@link Provides}.
     */
    protected InjectorImportStage(MethodHandles.Lookup lookup) {
        super(lookup);
    }

    /**
     * Processes each service.
     * 
     * @param sc
     *            the service configuration
     */
    @Override
    protected void onService(ServiceConfiguration<?> sc) {}

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
    public static InjectorImportStage accept(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return new InjectorImportStage() {
            @Override
            public void onService(ServiceConfiguration<?> sc) {
                if (!predicate.test(ServiceDescriptor.of(sc))) {
                    sc.asNone();
                }
            }
        };
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified class keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static InjectorImportStage acceptKeys(Class<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return accept(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that only accepts services that have a key matching any of the specified keys.
     * 
     * @param keys
     *            the keys for which services will be accepted
     * @return the new import stage
     */
    public static InjectorImportStage acceptKeys(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        Set<Key<?>> set = Set.of(keys);
        return accept(d -> set.contains(d.getKey()));
    }

    /**
     * This method exists mainly to support debugging, where you want to see the services that are available at a particular
     * place in the pipeline: <pre>
     * {@code 
     * Injector injector = some injector to import;
     *
     * Injector.of(c -> {
     *   c.injectorBind(injector, ServiceImportStage.peek(e -> System.out.println("Importing service " + e.getKey())));
     * });}
     * </pre>
     * 
     * @param action
     *            the action to perform for each service
     * 
     * @return the new stage
     */
    public static InjectorImportStage peek(Consumer<? super ServiceDescriptor> action) {
        requireNonNull(action, "action is null");
        return new InjectorImportStage() {
            @Override
            public void onService(ServiceConfiguration<?> sc) {
                action.accept(ServiceDescriptor.of(sc));
            }
        };
    }

    /**
     * Creates a new stage that will rebind any service with the specified {@code from} key to the specified {@code from}
     * key. If there are no service with the specified the returned stage does nothing.
     * 
     * @param from
     *            the key of the service to rebind
     * @param to
     *            the key that the service should be rebound to
     * @return the new filter
     */
    public static InjectorImportStage rebind(Key<?> from, Key<?> to) {
        requireNonNull(from, "from is null");
        requireNonNull(to, "to is null");
        return new InjectorImportStage() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public void onService(ServiceConfiguration<?> sc) {
                if (sc.getKey().equals(from)) {
                    sc.as((Key) to);
                }
            }
        };
    }

    /**
     * Returns a new import stage that rejects any service accepted by the specified predicate.
     * 
     * @param predicate
     *            the predicate to test against
     * @return the new import stage
     */
    public static InjectorImportStage reject(Predicate<? super ServiceDescriptor> predicate) {
        requireNonNull(predicate, "predicate is null");
        return accept(e -> !predicate.test(e));
    }

    /**
     * Returns a new import stage that rejects any service with one of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static InjectorImportStage rejectKeys(Class<?>... keys) {
        Set<Key<?>> set = Arrays.stream(keys).map(k -> Key.of(k)).collect(Collectors.toSet());
        return reject(d -> set.contains(d.getKey()));
    }

    /**
     * Returns a new import stage that rejects any service with one of the specified keys.
     * 
     * @param keys
     *            the keys that should be rejected
     * @return the new import stage
     */
    public static InjectorImportStage rejectKeys(Key<?>... keys) {
        Set<Key<?>> set = Set.of(keys);
        return reject(d -> set.contains(d.getKey()));
    }

}

class XImportVer2 {

    InjectorImportStage andThen(InjectorImportStage next) {
        return next;
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

    public static <T, S> InjectorImportStage adapt(Key<T> key1, Key<T> newKey1, Function<S, T> f) {
        // Nahhh kun supporte provides her.. Evt. Factory...
        throw new UnsupportedOperationException();

        // Hvis eksempel med Vector

    }

    public static InjectorImportStage combine(InjectorImportStage s1, InjectorImportStage s2, InjectorImportStage... ss) {
        // Use case foer vi tilfoejer den.
        throw new UnsupportedOperationException();
    }

    // Men 1.
    // Man skal kunne sprede ting.
    // For eksempel. Skal vi kunne tage et PropertyMap
    // Og split det ud i Key<@XXX String>.
    // Det vil sige en service til mange...
    // Ikke supporteret

    static class OldService {
        String importInfo;
        String nonimportantInfo;
        Date startdato;
        Date stopdato;
        TimeZone timeZone;
    }
}