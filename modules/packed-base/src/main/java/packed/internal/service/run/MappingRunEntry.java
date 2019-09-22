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
package packed.internal.service.run;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import app.packed.service.InstantiationMode;
import app.packed.service.ServiceRequest;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.BuildEntry;

/**
 *
 */
public class MappingRunEntry<F, T> extends RSE<T> {

    /** The runtime node to delegate to. */
    private final RSE<F> delegate;

    private final Function<F, T> function;

    /**
     * Creates a new runtime alias node.
     *
     * @param delegate
     *            the build time alias node to create a runtime node from
     */
    public MappingRunEntry(BuildEntry<T> buildNode, ServiceEntry<F> delegate, Function<F, T> function) {
        super(buildNode);
        this.delegate = requireNonNull(delegate.toRuntimeEntry());
        this.function = requireNonNull(function);
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return delegate.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ServiceRequest site) {
        F f = delegate.getInstance(site);
        T t = function.apply(f);
        // Check Type, and not null
        return t;
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsInjectionSite() {
        return delegate.needsInjectionSite();
    }
}
