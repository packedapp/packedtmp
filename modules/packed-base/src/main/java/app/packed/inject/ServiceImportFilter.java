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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * When you import an injector all service are imported per default. And you must eksplicitly define every imported
 * service. Calling importService(X.class) will remove all default imported services
 */
// Naar vi nu har to versioner...
// of(Injector)->imports all
// of(Injector, Consumer) imports none per default, must manual import
// ServiceRemapper??? Vi filter + reconfigurere
public interface ServiceImportFilter {

    /**
     * Returns an immutable map of all services that are available for import. A service whose key have been remapped will
     * have t <pre> {@code
     *  Key<Integer> -> Descriptor<Key<Integer>, "MyService>
     *  importService(Integer.class).as(new Key<@Left Integer>));
     *  Key<Integer> -> Descriptor<Key<@Left Integer>, "MyService>
     *  Note the key of the map has not changed, only the key of the descriptor.}
     * </pre> Eller ogsaa er det kun i imported servicess?????Ja det tror jeg
     *
     * @return all the services that are available for import
     */
    Map<Key<?>, ServiceDescriptor> availableServices();

    /**
     * Returns a map of all the services that have been imported. The default is to import every service, unless
     * <p>
     * This map will be remain empty until porpulated by calls to the import service functions. Initially this map will be
     * empt
     *
     * Initially all services from will be imported.
     *
     * @return a map of all the services that have been imported
     */
    Map<Key<?>, ServiceConfiguration<?>> importedServices();

    /**
     * Explicitly imports a service of the specified type.
     *
     * This can, for example, if trying to import 2 service from 2 module that are both exposed as the same type.
     * <p>
     * Calling this method repeatable with the same key, will return the same service configuration.
     *
     * @param <T>
     *            the type of service to import
     * @param type
     *            the type of service
     * @return a service configuration
     * @throws IllegalArgumentException
     *             if a service of the specified type is not among the services returned by {@link Injector#services()}
     */
    default <T> ServiceConfiguration<T> importService(Class<T> type) {
        return importService(Key.of(type));
    }

    <T> ServiceConfiguration<T> importService(Key<T> type);

    /**
     * This method will traverse through all available service descriptor from {@link #availableServices()} and apply the
     * specified predicate. If the predicate accepts the descriptor, {@link #importService(Key)} is called with the
     * descriptors {@link ServiceDescriptor#getKey() key}. And the returning service configuration is added to the map this
     * method returns.
     *
     * @param predicate
     *            the predicate to test whether a service should be imported
     * @return a map of the configurations of all services that was imported
     */
    default Map<Key<?>, ServiceConfiguration<?>> importServices(Predicate<? super ServiceDescriptor> predicate) {
        HashMap<Key<?>, ServiceConfiguration<?>> result = new HashMap<>();
        for (ServiceDescriptor sd : availableServices().values()) {
            if (predicate.test(sd)) {
                result.put(sd.getKey(), importService(sd.getKey()));
            }
        }
        return Map.copyOf(result);
    }
    // importNoServices<---- giver ikke saa meget mening her, men i ContainerImport goer det nok..
}
