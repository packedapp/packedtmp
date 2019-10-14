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
package packed.internal.service.build.service;

import static java.util.Objects.requireNonNull;

import java.util.List;

import app.packed.container.Wirelet;
import app.packed.service.Injector;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceExtension;
import app.packed.service.ServiceRequest;
import app.packed.util.Key;
import app.packed.util.Nullable;
import packed.internal.service.ServiceEntry;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.run.RuntimeEntry;
import packed.internal.service.run.DelegatingRuntimeEntry;

/** An entry specifically used for {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
final class ProvideAllBuildEntry<T> extends BuildEntry<T> {

    /** The entry from the 'imported' injector. */
    final ServiceEntry<T> entry;

    /** A wrapper for the 'imported' injector. */
    final ProvideAllFromInjector fromInjector;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    ProvideAllBuildEntry(ProvideAllFromInjector fromInjector, ServiceEntry<T> entry) {
        super(fromInjector.node, fromInjector.configSite.withParent(entry.configSite()), List.of());
        this.entry = requireNonNull(entry);
        this.fromInjector = requireNonNull(fromInjector);
        this.as((Key) entry.key());
        this.description = entry.description().orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return (entry instanceof BuildEntry) ? ((BuildEntry<?>) entry).declaringEntry() : null;
    }

    /** {@inheritDoc} */
    @Override
    public T getInstance(ServiceRequest site) {
        return entry.getInstance(site);
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return entry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    public boolean needsServiceRequest() {
        return entry.needsServiceRequest();
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return false;// entry.needsResolving();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode() {
        return new DelegatingRuntimeEntry<T>(this, entry);
    }
}
