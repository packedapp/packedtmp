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
package packed.internal.service.buildtime.service;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandle;

import app.packed.base.Nullable;
import app.packed.component.Wirelet;
import app.packed.service.Injector;
import app.packed.service.ServiceExtension;
import packed.internal.service.buildtime.BuildEntry;
import packed.internal.service.buildtime.ServiceExtensionInstantiationContext;
import packed.internal.service.buildtime.ServiceMode;
import packed.internal.service.runtime.DelegatingInjectorEntry;
import packed.internal.service.runtime.RuntimeEntry;

/** An entry specifically used for {@link ServiceExtension#provideAll(Injector, Wirelet...)}. */
final class FromOtherInjectorBuildEntry<T> extends BuildEntry<T> {

    /** The entry from the 'imported' injector. */
    private final RuntimeEntry<T> entry;

    /** A wrapper for the 'imported' injector. */
    final ProvideAllFromOtherInjector fromInjector; // not used currently

    FromOtherInjectorBuildEntry(ProvideAllFromOtherInjector fromInjector, RuntimeEntry<T> entry) {
        super(fromInjector.node, fromInjector.configSite.withParent(entry.configSite()));
        this.entry = entry;
        this.fromInjector = fromInjector;
        this.key = requireNonNull(entry.key());
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
    public ServiceMode instantiationMode() {
        return entry.instantiationMode();
    }

    /** {@inheritDoc} */
    @Override
    protected RuntimeEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        return new DelegatingInjectorEntry<T>(this, entry);
    }

    /** {@inheritDoc} */
    @Override
    public boolean requiresPrototypeRequest() {
        return entry.requiresPrototypeRequest();
    }

    /** {@inheritDoc} */
    @Override
    protected MethodHandle newMH(ServiceProvidingManager spm) {
        throw new UnsupportedOperationException();
    }
}
