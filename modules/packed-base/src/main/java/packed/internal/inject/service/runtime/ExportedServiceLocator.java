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
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.inject.Provider;
import app.packed.inject.ProvisionContext;
import app.packed.inject.Service;
import app.packed.inject.ServiceLocator;
import packed.internal.component.ComponentNode;
import packed.internal.inject.context.PackedProvideContext;

/**
 *
 */
public final class ExportedServiceLocator extends AbstractServiceRegistry implements ServiceLocator {

    private final ComponentNode component;

    /** All services that this injector provides. */
    private final Map<Key<?>, RuntimeService<?>> services;

    public ExportedServiceLocator(ComponentNode component, Map<Key<?>, RuntimeService<?>> services) {
        this.services = requireNonNull(services);
        this.component = requireNonNull(component);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected Map<Key<?>, Service> services() {
        return (Map) services;
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<T> find(Key<T> key) {
        requireNonNull(key, "key is null");
        @SuppressWarnings("unchecked")
        RuntimeService<T> s = (RuntimeService<T>) services.get(key);
        if (s == null) {
            return Optional.empty();
        }
        T t = s.forLocator(this);
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        @SuppressWarnings("unchecked")
        RuntimeService<T> s = (RuntimeService<T>) services.get(key);
        if (s == null) {
            return Optional.empty();
        }

        Provider<T> provider;
        if (s.isConstant()) {
            T t = s.forLocator(this);
            provider = Provider.ofConstant(t);
        } else {
            ProvisionContext pc = PackedProvideContext.of(key);
            provider = new NonConstantLocatorProvider<T>(s, pc);
        }
        return Optional.of(provider);
    }

    /** {@inheritDoc} */
    @Override
    public <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(key, "key is null");
        requireNonNull(action, "action is null");
        @SuppressWarnings("unchecked")
        RuntimeService<T> s = (RuntimeService<T>) services.get(key);
        if (s != null) {
            T t = s.forLocator(this);
            action.accept(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public <T> T use(Key<T> key) {
        requireNonNull(key, "key is null");
        @SuppressWarnings("unchecked")
        RuntimeService<T> s = (RuntimeService<T>) services.get(key);
        if (s != null) {
            return s.forLocator(this);
        }
        // /child [ss.BaseMyBundle] does not export a service with the specified key
        throw new NoSuchElementException("'" + component.path() + "' does not export a service with the specified key, key = " + key);
    }
}
