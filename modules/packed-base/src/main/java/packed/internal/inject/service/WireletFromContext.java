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
package packed.internal.inject.service;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.cube.ExtensionMember;
import app.packed.inject.Service;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceRegistry;
import app.packed.inject.ServiceTransformer;
import packed.internal.inject.service.build.ExportedServiceBuild;
import packed.internal.inject.service.build.ServiceBuild;
import packed.internal.inject.service.runtime.AbstractServiceRegistry;

/** A */
public final class WireletFromContext implements ServiceTransformer {

    private ServiceWireletFrom current;

    @Nullable
    final ServiceExportManager m;

    Map<Key<?>, ServiceBuild> services = new HashMap<>();

    final List<ServiceWireletFrom> wirelets;

    WireletFromContext(List<ServiceWireletFrom> wirelets, @Nullable ServiceExportManager m) {
        this.wirelets = requireNonNull(wirelets);
        this.m = m;
        if (m != null) {
            for (ExportedServiceBuild a : m) {
                services.put(a.key(), a);
            }
        }
    }

    private void checkCurrent(ServiceWireletFrom wirelet) {
        if (current != wirelet) {
            new IllegalStateException("This operation cannot be performed after the wirelet has been processed");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Key<?> key) {
        requireNonNull(key, "key is null");
        return forRead().containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> void decorate(Key<T> key, Function<? super T, ? extends T> decoratingFunction) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Service> find(Key<?> key) {
        requireNonNull(key, "key is null");
        ServiceBuild sb = forRead().get(key);
        return sb == null ? Optional.empty() : Optional.of(sb.toService());
    }

    private Map<Key<?>, ServiceBuild> forModification() {
        return services;
    }

    private Map<Key<?>, ServiceBuild> forRead() {
        return services;
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        // we allow remove...
        return forModification().keySet();
    }

    public void peek(ServiceWireletFrom wirelet, Consumer<? super ServiceRegistry> action) {
        checkCurrent(wirelet);
        action.accept(AbstractServiceRegistry.copyOf(services));
    }

    /** {@inheritDoc} */
    @Override
    public <T> void provideInstance(Key<T> key, T instance) {}

    /** {@inheritDoc} */
    @Override
    public void rekey(Key<?> existing, Key<?> newKey) {
        requireNonNull(existing, "existing is null");
        requireNonNull(newKey, "newKey is null");
        if (existing.equals(newKey)) {
            throw new IllegalStateException("Cannot rekey the same key, key = " + existing);
        }

        Map<Key<?>, ServiceBuild> services = forModification();
        if (services.containsKey(newKey)) {
            throw new IllegalStateException("A service with newKey already exists, key = " + newKey);
        }
        ServiceBuild s = services.remove(existing);
        if (s == null) {
            throw new NoSuchElementException("No service with the specified key exists, key = " + existing);
        }
        services.put(newKey, s.rekeyAs(newKey));
    }

    /** {@inheritDoc} */
    @Override
    public void rekeyAll(Function<Service, @Nullable Key<?>> function) {
        requireNonNull(function, "function is null");

        Map<Key<?>, ServiceBuild> services = forModification();

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
        this.services = newServices;
    }

    /** {@inheritDoc} */
    @Override
    public void remove(Key<?>... keys) {
        requireNonNull(keys, "keys is null");
        Map<Key<?>, ServiceBuild> services = forModification();
        for (Key<?> k : keys) {
            requireNonNull(k, "key in specified array is null");
            services.remove(k);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeAll() {
        forModification().clear();
    }

    /** {@inheritDoc} */
    @Override
    public void removeIf(Predicate<? super Service> filter) {
        requireNonNull(filter, "filter is null");
        Map<Key<?>, ServiceBuild> services = forModification();
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
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return forRead().size();
    }

    public ServiceContract childContract() {
        throw new UnsupportedOperationException();
    }

    @ExtensionMember(ServiceExtension.class)
    public static abstract class ServiceWireletFrom extends Wirelet {
        protected abstract void process(WireletFromContext context);
    }
}
