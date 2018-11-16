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
import java.util.Set;
import java.util.function.Predicate;

/**
 * This interface provides methods to exactly control which services are imported and exported when one injector is
 * imported into another injector.
 */
// Maybe just ServiceStaging.... Or Service.Staging
// Eller noget med Joint, joine
// ServiceLink
// https://www.thesaurus.com/browse/join
public interface ServiceStagingArea extends ServiceSpecification {

    void export(Key<?> key);
    // ServiceConfiguration????? Can we set tags?, ect, context. wulala??
    // Maybe we can set it and then let it be overridden...
    // Hvordan fanden bliver de sat i bundled.
    // Definere man en service, og saa lader den vaere overridable???

    default ServiceStagingArea exportAllOptionalServices() {
        optionalServices().forEach(c -> export(c));
        return this;
    }

    default ServiceStagingArea exportAllRequiredServices() {
        requiredServices().forEach(c -> export(c));
        return this;
    }

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

    Set<Key<?>> exportedServices();

    /**
     * Imports all of the services exposed via {@link ServiceSpecification#exposedServices()} that have not already been
     * imported.
     * 
     * @return this staging
     */
    ServiceStagingArea importAllServices();

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
     * Returns an immutable view of all the services that have been imported. Initially this map is empty. But can be filled
     * up by calling the various import methods of this interface.
     *
     * @return an immutable view of all the services that will be imported
     */
    Map<Key<?>, ServiceConfiguration<?>> importedServices();

    /**
     * Equivalent to calling {@code importService(Key.of(key)}.
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

    /**
     * Imports a service with the specified key. Or throws an {@link IllegalArgumentException} if the specified key does not
     * match one of the {@link ServiceSpecification#exposedServices() exposed services}.
     * <p>
     * The returned service configuration can be used to make the service available under a different key. This is useful,
     * for example, if trying to import two services from two different injectors that are both exposed as the same type.
     * The imported is made available under the specified key unless other
     *
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
    <T> ServiceConfiguration<T> importService(Key<T> key);

    // exportedServices...
    // importedService...
}
