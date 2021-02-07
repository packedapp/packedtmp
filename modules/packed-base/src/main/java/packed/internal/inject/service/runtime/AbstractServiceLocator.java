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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import app.packed.base.Key;
import app.packed.inject.Provider;
import app.packed.inject.Service;
import app.packed.inject.ServiceComposer;
import app.packed.inject.ServiceLocator;
import app.packed.inject.ServiceSelection;
import packed.internal.inject.service.AbstractServiceRegistry;
import packed.internal.inject.service.build.PackedServiceComposer;

/**
 * An abstract implementation of {@link ServiceLocator}. {@link #asMap()} must always return an immutable map, with
 * effectively immutable (frozen) services.
 **/
public abstract class AbstractServiceLocator extends AbstractServiceRegistry implements ServiceLocator {

    /** {@inheritDoc} */
    @Override
    public Map<Key<?>, Service> asMap() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> findInstance(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = (RuntimeService) asMap().get(key);
        if (s == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        T t = (T) s.getInstanceForLocator(this);
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = (RuntimeService) asMap().get(key);
        if (s == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        Provider<T> provider = (Provider<T>) s.getProviderForLocator(this);
        return Optional.of(provider);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(key, "key is null");
        requireNonNull(action, "action is null");
        RuntimeService s = (RuntimeService) asMap().get(key);
        if (s != null) {
            @SuppressWarnings("unchecked")
            T t = (T) s.getInstanceForLocator(this);
            action.accept(t);
        }
    }

    private <T> ServiceSelection<T> select(Predicate<? super Service> filter) {
        HashMap<Key<?>, RuntimeService> m = new HashMap<>();
        for (Service s : asMap().values()) {
            if (filter.test(s)) {
                m.put(s.key(), (RuntimeService) s);
            }
        }
        return new PackedServiceSelection<>(Map.copyOf(m));
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final ServiceSelection<Object> selectAll() {
        return new PackedServiceSelection<>((Map) asMap());
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceSelection<T> selectWithAnyQualifiers(Key<T> key) {
        return select(s -> key.isSuperKeyOf(s.key()));
    }

    /** {@inheritDoc} */
    @Override
    public final <T> ServiceSelection<T> selectAssignableTo(Class<T> key) {
        return select(s -> key.isAssignableFrom(s.key().rawType()));
    }

    /** {@inheritDoc} */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public final ServiceLocator spawn(Consumer<ServiceComposer> transformer) {
        return PackedServiceComposer.transform(transformer, (Collection) asMap().values());
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> T use(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = (RuntimeService) asMap().get(key);
        if (s != null) {
            return (T) s.getInstanceForLocator(this);
        }
        String msg = useFailedMessage(key);
        throw new NoSuchElementException(msg);
    }

    // /child [ss.BaseMyBundle] does not export a service with the specified key
    protected abstract String useFailedMessage(Key<?> key);
}
