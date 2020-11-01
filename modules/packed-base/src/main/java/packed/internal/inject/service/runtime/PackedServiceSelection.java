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

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import app.packed.base.Key;
import app.packed.inject.Service;
import app.packed.inject.ServiceSelection;

/**
 * Implementation of {@link ServiceSelection}.
 */
final class PackedServiceSelection<S> extends AbstractServiceLocator implements ServiceSelection<S> {

    /** The services that we wrap */
    // An alternative implementation would be to have a backing map and a filter
    // However then asMap() would be a lot of effort to implement.
    private final Map<Key<?>, RuntimeService> services;

    PackedServiceSelection(Map<Key<?>, RuntimeService> services) {
        this.services = requireNonNull(services);
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map<Key<?>, Service> asMap() {
        return (Map) services;
    }

    /** {@inheritDoc} */
    @Override
    public void forEachInstance(BiConsumer<? super Service, ? super S> action) {
        requireNonNull(action, "action is null");
        for (RuntimeService s : services.values()) {
            @SuppressWarnings("unchecked")
            S instance = (S) s.getInstanceForLocator(this);
            action.accept(s, instance);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void forEachInstance(Consumer<? super S> action) {
        requireNonNull(action, "action is null");
        for (RuntimeService s : services.values()) {
            @SuppressWarnings("unchecked")
            S instance = (S) s.getInstanceForLocator(this);
            action.accept(instance);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Stream<S> instances() {
        return (Stream<S>) services.values().stream();
    }

    /** {@inheritDoc} */
    @Override
    protected String useFailedMessage(Key<?> key) {
        return "No service with the specified key has been selected, key = " + key;
    }
}