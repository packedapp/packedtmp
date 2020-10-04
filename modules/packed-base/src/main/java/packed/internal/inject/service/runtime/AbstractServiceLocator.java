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
import packed.internal.inject.context.PackedProvideContext;

/** An abstract implementation of {@link ServiceLocator}. */
public abstract class AbstractServiceLocator extends AbstractServiceRegistry implements ServiceLocator {

    protected abstract String failedToUseMessage(Key<?> key);

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<T> find(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService<T> s = getService(key);
        if (s == null) {
            return Optional.empty();
        }
        T t = s.forLocator(this);
        return Optional.of(t);
    }

    /** {@inheritDoc} */
    @Override
    public final <T> Optional<Provider<T>> findProvider(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService<T> s = getService(key);
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

    @Nullable
    protected abstract <T> RuntimeService<T> getService(Key<T> key);

    /** {@inheritDoc} */
    @Override
    public final <T> void ifPresent(Key<T> key, Consumer<? super T> action) {
        requireNonNull(key, "key is null");
        requireNonNull(action, "action is null");
        RuntimeService<T> s = getService(key);
        if (s != null) {
            T t = s.forLocator(this);
            action.accept(t);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final <T> T use(Key<T> key) {
        requireNonNull(key, "key is null");
        RuntimeService<T> s = getService(key);
        if (s != null) {
            return s.forLocator(this);
        }
        String msg = failedToUseMessage(key);
        // /child [ss.BaseMyBundle] does not export a service with the specified key
        throw new NoSuchElementException(msg);
    }
}
