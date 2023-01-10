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
package internal.app.packed.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import app.packed.binding.Key;
import app.packed.binding.Provider;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceSelection;
import internal.app.packed.lifetime.PackedExtensionContext;
import internal.app.packed.util.ThrowableUtil;

/**
 *
 */
public record PackedServiceLocator(PackedExtensionContext pec, Map<Key<?>, MethodHandle> map) implements ServiceLocator {

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        MethodHandle provider = map.get(key);
        if (provider == null) {
            return Optional.empty();
        }
        Provider<T> p = new Provider<>() {

            @Override
            public T provide() {
                try {
                    return (T) provider.invoke(pec);
                } catch (Throwable e) {
                    throw ThrowableUtil.orUndeclared(e);
                }
            }
        };
        return Optional.of(p);
    }

    /** {@inheritDoc} */
    @Override
    public Set<Key<?>> keys() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    @Override
    public ServiceSelection<?> selectAll() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public <T> ServiceSelection<T> selectAssignableTo(Class<T> type) {
       throw new UnsupportedOperationException();
    }
}
