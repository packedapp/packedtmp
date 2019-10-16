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
import java.util.Map;

import app.packed.container.Wirelet;
import app.packed.service.Injector;
import app.packed.service.InstantiationMode;
import app.packed.service.ServiceExtension;
import app.packed.util.Nullable;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.run.DelegatingInjectorEntry;
import packed.internal.service.run.InjectorEntry;

/** An entry specifically used for {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
final class FromOtherInjectorBuildEntry<T> extends BuildEntry<T> {

    /** The entry from the 'imported' injector. */
    private final InjectorEntry<T> entry;

    /** A wrapper for the 'imported' injector. */
    final ProvideAllFromOtherInjector fromInjector; // not used currently

    FromOtherInjectorBuildEntry(ProvideAllFromOtherInjector fromInjector, InjectorEntry<T> entry) {
        super(fromInjector.node, fromInjector.configSite.withParent(entry.configSite()), List.of());
        this.entry = entry;
        this.fromInjector = fromInjector;
        this.key = requireNonNull(entry.key());
        this.description = entry.description().orElse(null);
    }

    /** {@inheritDoc} */
    @Override
    @Nullable
    public BuildEntry<?> declaringEntry() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasUnresolvedDependencies() {
        return false; // services from an never needs to resolved
    }

    /** {@inheritDoc} */
    @Override
    public InstantiationMode instantiationMode() {
        return entry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    protected InjectorEntry<T> newRuntimeNode(Map<BuildEntry<?>, InjectorEntry<?>> entries) {
        return new DelegatingInjectorEntry<T>(this, entry);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return entry.requiresPrototypeRequest();
    }
}
