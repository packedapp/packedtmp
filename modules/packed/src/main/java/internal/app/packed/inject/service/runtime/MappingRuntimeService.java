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
package internal.app.packed.inject.service.runtime;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.base.Key;

/** A runtime service that uses a {@link Function} to map a service instance from another service. */
public final class MappingRuntimeService implements RuntimeService {

    /** The runtime entry whose service should mapped. */
    private final RuntimeService delegate;

    /** The function that maps the service. */
    private final Function<?, ?> function;

    /** The key under which the service is available. */
    private final Key<?> key;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public MappingRuntimeService(Key<?> key, RuntimeService delegate, Function<?, ?> function) {
        this.key = requireNonNull(key);
        this.delegate = requireNonNull(delegate);
        this.function = requireNonNull(function);
    }
    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    public Key<?> key() {
        return key;
    }

    /** {@inheritDoc} */
    @Override
    public Object provideInstance() {
        Object other = delegate.provideInstance();

        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object t = ((Function) function).apply(other);

        if (!key.rawType().isInstance(t)) {
            if (t == null) {
                throw new NullPointerException();
            }
            throw new RuntimeException();
        }
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresProvisionContext() {
        return delegate.requiresProvisionContext();
    }

    @Override
    public String toString() {
        return RuntimeService.toString(this);
    }
    @Override
    public boolean isConstant() {
        return delegate.isConstant();
    }
}
