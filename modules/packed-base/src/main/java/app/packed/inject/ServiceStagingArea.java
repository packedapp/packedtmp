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
 *
 */
// Maybe just ServiceStaging.... Or Service.Staging
public interface ServiceStagingArea extends ServiceSpecification {

    void export(Key<?> key);
    // ServiceConfiguration????? Can we set tags?, ect, context. wulala??
    // Maybe we can set it and then let it be overridden...
    // Hvordan fanden bliver de sat i bundled.
    // Definere man en service, og saa lader den vaere overridable???

    ServiceStagingArea exportAllOptionalServices();

    ServiceStagingArea exportAllRequiredServices();

    /**
     * Export all services required and optional services from the parent.
     * 
     * @return
     */
    default ServiceStagingArea exportAllServices() {
        exportAllRequiredServices();
        exportAllOptionalServices();
        return this;
    }

    /**
     * Imports all of the exposed services.
     * 
     * @return
     */
    // TODO this override any service that have already been imported right????
    // Nej det syntes jeg egentlig ikke, skal vel virke lidt som importService(Predicate)
    // importAllServices().exportAllServices()
    default ServiceStagingArea importAllServices() {
        importAllServices(e -> true);
        return this;
    }

    /**
     * This method will traverse through all available service descriptor from {@link #exposedServices()} and apply the
     * specified predicate. If the predicate accepts the descriptor, {@link #importService(Key)} is called with the
     * descriptors {@link ServiceDescriptor#getKey() key}. And the returning service configuration is added to the map this
     * method returns.
     * <p>
     * Services that have already been imported by previous calls to, for example, {@link #importService(Key)} will still be
     * imported even if not accepted by the specified predicate. However, these will not be included in the returned map.
     * 
     * @param predicate
     *            the predicate to test whether a service should be imported
     * @return a map of the configurations of all services that was imported
     */
    default Map<Key<?>, ServiceConfiguration<?>> importAllServices(Predicate<? super ServiceDescriptor> predicate) {
        HashMap<Key<?>, ServiceConfiguration<?>> result = new HashMap<>();
        for (ServiceDescriptor sd : exposedServices().values()) {
            if (predicate.test(sd)) {
                result.put(sd.getKey(), importService(sd.getKey()));
            }
        }
        return Map.copyOf(result);
    }

    /**
     * Returns a map of all the services that have been imported.
     * 
     * <p>
     * This map will be remain empty until porpulated by calls to the import service functions. Initially this map will be
     * empt
     *
     * <p>
     * The returned map is partial mutable. Operations that remove entries from the map, such as {@link Map#clear()} or
     * {@link Map#remove(Object)}, are allowed. While operations that add entries to map, such as
     * {@link Map#put(Object, Object)}, will throw {@link UnsupportedOperationException}.
     *
     * @return a map of all the services that will be imported
     */
    Map<Key<?>, ServiceConfiguration<?>> importedServices();

    /**
     * Imports a service of the specified type. The imported is made available under the specified key unless other
     *
     * This can, for example, if trying to import 2 service from 2 module that are both exposed as the same type.
     * <p>
     * Calling this method repeatable with the same key, will return the same service configuration.
     *
     * @param <T>
     *            the type of service to import
     * @param key
     *            the key of the service
     * @return a service configuration
     * @throws IllegalArgumentException
     *             if a service for the specified type is not among the services returned by {@link #exposedServices()}
     */
    default <T> ServiceConfiguration<T> importService(Class<T> key) {
        return importService(Key.of(key));
    }

    <T> ServiceConfiguration<T> importService(Key<T> key);

    // exportedServices...
    // importedService...
}
