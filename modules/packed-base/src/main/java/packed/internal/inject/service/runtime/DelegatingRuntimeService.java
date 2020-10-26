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

import app.packed.inject.ProvisionContext;
import packed.internal.inject.service.build.ServiceBuild;

/**
 * A delegating runtime service node.
 * <p>
 * This type is used for exported nodes as well as nodes that are imported from other containers.
 */
public final class DelegatingRuntimeService extends RuntimeService {

    /** The runtime node to delegate to. */
    private final RuntimeService delegate;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public DelegatingRuntimeService(ServiceBuild buildNode, RuntimeService delegate) {
        super(buildNode);
        this.delegate = requireNonNull(delegate);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstant() {
        return delegate.isConstant();
    }

    /** {@inheritDoc} */
    @Override
    public Object getInstance(ProvisionContext site) {
        return delegate.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return delegate.requiresPrototypeRequest();
    }

    /** {@inheritDoc} */
    @Override
    public MethodHandle dependencyAccessor() {
        return delegate.dependencyAccessor();
    }
}
