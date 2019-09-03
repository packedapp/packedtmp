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
package packed.internal.inject.run;

import static java.util.Objects.requireNonNull;

import app.packed.inject.InstantiationMode;
import app.packed.inject.ProvideHelper;
import packed.internal.inject.ServiceEntry;
import packed.internal.inject.build.BSE;

/**
 * A delegating runtime service node.
 * <p>
 * This type is used for exported nodes as well as nodes that are imported from other containers.
 */
public final class RSEDelegate<T> extends RSE<T> {

    /** The runtime node to delegate to. */
    private final RSE<T> delegate;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public RSEDelegate(BSE<T> buildNode, ServiceEntry<T> delegate) {
        super(buildNode);
        this.delegate = requireNonNull(delegate.toRuntimeEntry());
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return delegate.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ProvideHelper site) {
        return delegate.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return delegate.needsInjectionSite();
    }
}
