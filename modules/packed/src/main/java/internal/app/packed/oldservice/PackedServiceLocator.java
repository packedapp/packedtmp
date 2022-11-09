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
package internal.app.packed.oldservice;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.framework.Key;
import app.packed.operation.Provider;
import app.packed.service.ServiceLocator;
import internal.app.packed.oldservice.runtime.RuntimeService;

/**
 *
 */
public record PackedServiceLocator(Map<Key<?>, ? extends RuntimeService> services) implements ServiceLocator {

    /** {@inheritDoc} */
    @Override
    public <T> Optional<T> findInstance(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = services.get(key);
        if (s == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        T t = (T) s.provideInstance();
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = services.get(key);
        if (s == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        Provider<T> provider = (Provider<T>) s.provider();
        return Optional.of(provider);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        return services.keySet();
    }
}
