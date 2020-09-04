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

import app.packed.inject.ProvideContext;
import packed.internal.component.NodeStore;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceMode;

/** An entry holding a constant. */
// Can't implement both ServiceDescriptor and Provider...
public final class IndexedInjectorEntry<T> extends RuntimeEntry<T> {

    private final NodeStore ns;

    private final int index;

    /**
     * Creates a new entry.
     *
     * @param entry
     *            the build entry to create this entry from
     */
    public IndexedInjectorEntry(BuildEntry<T> entry, NodeStore ns, int index) {
        super(entry);
        this.ns = requireNonNull(ns);
        this.index = index;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public T getInstance(ProvideContext ignore) {
        return (T) ns.getSingletonInstance(index);
    }

    /** {@inheritDoc} */
    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.CONSTANT;
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
