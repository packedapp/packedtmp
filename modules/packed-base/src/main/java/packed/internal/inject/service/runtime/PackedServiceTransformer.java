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
package packed.internal.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceTransformer;
import packed.internal.inject.service.build.ConstantServiceBuild;
import packed.internal.inject.service.build.ServiceBuild;

/** Implementation of {@link ServiceTransformer}. */
public class PackedServiceTransformer extends AbstractServiceRegistry implements ServiceTransformer {

    /** The services that we are transforming */
    public final Map<Key<?>, ServiceBuild> services;

    public PackedServiceTransformer(Map<Key<?>, ServiceBuild> services) {
        this.services = requireNonNull(services);
    }

    private ServiceBuild checkKeyExists(Key<?> key) {
        ServiceBuild s = services.remove(key);
        if (s == null) {
            throw new NoSuchElementException("No service with the specified key exists, key = " + key);
        }
        return s;
    }

    /** {@inheritDoc} */
    @Override
    public <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction) {
        requireNonNull(key, "key is null");
        requireNonNull(decoratingFunction, "decoratingFunction is null");
        services.put(key, checkKeyExists(key).decorate(decoratingFunction));
    }

    @Override
    public <T> void provideInstance(Key<T> key, T instance) {
        requireNonNull(key, "key is null");
        requireNonNull(instance, "instance is null");
        // instance must be assignable to key raw type, maybe the build entry should check that...
        services.put(key, new ConstantServiceBuild(key, instance));
    }

    /** {@inheritDoc} */
    @Override
    public void rekey(Key<?> existingKey, Key<?> newKey) {
        requireNonNull(existingKey, "existingKey is null");
        requireNonNull(newKey, "newKey is null");
        if (existingKey.equals(newKey)) {
            throw new IllegalStateException("Cannot rekey the same key, key = " + existingKey);
        } else if (services.containsKey(newKey)) {
            throw new IllegalStateException("A service with newKey already exists, key = " + newKey);
        }
        services.put(newKey, checkKeyExists(existingKey).rekeyAs(newKey));
    }

    /** {@inheritDoc} */
    @Override
    public void rekeyAll(Function<Service, @Nullable Key<?>> function) {
        requireNonNull(function, "function is null");

        Map<Key<?>, ServiceBuild> newServices = new HashMap<>();
        for (ServiceBuild s : services.values()) {
            Key<?> key = function.apply(s.toService());
            // If the function returns null we exclude the key
            if (key != null) {
                Key<?> existing = s.key();
                // we need to rekey the service if the function return a new key
                if (!key.equals(existing)) {
                    s = s.rekeyAs(existing);
                }
                // Make sure that we do not end up with multiple services with the same key
                if (newServices.putIfAbsent(key, s) != null) {
                    throw new IllegalStateException("A service with key already exists, key = " + key);
                }
            }
        }
        // We replace in-map just in case somebody got the idea of storing
        // ServiceRegistry#keys() somewhere
        services.clear();
        services.putAll(newServices);
    }

    /** {@inheritDoc} */
    @Override
    public void remove(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        for (Key<?> k : keys) {
            requireNonNull(k, "key in specified array is null");
            services.remove(k);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeAll() {
        services.clear();
    }

    /** {@inheritDoc} */
    @Override
    public void removeIf(Predicate<? super Service> filter) {
        requireNonNull(filter, "filter is null");
        for (Iterator<ServiceBuild> iterator = services.values().iterator(); iterator.hasNext();) {
            ServiceBuild s = iterator.next();
            if (filter.test(s.toService())) {
                iterator.remove();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void retain(Key<?>... keys) {
        services.keySet().retainAll(List.of(keys));
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<Key<?>, Service> servicesX() {
        return (Map) services;
    }

    public ServiceLocator toServiceLocator() {
        Map<Key<?>, RuntimeService> runtimeEntries = new LinkedHashMap<>();
        ServiceInstantiationContext con = new ServiceInstantiationContext();
        for (ServiceBuild e : services.values()) {
            runtimeEntries.put(e.key(), e.toRuntimeEntry(con));
        }
        return new PackedInjector(ConfigSite.UNKNOWN, runtimeEntries);
    }
}
