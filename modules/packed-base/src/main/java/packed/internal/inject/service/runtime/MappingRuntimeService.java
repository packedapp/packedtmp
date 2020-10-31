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

import java.lang.invoke.MethodHandle;
import java.util.function.Function;

import app.packed.inject.ProvisionContext;
import packed.internal.inject.service.build.BuildtimeService;

/** A runtime service entry that uses a {@link Function} to map an existing service. */
public final class MappingRuntimeService extends RuntimeService {

    /** The runtime entry whose service should mapped. */
    private final RuntimeService delegate;

    /** The function that maps the service. */
    private final Function<?, ?> function;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public MappingRuntimeService(BuildtimeService buildNode, RuntimeService delegate, Function<?, ?> function) {
        super(buildNode);
        this.delegate = requireNonNull(delegate);
        this.function = requireNonNull(function);
    }

    /** {@inheritDoc} */
    @Override
    public Object getInstance(ProvisionContext site) {
        Object f = delegate.getInstance(site);
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Object t = ((Function) function).apply(f);
        // TODO Check Type, and not null
        // Throw Provision Exception????
        // Every node
        // Vi bliver vel ogsaa noedt til at checke det for en build entry....
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return delegate.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return delegate.requiresPrototypeRequest();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        throw new UnsupportedOperationException();
    }
}
