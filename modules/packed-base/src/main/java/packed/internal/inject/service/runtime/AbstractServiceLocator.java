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

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.inject.Provider;
import app.packed.inject.ProvisionContext;
import app.packed.inject.ServiceLocator;
import packed.internal.inject.PackedProvisionContext;

/** An abstract implementation of {@link ServiceLocator}. */
public abstract class AbstractServiceLocator extends AbstractServiceRegistry implements ServiceLocator {

    // /child [ss.BaseMyBundle] does not export a service with the specified key
    protected abstract String failedToUseMessage(Key<?> key);

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> findInstance(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = getService(key);
        if (s == null) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        T t = (T) s.forLocator(this);
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = getService(key);
        if (s == null) {
            return Optional.empty();
        }

        Provider<T> provider;
        if (s.isConstant()) {
            @SuppressWarnings("unchecked")
            T t = (T) s.forLocator(this);
            provider = Provider.ofConstant(t);
        } else {
            ProvisionContext pc = PackedProvisionContext.of(key);
            provider = new ServiceWrapperProvider<T>(s, pc);
        }
        return Optional.of(provider);
    }

    @Nullable
    protected abstract RuntimeService getService(Key<?> key);

    /** {@inheritDoc} */
    @Override
    public final <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(key, "key is null");
        requireNonNull(action, "action is null");
        RuntimeService s = getService(key);
        if (s != null) {
            @SuppressWarnings("unchecked")
            T t = (T) s.forLocator(this);
            action.accept(t);
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public final <T> T use(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService s = getService(key);
        if (s != null) {
            return (T) s.forLocator(this);
        }
        String msg = failedToUseMessage(key);
        throw new NoSuchElementException(msg);
    }
}
