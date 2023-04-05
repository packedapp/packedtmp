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

import app.packed.extension.BaseExtensionPoint.CodeGenerated;
import app.packed.extension.ExtensionContext;
import app.packed.operation.Provider;
import app.packed.service.ServiceLocator;
import app.packed.service.ServiceSelection;
import app.packed.util.Key;
import internal.app.packed.util.ThrowableUtil;

/** Default implementation of ServiceLocator. */
public record PackedServiceLocator(ExtensionContext context, @CodeGenerated Map<Key<?>, MethodHandle> entries) implements ServiceLocator {

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        return ServiceLocator.super.use(key);
    }

    /** {@inheritDoc} */
    @Override
    public boolean contains(Key<?> key) {
        return entries.containsKey(key);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<T> findInstance(Key<T> key) {
        requireNonNull(key, "key is null");
        MethodHandle provider = entries.get(key);

        // Test if a provider was found that matches the specified key
        if (provider == null) {
            return Optional.empty();
        }

        T t;
        try {
            // Call the provider
            t = (T) provider.invokeExact(context);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        return entries.size();
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        MethodHandle provider = entries.get(key);
        if (provider == null) {
            return Optional.empty();
        }
        Provider<T> p = new Provider<>() {

            @Override
            public T provide() {
                try {
                    return (T) provider.invokeExact(context);
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
        return entries.keySet();
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
