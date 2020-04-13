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
package packed.internal.service.runtime;

import static java.util.Objects.requireNonNull;

import app.packed.service.ServiceMode;
import packed.internal.service.buildtime.BuildEntry;
import app.packed.service.ProvideContext;

/**
 * A delegating runtime service node.
 * <p>
 * This type is used for exported nodes as well as nodes that are imported from other containers.
 */
public final class DelegatingInjectorEntry<T> extends InjectorEntry<T> {

    /** The runtime node to delegate to. */
    private final InjectorEntry<T> delegate;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public DelegatingInjectorEntry(BuildEntry<T> buildNode, InjectorEntry<T> delegate) {
        super(buildNode);
        this.delegate = requireNonNull(delegate);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return delegate.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideContext site) {
        return delegate.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return delegate.requiresPrototypeRequest();
    }
}
